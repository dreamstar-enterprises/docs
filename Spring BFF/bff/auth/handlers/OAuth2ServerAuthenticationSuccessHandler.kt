package com.example.bff.auth.handlers

import com.example.bff.auth.redirects.OAuth2ServerRedirectStrategy
import com.example.bff.props.LoginProperties
import com.example.bff.props.OAuth2RedirectionProperties
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class OAuth2ServerAuthenticationSuccessHandler(
    oauth2RedirectionProperties: OAuth2RedirectionProperties,
    private val loginProperties: LoginProperties
) : ServerAuthenticationSuccessHandler {

    // construct default re-direct uri
    private val defaultRedirectUri: URI = oauth2RedirectionProperties.getPostLoginRedirectUri()
        ?: URI.create("/")

    // construct the post authorization redirect strategy
    private val redirectStrategy: OAuth2ServerRedirectStrategy = OAuth2ServerRedirectStrategy(
        oauth2RedirectionProperties.postAuthorizationCode
    )

    // override onSuccess function
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {
        return webFilterExchange.exchange.session.flatMap { session ->

            // retrieve the post-login success URI from session or use a default URI
            val uri = session.getAttributeOrDefault(
                loginProperties.POST_AUTHENTICATION_SUCCESS_URI_SESSION_ATTRIBUTE,
                defaultRedirectUri
            )

            println("ON SUCCESS REDIRECT URI: $uri")

            // apply the redirect and return Mono<Void>
            redirectStrategy.sendRedirect(
                webFilterExchange.exchange,
                uri
            )
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/