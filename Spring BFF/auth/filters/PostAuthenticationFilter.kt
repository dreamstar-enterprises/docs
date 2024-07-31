package com.example.authorizationserver.auth.security.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.Instant

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class PostAuthenticationFilter() : OncePerRequestFilter() {

    // main filter logic (don't user 'super' for this!)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        /* session management */
        this.manageSession(request)

        // won't continue filter chain!

    }


    /**
     * Manages the HTTP session, including setting the timeout and logging session properties,
     * that cannot be set in SessionAuthenticationStrategy, (e.g. session time out)
     * @param request The HttpServletRequest containing the session to manage.
     */
    private fun manageSession(request: HttpServletRequest?) {
        // retrieve the existing session or return if it doesn't exist
        val session: HttpSession? = request?.getSession(false)

        // only proceed if there is a session
        session?.let {
            // set session timeout to 5 seconds
            request.getSession()?.setMaxInactiveInterval(5)

            println("SESSION TIME OUT")

            // retrieve session properties
            val creationTime = session.creationTime.let { Instant.ofEpochMilli(it) }
            val lastAccessedTime = session.lastAccessedTime.let { Instant.ofEpochMilli(it) }
            val timeout = session.maxInactiveInterval

            // calculate expiration time
            val expirationTime = lastAccessedTime?.plus(timeout.let { Duration.ofSeconds(it.toLong()) })

            // log for debugging purposes
            println("Session ID: ${session.id}")
            println("Session Creation Time: $creationTime")
            println("Session Last Accessed Time: $lastAccessedTime")
            println("Session Timeout (seconds): $timeout")
            println("Session Expiration Time: $expirationTime")
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/