package com.example.bff.auth.handlers

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

