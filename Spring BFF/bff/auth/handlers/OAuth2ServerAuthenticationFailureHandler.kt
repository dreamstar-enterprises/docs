package com.example.bff.auth.handlers

import com.example.bff.auth.redirects.OAuth2ServerRedirectStrategy
import com.example.bff.props.LoginProperties
import com.example.bff.props.OAuth2RedirectionProperties
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class OAuth2ServerAuthenticationFailureHandler(
    oauth2RedirectionProperties: OAuth2RedirectionProperties,
    private val loginProperties: LoginProperties
) : ServerAuthenticationFailureHandler {

    // construct default re-direct uri
    private val defaultRedirectUri: URI = oauth2RedirectionProperties.getLoginErrorRedirectUri()
        ?: URI.create("/")

    // construct the default redirect strategy
    private val redirectStrategy: OAuth2ServerRedirectStrategy = OAuth2ServerRedirectStrategy(
        oauth2RedirectionProperties.postAuthorizationCode
    )

    // override onSuccess function
    override fun onAuthenticationFailure(
        webFilterExchange: WebFilterExchange,
        exception: AuthenticationException?
    ): Mono<Void>? {
        return webFilterExchange.exchange?.session?.flatMap { session ->
            // retrieve the post-login failure URI from session or use a default URI
            val uri = session.getAttributeOrDefault(
                loginProperties.POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE,
                defaultRedirectUri
            )

            println("SESSION FAILURE ATTRIBUTE SET: ${session.attributes[loginProperties.POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE]}")

            println("ON FAILURE REDIRECT URI: $uri")

            // add query param to uri
            val uriWithQueryParam = UriComponentsBuilder.fromUri(uri)
                .queryParam(
                    loginProperties.POST_AUTHENTICATION_FAILURE_CAUSE_ATTRIBUTE,
                    HtmlUtils.htmlEscape(exception?.message ?: "unknown error")
                ).build().toUri()

            // apply the redirect and return Mono<Void>
            redirectStrategy.sendRedirect(
                webFilterExchange.exchange,
                uriWithQueryParam
            )
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/