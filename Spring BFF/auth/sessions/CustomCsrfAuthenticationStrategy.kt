package com.example.authorizationserver.auth.sessions

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/****************************************************** SESSION STRATEGY **********************************************/
/**********************************************************************************************************************/

@Component
class CustomCsrfAuthenticationStrategy(
    csrfTokenRepository: CsrfTokenRepository
) : SessionAuthenticationStrategy {

    private val delegate = CsrfAuthenticationStrategy(csrfTokenRepository)

    override fun onAuthentication(
        authentication: Authentication?,
        request: HttpServletRequest?,
        response: HttpServletResponse?
    ) {

        // delegate to the original CsrfAuthenticationStrategy
        delegate.onAuthentication(authentication, request, response)

    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/