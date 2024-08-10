package com.example.bff.auth.handlers

import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

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
                // Delegate to the secondary handler
                secondaryHandler.onAuthenticationSuccess(webFilterExchange, authentication)
            )
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/