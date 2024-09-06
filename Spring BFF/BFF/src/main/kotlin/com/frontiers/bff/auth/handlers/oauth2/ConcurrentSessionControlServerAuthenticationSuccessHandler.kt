package com.frontiers.bff.auth.handlers.oauth2

import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.session.ReactiveSessionRegistry
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ConcurrentSessionControlServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.ServerMaximumSessionsExceededHandler
import org.springframework.security.web.server.authentication.SessionLimit
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

/**
 * Configures a session limit of 1, meaning users can only have one active session at a time.
 */
@Component
internal class CustomConcurrentSessionControlSuccessHandler(
    reactiveSessionRegistry: ReactiveSessionRegistry,
    maximumSessionsExceededHandler: ServerMaximumSessionsExceededHandler
) : ServerAuthenticationSuccessHandler {

    private val delegate: ConcurrentSessionControlServerAuthenticationSuccessHandler =
        ConcurrentSessionControlServerAuthenticationSuccessHandler(
            reactiveSessionRegistry,
            maximumSessionsExceededHandler
        )

    init {
        // configure the default session limit
        delegate.setSessionLimit(SessionLimit.of(1))
    }

    // override method
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {

        return delegate.onAuthenticationSuccess(webFilterExchange, authentication)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/

