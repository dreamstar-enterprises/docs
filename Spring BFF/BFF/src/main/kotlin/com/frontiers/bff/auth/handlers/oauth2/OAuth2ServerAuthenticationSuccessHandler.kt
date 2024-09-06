package com.frontiers.bff.auth.handlers.oauth2

import com.frontiers.bff.auth.redirects.OAuth2ServerRedirectStrategy
import com.frontiers.bff.props.LoginProperties
import com.frontiers.bff.props.OAuth2RedirectionProperties
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/client/SpringAddonsOauth2ServerAuthenticationSuccessHandler.java

/**
 * Provides a customizable and flexible way to handle successful OAuth2 authentication by redirecting users to
 * appropriate URIs based on session data and configuration properties
 */
@Component
internal class OAuth2ServerAuthenticationSuccessHandler(
    oauth2RedirectionProperties: OAuth2RedirectionProperties,
    private val loginProperties: LoginProperties
) : ServerAuthenticationSuccessHandler {

    private val logger = LoggerFactory.getLogger(OAuth2ServerAuthenticationSuccessHandler::class.java)

    // construct default re-direct uri
    private val defaultRedirectUri: URI = oauth2RedirectionProperties.getPostLoginRedirectUri()
        ?: URI.create("/")

    // construct the post authorization redirect strategy
    private val redirectStrategy: OAuth2ServerRedirectStrategy = OAuth2ServerRedirectStrategy(
        oauth2RedirectionProperties.postAuthorizationCode,
        oauth2RedirectionProperties
    )

    // override onSuccess function
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {

        return webFilterExchange.exchange.session.flatMap { session ->

            // retrieve the post-login success URI from session or use a default URI
            val uriString = session.getAttributeOrDefault(
                loginProperties.POST_AUTHENTICATION_SUCCESS_URI_SESSION_ATTRIBUTE,
                defaultRedirectUri.toString()
            )

            logger.info("ON SUCCESSFUL REDIRECT URI: $uriString")

            // apply the redirect and return Mono<Void>
            redirectStrategy.sendRedirect(
                webFilterExchange.exchange,
                URI.create(uriString)
            )
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/