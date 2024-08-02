package com.example.bff.auth.resolvers

import com.c4_soft.springaddons.security.oidc.starter.CompositeOAuth2AuthorizationRequestCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.stereotype.Component
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

@Component
internal class OAuthAuthorizationRequestResolver(
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
    bootClientProperties: OAuth2ClientProperties
) : ServerOAuth2AuthorizationRequestResolver {

    @Value("\${reverse-proxy-uri}")
    private lateinit var reverseProxyUri: String

    @Value("\${bff-prefix}")
    private lateinit var bffPrefix: String

    // static variables (companion object)
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(OAuthAuthorizationRequestResolver::class.java)

        // constants
        private const val POST_AUTHENTICATION_SUCCESS_URI_HEADER: String = "X-POST-LOGIN-SUCCESS-URI"
        private const val POST_AUTHENTICATION_SUCCESS_URI_PARAM: String = "post_login_success_uri"
        private const val POST_AUTHENTICATION_SUCCESS_URI_SESSION_ATTRIBUTE = POST_AUTHENTICATION_SUCCESS_URI_PARAM;

        private const val POST_AUTHENTICATION_FAILURE_URI_HEADER: String = "X-POST-LOGIN-FAILURE-URI"
        private const val POST_AUTHENTICATION_FAILURE_URI_PARAM: String = "post_login_failure_uri"
        private const val POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE: String = POST_AUTHENTICATION_FAILURE_URI_PARAM

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

    // instance variables (re-assignable)
    private final var authorizationRequestMatcher: ServerWebExchangeMatcher
    private final var requestCustomizers: Map<String, CompositeOAuth2AuthorizationRequestCustomizer> = emptyMap()

    // initialize instance variables here
    init {
        this.authorizationRequestMatcher =
            PathPatternParserServerWebExchangeMatcher(
                DefaultServerOAuth2AuthorizationRequestResolver.DEFAULT_AUTHORIZATION_REQUEST_PATTERN
            )

        this.requestCustomizers = bootClientProperties.registration.entries.associate { (key, _) ->
            val requestCustomizer = CompositeOAuth2AuthorizationRequestCustomizer()
            requestCustomizer.addCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce())
            key to requestCustomizer
        }
    }

    // save post login uris (in header or query parameter) to session
    private fun savePostLoginUrisInSession(exchange: ServerWebExchange): Mono<WebSession> {
        val request = exchange.request
        val headers = request.headers
        val params = request.queryParams

        return exchange.session.map { session ->

            // get and process the success URI
            val postLoginSuccessUri = headers.getFirst(POST_AUTHENTICATION_SUCCESS_URI_HEADER)
                ?: params.getFirst(POST_AUTHENTICATION_SUCCESS_URI_PARAM)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { URI.create(it) }
            postLoginSuccessUri?.let {
                session.attributes[POST_AUTHENTICATION_SUCCESS_URI_SESSION_ATTRIBUTE] = it
            }

            // get and process the failure URI
            val postLoginFailureUri = headers.getFirst(POST_AUTHENTICATION_FAILURE_URI_HEADER)
                ?: params.getFirst(POST_AUTHENTICATION_FAILURE_URI_PARAM)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { URI.create(it) }
            postLoginFailureUri?.let {
                session.attributes[POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE] = it
            }

            session
        }
    }

    // ensure the authority on the redirect URI is the client URI (i.e. the bff, reverse proxy address)
    private fun postProcess(request: OAuth2AuthorizationRequest): OAuth2AuthorizationRequest {
        // create a mutable copy of the original request
        val modified = OAuth2AuthorizationRequest.from(request)

        // parse the original redirect URI
        val original = URI.create(request.redirectUri)

        // update redirect URI
        val baseUri = URI.create(reverseProxyUri + bffPrefix)

        println("NEW REDIRECT URL: $baseUri")

        // build the new redirect URI with the original path, query, and fragment
        val redirectUri = UriComponentsBuilder.fromUri(baseUri)
            .path(original.path)
            .query(original.query)
            .fragment(original.fragment)
            .build()
            .toUriString()

        // set the modified redirect URI
        modified.redirectUri(redirectUri)

        // log the changes
        logger.debug("Changed OAuth2AuthorizationRequest redirectUri from {} to {}", original, redirectUri)

        // Return the modified request
        return modified.build()
    }

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

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/