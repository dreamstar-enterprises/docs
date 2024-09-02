package com.frontiers.bff.auth.handlers.oauth2.builders

import com.frontiers.bff.props.LogoutProperties
import com.frontiers.bff.props.OAuth2LogoutProperties
import com.frontiers.bff.props.OAuth2RedirectionProperties
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.nio.charset.StandardCharsets

/**********************************************************************************************************************/
/****************************************************** BUILDER *******************************************************/
/**********************************************************************************************************************/

/**
 * logoutRequestUriBuilder: builder for RP-Initiated Logout queries, taking configuration from properties for
 * OIDC providers which do not strictly comply with the spec: logout URI not provided by OIDC conf or non-standard
 * parameter names (Auth0 and Cognito are samples of such OPs)
 * Overall, gets logout request URI (which may call the logout endpoint of the authentication provider)
 * So, ensures that the logout request is properly constructed and redirects the user to the appropriate URI
 * based on the OAuth2 and application-specific configurations.
 */

@Component
internal class OAuth2LogoutRequestUriBuilder(
    private val oauth2RedirectionProperties: OAuth2RedirectionProperties,
    private val logoutProperties: LogoutProperties
) : LogoutRequestUriBuilder {

    private val logger = LoggerFactory.getLogger(OAuth2LogoutRequestUriBuilder::class.java)

    companion object {
        private const val OIDC_RP_INITIATED_LOGOUT_CONFIGURATION_ENTRY = "end_session_endpoint"
        private const val OIDC_RP_INITIATED_LOGOUT_CLIENT_ID_REQUEST_PARAM = "client_id"
        private const val OIDC_RP_INITIATED_LOGOUT_ID_TOKEN_HINT_REQUEST_PARAM = "id_token_hint"
        private const val OIDC_RP_INITIATED_LOGOUT_POST_LOGOUT_URI_REQUEST_PARAM = "post_logout_redirect_uri"
    }

    /*************************/
    /* MAIN FUNCTIONS        */
    /*************************/

    // if postLogout URI call this function
    override fun getLogoutRequestUri(
        clientRegistration: ClientRegistration,
        idToken: String,
        postLogoutUri: URI?
    ): String? {

        logger.info("Getting logout request URI")

        // get client registration logout properties (if they exist) from custom OAuth2 Logout Hashmap
        val logoutProps = oauth2RedirectionProperties.getLogoutProperties(clientRegistration.registrationId)

        // if logout properties exist and rp-initialied logout is not enabled,
        // return postLogout URI (don't go to logout endpoint)
        if (logoutProps?.rpInitiatedLogoutEnabled == false) {
            return postLogoutUri?.toString()?.takeIf { it.isNotBlank() }
        }

        // otherwise, go and get the logout endpoint on the auth server (i.e. end session endpoint):
        val logoutEndpointUri = getLogoutEndpointUri(logoutProps, clientRegistration)
            ?: throw MisconfiguredProviderException(clientRegistration.registrationId)

        // create builder from logout endpoint
        val builder = UriComponentsBuilder.fromUri(logoutEndpointUri)

        // add token hint request parameter, with idToken, to builder
        getIdTokenHintRequestParam(logoutProps).let { idTokenHintParamName ->
            if (idToken.isNotBlank()) {
                builder.queryParam(idTokenHintParamName, idToken)
            }
        }

        // add client id request parameter, with clientId, to builder
        getClientIdRequestParam(logoutProps).let { clientIdParamName ->
            clientRegistration.clientId?.takeIf { it.isNotBlank() }?.let { clientId ->
                builder.queryParam(clientIdParamName, clientId)
            }
        }

        // add post logout request parameter, with postLogoutURI, to builder
        getPostLogoutUriRequestParam(logoutProps).let { postLogoutUriParamName ->
            postLogoutUri?.toString()?.takeIf { it.isNotBlank() }?.let { uri ->
                builder.queryParam(postLogoutUriParamName, uri)
            }
        }

        return builder.encode(StandardCharsets.UTF_8).build().toUriString()
    }

    // if no postLogout URI exists then still call the main function above!
    override fun getLogoutRequestUri(
        clientRegistration: ClientRegistration,
        idToken: String
    ): String? {
        return getLogoutRequestUri(
            clientRegistration,
            idToken,
            logoutProperties.getPostLogoutRedirectUri()
        )
    }

    /*************************/
    /* HELPER FUNCTIONS      */
    /*************************/
    // get LogoutEndPoint URI
    fun getLogoutEndpointUri(
        logoutProps: OAuth2LogoutProperties?,
        clientRegistration: ClientRegistration
    ): URI? {
        // if logout properties exist & rp-initiated logout is enabled, return OAuth2 property logout endpoint
        return logoutProps?.let {
            if (it.rpInitiatedLogoutEnabled) {
                it.uri
            } else {
                null
            }
        }
        // otherwise, if logout properties do not exist
        // return standard OIDC logout endpoint (as defined in client registrations)
            ?: run {
            val oidcConfig = clientRegistration.providerDetails.configurationMetadata
            oidcConfig[OIDC_RP_INITIATED_LOGOUT_CONFIGURATION_ENTRY]?.toString()?.let { URI.create(it) }
        }
    }

    // get logoutTokenHint request parameter
    fun getIdTokenHintRequestParam(
        logoutProps: OAuth2LogoutProperties?
    ): String {
        return logoutProps?.idTokenHintRequestParam ?: OIDC_RP_INITIATED_LOGOUT_ID_TOKEN_HINT_REQUEST_PARAM
    }

    // get clientID request parameter
    fun getClientIdRequestParam(
        logoutProps: OAuth2LogoutProperties?): String {
        return logoutProps?.clientIdRequestParam ?: OIDC_RP_INITIATED_LOGOUT_CLIENT_ID_REQUEST_PARAM
    }

    // get postLogout URI request param
    fun getPostLogoutUriRequestParam(
        logoutProps: OAuth2LogoutProperties?
    ): String {
        return logoutProps?.postLogoutUriRequestParam ?: OIDC_RP_INITIATED_LOGOUT_POST_LOGOUT_URI_REQUEST_PARAM
    }

    // misconfigured provider exception
    internal class MisconfiguredProviderException(clientRegistrationId: String) : RuntimeException(
        "OAuth2 client registration for $clientRegistrationId RP-Initiated Logout is misconfigured: it is neither OIDC compliant nor defined in spring-addons properties"
    ) {
        companion object {
            private const val serialVersionUID = -6023478025541369262L
        }
    }

}

/**********************************************************************************************************************/
/***************************************************** INTERFACES *****************************************************/
/**********************************************************************************************************************/

internal interface LogoutRequestUriBuilder {

    fun getLogoutRequestUri(
        clientRegistration: ClientRegistration,
        idToken: String
    ): String?

    fun getLogoutRequestUri(
        clientRegistration: ClientRegistration,
        idToken: String,
        postLogoutUri: URI?
    ): String?
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/