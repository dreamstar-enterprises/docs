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
import org.springframework.web.util.UriUtils
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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

            println("ON FAILURE REDIRECT URI: $uri")

            // decode the exception message
            val decodedMessageMono = Mono.fromCallable {
                URLDecoder.decode(exception?.message ?: "unknown error", StandardCharsets.UTF_8.name())
            }.subscribeOn(Schedulers.boundedElastic())


            // add query param to uri
            decodedMessageMono.flatMap { decodedMessage ->
                val encodedMessage = UriUtils.encodePath(
                    decodedMessage, StandardCharsets.UTF_8.name()
                )

                // Build the URI with the properly encoded query parameter
                val uriWithQueryParam = UriComponentsBuilder.fromUri(uri)
                    .queryParam(
                        loginProperties.POST_AUTHENTICATION_FAILURE_CAUSE_ATTRIBUTE,
                        encodedMessage
                    ).build(true)
                    .toUri()

                println("URI with Query Parameters: $uriWithQueryParam")

                // Apply the redirect and return Mono<Void>
                redirectStrategy.sendRedirect(
                    webFilterExchange.exchange,
                    uriWithQueryParam
                )
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/