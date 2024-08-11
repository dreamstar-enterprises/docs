package com.example.authorizationserver.auth.sessions

import com.example.authorizationserver.props.SessionProperties
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.session.InvalidSessionStrategy
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/*************************************************** INVALIDATION STRATEGY ********************************************/
/**********************************************************************************************************************/

@Configuration
internal class CustomInvalidSessionStrategy(
    private val sessionProperties: SessionProperties
) : InvalidSessionStrategy {

    override fun onInvalidSessionDetected(request: HttpServletRequest, response: HttpServletResponse) {
        // handle session expiry
        handleSessionExpiry(request, response)
    }

    private fun handleSessionExpiry(request: HttpServletRequest, response: HttpServletResponse) {

        // clear authentication object
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null) {
            SecurityContextLogoutHandler().logout(request, response, auth)
        }

        // retrieve the existing session or return if it doesn't exist
        val session: HttpSession? = request.getSession(false)

        // delete cookies
        val sessionCookieConfig = session?.servletContext?.sessionCookieConfig

        // create a new cookie with the same name as the session cookie but with maxAge set to 0
        val deleteCookie = Cookie(
            sessionCookieConfig?.name
                    ?: sessionProperties.SESSION_COOKIE_NAME,
            null).apply {
            isHttpOnly = sessionCookieConfig?.isHttpOnly
                ?: sessionProperties.SESSION_COOKIE_HTTP_ONLY
            maxAge = 0 // session age is 0!
            secure = sessionCookieConfig?.isSecure
                ?: sessionProperties.SESSION_COOKIE_SECURE
            path = sessionCookieConfig?.path
                ?: sessionProperties.SESSION_COOKIE_PATH
        }

        // add the cookie to the response to delete it
        response.addCookie(deleteCookie)

        // redirect to session expired URL
        response.sendRedirect(sessionProperties.REDIRECT_URL)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/