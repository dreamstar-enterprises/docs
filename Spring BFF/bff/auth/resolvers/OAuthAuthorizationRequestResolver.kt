package com.example.bff.auth.resolvers

import com.example.bff.auth.resolvers.customizers.AdditionalParamsAuthorizationRequestCustomizer
import com.example.bff.auth.resolvers.customizers.CompositeOAuth2AuthorizationRequestCustomizer
import com.example.bff.props.LoginProperties
import com.example.bff.props.RequestParameterProperties
import com.example.bff.props.ServerProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.util.regex.Pattern

/**********************************************************************************************************************/
/***************************************************** RESOLVER *******************************************************/
/**********************************************************************************************************************/

// more here:
// https://www.baeldung.com/spring-security-pkce-secret-clients
// https://github.com/ch4mpy/spring-addons/tree/master/spring-addons-starter-oidc

/**
 * Adds custom parameters (from application properties) to the authorization code request
 */
@Component
internal class OAuthAuthorizationRequestResolver(
    private val serverProperties: ServerProperties,
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
    private val loginProperties: LoginProperties,
    private val requestParameterProperties: RequestParameterProperties
) : ServerOAuth2AuthorizationRequestResolver {


    // instance variables
    private final var authorizationRequestMatcher: ServerWebExchangeMatcher
    private final var requestCustomizers: Map<String, CompositeOAuth2AuthorizationRequestCustomizer> = emptyMap()
    final val clientRegistrations = (clientRegistrationRepository as? InMemoryReactiveClientRegistrationRepository)?.toList()
        ?: emptyList()


    // initialize instance variables here
    init {
        this.authorizationRequestMatcher =
            PathPatternParserServerWebExchangeMatcher(
                DefaultServerOAuth2AuthorizationRequestResolver.DEFAULT_AUTHORIZATION_REQUEST_PATTERN
            )

        this.requestCustomizers = clientRegistrations.associate { clientRegistration ->
            val key = clientRegistration.registrationId
            val requestCustomizer = CompositeOAuth2AuthorizationRequestCustomizer()

            // add additional authorization request parameters!
            val additionalProperties: MultiValueMap<String, String> =
                requestParameterProperties.getExtraAuthorizationParameters(key)

            if (additionalProperties.size > 0) {
                requestCustomizer.addCustomizer(
                    AdditionalParamsAuthorizationRequestCustomizer(additionalProperties)
                )
            }

            // add pkce
            requestCustomizer.addCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce())

            // return entry
            key to requestCustomizer
        }

    }


    // checks if HTTP request satisfies request matcher; then extracts registration id; runs the other resolver
    override fun resolve(exchange: ServerWebExchange?): Mono<OAuth2AuthorizationRequest> {
        // @formatter:off
        return this.authorizationRequestMatcher
            .matches(exchange)
            .filter { matchResult -> matchResult.isMatch }
            .map { matchResult -> matchResult.variables }
            .mapNotNull { variables -> variables[DefaultServerOAuth2AuthorizationRequestResolver.DEFAULT_REGISTRATION_ID_URI_VARIABLE_NAME] }
            .map { it as String }
            .flatMap { clientRegistrationId -> resolve(exchange, clientRegistrationId) }
        // @formatter:on
    }


    // runs saveURIs to session function; then gets the request resolver; then runs post processor
    override fun resolve(
        exchange: ServerWebExchange?,
        clientRegistrationId: String?
    ): Mono<OAuth2AuthorizationRequest> {
        if (exchange == null || clientRegistrationId == null) {
            return Mono.empty()
        }
        // obtain the request resolver
        val delegate = getRequestResolver(exchange, clientRegistrationId) ?: return Mono.empty()

        // process and return the OAuth2AuthorizationRequest
        return savePostLoginUrisInSession(exchange)
            // resolve the authorization request
            .then(delegate.resolve(exchange, clientRegistrationId))
            // post-process the resolved request
            .map { postProcess(it) }
    }


    // saves post login uris (in header or query parameter) to session
    private fun savePostLoginUrisInSession(exchange: ServerWebExchange): Mono<WebSession> {
        val request = exchange.request
        val headers = request.headers
        val params = request.queryParams

        return exchange.session.map { session ->

            // get and process the success URI
            val postLoginSuccessUri = headers.getFirst(loginProperties.POST_AUTHENTICATION_SUCCESS_URI_HEADER)
                ?: params.getFirst(loginProperties.POST_AUTHENTICATION_SUCCESS_URI_PARAM)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { URI.create(it) }
            postLoginSuccessUri?.let {
                session.attributes[loginProperties.POST_AUTHENTICATION_SUCCESS_URI_SESSION_ATTRIBUTE] = it
                println("SESSION SUCCESS ATTRIBUTE SET: ${session.attributes[loginProperties.POST_AUTHENTICATION_SUCCESS_URI_SESSION_ATTRIBUTE]}")
            }

            // get and process the failure URI
            val postLoginFailureUri = headers.getFirst(loginProperties.POST_AUTHENTICATION_FAILURE_URI_HEADER)
                ?: params.getFirst(loginProperties.POST_AUTHENTICATION_FAILURE_URI_PARAM)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { URI.create(it) }
            postLoginFailureUri?.let {
                session.attributes[loginProperties.POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE] = it
                println("SESSION FAILURE ATTRIBUTE SET: ${session.attributes[loginProperties.POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE]}")
            }

            session
        }
    }


    // potentially modifies the re-direct URI, to handle a proxy server configuration
    private fun postProcess(request: OAuth2AuthorizationRequest): OAuth2AuthorizationRequest {

        // create a mutable copy of the original request
        val modified = OAuth2AuthorizationRequest.from(request)

        // parse the original redirect URI
        val original = URI.create(request.redirectUri)

        // update redirect URI
        val baseUri = URI.create(serverProperties.clientUri)

        // extract the authorities
        val originalAuthority = original.authority
        val baseAuthority = baseUri.authority

        // extract the first elements of the paths
        val originalFirstElement = original.path.split("/").getOrNull(1)
        val baseFirstElement = baseUri.path.split("/").getOrNull(1)

        // check if the authorities and the first elements of the paths match
        val redirectUri = if (originalAuthority != baseAuthority && originalFirstElement != baseFirstElement) {

            // build the new redirect URI with the original path, query, and fragment
            UriComponentsBuilder.fromUri(baseUri)
                .path(original.path)
                .query(original.query)
                .fragment(original.fragment)
                .build()
                .toUriString()
        } else {
            original.toString()
        }

        // set the modified redirect URI
        modified.redirectUri(redirectUri)

        // log the changes
        logger.debug("Changed OAuth2AuthorizationRequest redirectUri from {} to {}", original, redirectUri)

        // return the modified request
        return modified.build()
    }


    /**
     * See getOAuth2AuthorizationRequestCustomizer to add advanced request customizer(s)
     *
     * @param exchange
     * @param clientRegistrationId
     * @return
     */
    protected fun getRequestResolver(exchange: ServerWebExchange, clientRegistrationId: String): ServerOAuth2AuthorizationRequestResolver? {
        val requestCustomizer = getOAuth2AuthorizationRequestCustomizer(exchange, clientRegistrationId)
        if (requestCustomizer == null) {
            return null
        }

        val delegate = DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository)
        delegate.setAuthorizationRequestCustomizer(requestCustomizer)

        return delegate
    }


    /**
     * Override this to use a "dynamic" request customizer. Something like:
     *
     * <pre>
     * return CompositeOAuth2AuthorizationRequestCustomizer(
     *     getCompositeOAuth2AuthorizationRequestCustomizer(clientRegistrationId),
     *     MyDynamicCustomizer(request),
     *     ...
     * )
     * </pre>
     *
     * @param exchange The ServerWebExchange to customize the request.
     * @param clientRegistrationId The client registration ID.
     * @return A Consumer for customizing OAuth2AuthorizationRequest.
     */
    protected fun getOAuth2AuthorizationRequestCustomizer(
        exchange: ServerWebExchange,
        clientRegistrationId: String
    ): CompositeOAuth2AuthorizationRequestCustomizer? {
        return getCompositeOAuth2AuthorizationRequestCustomizer(clientRegistrationId)
    }


    /**
     * @param clientRegistrationId The client registration ID.
     * @return A request customizer adding PKCE token (if activated) and "static" parameters defined in spring-addons properties.
     */
    protected fun getCompositeOAuth2AuthorizationRequestCustomizer(
        clientRegistrationId: String
    ): CompositeOAuth2AuthorizationRequestCustomizer? {
        return this.requestCustomizers[clientRegistrationId]
    }


    // static variables (companion object)
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(OAuthAuthorizationRequestResolver::class.java)
        private val authorizationRequestPattern: Pattern = Pattern.compile("/oauth2/authorization/([^/]+)")

        /**
         * Resolves the registration ID from the exchange's request path.
         *
         * @param exchange The ServerWebExchange to get the request from.
         * @return The resolved registration ID.
         */
        @JvmStatic
        fun resolveRegistrationId(exchange: ServerWebExchange): String? {
            val requestPath = exchange.request.path.toString()
            return resolveRegistrationId(requestPath)
        }

        /**
         * Resolves the registration ID from the request path.
         *
         * @param requestPath The request path.
         * @return The resolved registration ID.
         */
        @JvmStatic
        fun resolveRegistrationId(requestPath: String): String? {
            val matcher = authorizationRequestPattern.matcher(requestPath)
            return if (matcher.matches()) matcher.group(1) else null
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/