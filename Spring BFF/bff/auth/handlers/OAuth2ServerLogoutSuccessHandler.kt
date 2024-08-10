package com.example.bff.auth.handlers

import com.example.bff.auth.handlers.builders.LogoutRequestUriBuilder
import com.example.bff.auth.redirects.OAuth2ServerRedirectStrategy
import com.example.bff.props.LogoutProperties
import com.example.bff.props.OAuth2RedirectionProperties
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
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

@Component
internal class OAuth2ServerLogoutSuccessHandler(
    private val uriBuilder: LogoutRequestUriBuilder,
    private val clientRegistrationRepo: ReactiveClientRegistrationRepository,
    private val logoutProperties: LogoutProperties,
    oauth2RedirectionProperties: OAuth2RedirectionProperties,
) : ServerLogoutSuccessHandler {

    private val defaultPostLogoutUri: String? = logoutProperties.getPostLogoutRedirectUri()?.toString()
    private val redirectStrategy = OAuth2ServerRedirectStrategy(oauth2RedirectionProperties.rpInitiatedLogout)

    override fun onLogoutSuccess(exchange: WebFilterExchange, authentication: Authentication): Mono<Void> {
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
                    redirectStrategy.sendRedirect(exchange.exchange, URI.create(logoutUri))
                }
        }
        return Mono.empty()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/