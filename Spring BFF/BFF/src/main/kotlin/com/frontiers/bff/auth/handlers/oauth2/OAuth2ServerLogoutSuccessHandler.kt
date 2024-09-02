package com.frontiers.bff.auth.handlers.oauth2

import com.frontiers.bff.auth.handlers.oauth2.builders.LogoutRequestUriBuilder
import com.frontiers.bff.auth.redirects.OAuth2ServerRedirectStrategy
import com.frontiers.bff.props.LogoutProperties
import com.frontiers.bff.props.OAuth2RedirectionProperties
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

/**
 * Determines the redirection URI based on headers, query parameters, or a default value.
 * Constructs the appropriate logout URI using client registration details and ID token.
 * Uses OAuth2ServerRedirectStrategy to handle the actual redirection process
 */
@Component
internal class OAuth2ServerLogoutSuccessHandler(
    private val uriBuilder: LogoutRequestUriBuilder,
    private val clientRegistrationRepo: ReactiveClientRegistrationRepository,
    private val logoutProperties: LogoutProperties,
    oauth2RedirectionProperties: OAuth2RedirectionProperties,
) : ServerLogoutSuccessHandler {

    private val logger = LoggerFactory.getLogger(OAuth2ServerLogoutSuccessHandler::class.java)

    private val defaultPostLogoutUri: String? = logoutProperties.getPostLogoutRedirectUri()?.toString()
    private val redirectStrategy = OAuth2ServerRedirectStrategy(
        oauth2RedirectionProperties.rpInitiatedLogout,
        oauth2RedirectionProperties
    )

    override fun onLogoutSuccess(exchange: WebFilterExchange, authentication: Authentication): Mono<Void> {

        logger.info("Successfully logged out: {}", authentication.name)

        // run, if there is an authentication token
        if (authentication is OAuth2AuthenticationToken) {
            val oauth = authentication

            // get logout uri from
            val postLogoutUri = exchange.exchange.request.headers
                .getFirst(logoutProperties.POST_LOGOUT_SUCCESS_URI_HEADER)
                ?: exchange.exchange.request.queryParams
                    .getFirst(logoutProperties.POST_LOGOUT_SUCCESS_URI_PARAM)
                ?: defaultPostLogoutUri

            // perform a redirection to the constructed URI
            return clientRegistrationRepo
                .findByRegistrationId(oauth.authorizedClientRegistrationId)
                .flatMap { client ->
                    val idToken = (oauth.principal as OidcUser).idToken.tokenValue
                    val uri = if (postLogoutUri.isNullOrBlank()) {
                        uriBuilder.getLogoutRequestUri(client, idToken)
                    } else {
                        uriBuilder.getLogoutRequestUri(client, idToken, URI.create(postLogoutUri))
                    }
                    Mono.justOrEmpty(uri)
                }
                .flatMap { logoutUri ->

                    logger.info("ON LOGOUT REDIRECT URI: $logoutUri")

                    redirectStrategy.sendRedirect(
                        exchange.exchange,
                        URI.create(logoutUri)
                    )
                }
        }
        return Mono.empty()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/