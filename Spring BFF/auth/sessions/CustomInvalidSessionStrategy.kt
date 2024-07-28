package com.example.authorizationserver.auth.security.sessions

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.session.InvalidSessionStrategy
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/****************************************************** SESSION STRATEGY **********************************************/
/**********************************************************************************************************************/

@Component
internal class CustomInvalidSessionStrategy : InvalidSessionStrategy {

    override fun onInvalidSessionDetected(request: HttpServletRequest, response: HttpServletResponse) {
        // handle session expiry
        handleSessionExpiry(request, response)
    }

    private fun handleSessionExpiry(request: HttpServletRequest, response: HttpServletResponse) {

        // delete cookies
        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == "JSESSIONID") {
                    val deleteCookie = Cookie(cookie.name, null)
                    deleteCookie.path = "/"
                    deleteCookie.maxAge = 0
                    response.addCookie(deleteCookie)
                }
            }
        }

        // clear authentication object
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null) {
            SecurityContextLogoutHandler().logout(request, response, auth)
        }

        // redirect to session expired URL
        response.sendRedirect("/session-expired")
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/