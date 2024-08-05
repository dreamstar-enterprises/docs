package com.example.authorizationserver.auth.handlers

import com.example.authorizationserver.auth.requestcache.CustomRequestCache
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class CustomSavedRequestAwareAuthenticationSuccessHandler(
    customRequestCache: CustomRequestCache
) : SavedRequestAwareAuthenticationSuccessHandler() {

    init {
        // set the custom request cache here
        super.setRequestCache(customRequestCache)
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {

        // handle authentication success
        super.onAuthenticationSuccess(request, response, authentication)

        // manage the session
        this.manageSession(request, response)
    }


    /**
     * Manages the HTTP session, including setting the timeout and logging session properties,
     * that cannot be set in SessionAuthenticationStrategy, (e.g. session time out)
     * @param request The HttpServletRequest containing the session to manage.
     */
    private fun manageSession(
        request: HttpServletRequest?,
        response: HttpServletResponse?
    ) {
        // retrieve the existing session or return if it doesn't exist
        val session: HttpSession? = request?.getSession(false)

        // only proceed if there is a session
        session?.let {

            // set session timeout to 5 seconds (for testing purposes)
            request.getSession()?.setMaxInactiveInterval(5)

            // retrieve session properties
            val creationTime = session.creationTime.let { Instant.ofEpochMilli(it) }
            val lastAccessedTime = session.lastAccessedTime.let { Instant.ofEpochMilli(it) }
            val timeout = session.maxInactiveInterval

            // calculate expiration time
            val expirationTime = lastAccessedTime.plus(timeout.let { Duration.ofSeconds(it.toLong()) })

            // log session details
            println("SESSION INFORMATION - AFTER SUCCESSFUL AUTHENTICATION")
            println("Session ID: ${session.id}")
            println("Session Creation Time: $creationTime")
            println("Session Last Accessed Time: $lastAccessedTime")
            println("Session Timeout (seconds): $timeout")
            println("Session Expiration Time: $expirationTime")

            // log session cookie details
            val sessionCookie = session.servletContext.sessionCookieConfig
            println("SESSION COOKIE INFORMATION")
            println("Cookie Name: ${sessionCookie.name}")
            println("Cookie Path: ${sessionCookie.path}")
            println("Cookie Domain: ${sessionCookie.domain}")
            println("Cookie Max Age: ${sessionCookie.maxAge}")
            println("Cookie Secure: ${sessionCookie.isSecure}")
            println("Cookie HttpOnly: ${sessionCookie.isHttpOnly}")
            println("Cookie Attributes: ${sessionCookie.attributes}")
            println("-----------")

        } ?: run {
            println("No session available.")
        }
    }
}


/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/