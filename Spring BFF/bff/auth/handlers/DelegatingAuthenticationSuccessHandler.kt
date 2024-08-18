package com.example.bff.auth.handlers

import com.example.bff.auth.handlers.oauth2.OAuth2ServerAuthenticationSuccessHandler
import com.example.bff.auth.handlers.sessions.CustomConcurrentSessionControlSuccessHandler
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

/**
 * Coordinates multiple success handlers to ensure that all necessary actions are taken when authentication succeeds.
 * It allows different authentication success strategies to be applied in sequence.
 */
@Component
internal class DelegatingAuthenticationSuccessHandler(
    private val primaryHandler: CustomConcurrentSessionControlSuccessHandler,
    private val secondaryHandler: OAuth2ServerAuthenticationSuccessHandler
) : ServerAuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {
        // delegate to the primary handler
        return primaryHandler.onAuthenticationSuccess(webFilterExchange, authentication)
            .then(
                // delegate to the secondary handler
                secondaryHandler.onAuthenticationSuccess(webFilterExchange, authentication)
            )
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/