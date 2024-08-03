package com.example.authorizationserver.auth.handlers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class CustomSimpleUrlAuthenticationFailureHandler(

) : SimpleUrlAuthenticationFailureHandler() {

    init {
        // set default failure url here
        super.setDefaultFailureUrl("/login?error=true")
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {

        // handle authentication failure
        super.onAuthenticationFailure(request, response, exception)

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
            session.setMaxInactiveInterval(5)

            // retrieve session properties
            val creationTime = session.creationTime.let { Instant.ofEpochMilli(it) }
            val lastAccessedTime = session.lastAccessedTime.let { Instant.ofEpochMilli(it) }
            val timeout = session.maxInactiveInterval

            // calculate expiration time
            val expirationTime = lastAccessedTime.plus(timeout.let { Duration.ofSeconds(it.toLong()) })

            // log session details
            println("SESSION INFORMATION - AFTER FAILED AUTHENTICATION")
            println("Session ID: ${session.id}")
            println("Session Creation Time: $creationTime")
            println("Session Last Accessed Time: $lastAccessedTime")
            println("Session Timeout (seconds): $timeout")
            println("Session Expiration Time: $expirationTime")
            println("-----------")

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