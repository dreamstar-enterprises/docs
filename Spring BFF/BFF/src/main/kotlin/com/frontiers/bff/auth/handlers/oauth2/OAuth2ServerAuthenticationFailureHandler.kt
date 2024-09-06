package com.frontiers.bff.auth.handlers.oauth2

import com.frontiers.bff.auth.redirects.OAuth2ServerRedirectStrategy
import com.frontiers.bff.props.LoginProperties
import com.frontiers.bff.props.OAuth2RedirectionProperties
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.stereotype.Component
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

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/client/SpringAddonsOauth2ServerAuthenticationFailureHandler.java

/**
 * Provides a comprehensive solution for handling authentication failures in OAuth2 scenarios, offering user
 * feedback through redirection and error messages.
 */
@Component
internal class OAuth2ServerAuthenticationFailureHandler(
    oauth2RedirectionProperties: OAuth2RedirectionProperties,
    private val loginProperties: LoginProperties
) : ServerAuthenticationFailureHandler {

    private val logger = LoggerFactory.getLogger(OAuth2ServerAuthenticationFailureHandler::class.java)

    // construct default re-direct uri
    private val defaultRedirectUri: URI = oauth2RedirectionProperties.getLoginErrorRedirectUri()
        ?: URI.create("/")

    // construct the default redirect strategy
    private val redirectStrategy: OAuth2ServerRedirectStrategy = OAuth2ServerRedirectStrategy(
        oauth2RedirectionProperties.postAuthorizationCode, oauth2RedirectionProperties
    )

    // override onFailure function
    override fun onAuthenticationFailure(
        webFilterExchange: WebFilterExchange,
        exception: AuthenticationException?
    ): Mono<Void>? {

        logger.info("Failed authentication: {}", exception?.message)

        return webFilterExchange.exchange?.session?.flatMap { session ->

            // retrieve the post-login failure URI from session or use a default URI
            val uriString = session.getAttributeOrDefault(
                loginProperties.POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE,
                defaultRedirectUri.toString()
            )

            logger.info("ON FAILURE REDIRECT URI: $uriString")

            // decode the exception message
            val decodedMessageMono = Mono.fromCallable {
                URLDecoder.decode(exception?.message ?: "unknown error", StandardCharsets.UTF_8.name())
            }.subscribeOn(Schedulers.boundedElastic())


            // add query param to uri
            decodedMessageMono.flatMap { decodedMessage ->
                val encodedMessage = UriUtils.encodePath(
                    decodedMessage, StandardCharsets.UTF_8.name()
                )

                // build the URI with the properly encoded query parameter
                val uriWithQueryParam = UriComponentsBuilder.fromUri(URI.create(uriString))
                    .queryParam(
                        loginProperties.POST_AUTHENTICATION_FAILURE_CAUSE_ATTRIBUTE,
                        encodedMessage
                    ).build(true)
                    .toUri()

                logger.info("URI with Query Parameters: $uriWithQueryParam")

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