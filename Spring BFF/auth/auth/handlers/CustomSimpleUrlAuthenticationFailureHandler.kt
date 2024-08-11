package com.example.authorizationserver.auth.handlers

import com.example.authorizationserver.props.SessionProperties
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
    private val sessionProperties: SessionProperties
) : SimpleUrlAuthenticationFailureHandler() {

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
            session.setMaxInactiveInterval(sessionProperties.SESSION_MAX_AGE)

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

            val responseCookies = response?.getHeaders("Set-Cookie")
            println("RESPONSE COOKIES INFORMATION")
            responseCookies?.forEach { header ->

                // split the header into parts based on ";"
                val parts = header.split(";").map { it.trim() }

                // extract the cookie name and value from the first part
                val nameValue = parts[0].split("=")
                val cookieName = nameValue[0]
                val cookieValue = nameValue.getOrNull(1) ?: ""

                // initialize variables to store attributes
                var path: String? = null
                var domain: String? = null
                var maxAge: String? = null
                var expires: String? = null
                var secure = false
                var httpOnly = false
                var sameSite: String? = null

                // iterate over the remaining parts to extract attributes
                parts.drop(1).forEach { attribute ->
                    when {
                        attribute.startsWith("Path", ignoreCase = true) -> path = attribute.substringAfter("=")
                        attribute.startsWith("Domain", ignoreCase = true) -> domain = attribute.substringAfter("=")
                        attribute.startsWith("Max-Age", ignoreCase = true) -> maxAge = attribute.substringAfter("=")
                        attribute.startsWith("Expires", ignoreCase = true) -> expires = attribute.substringAfter("=")
                        attribute.startsWith("Secure", ignoreCase = true) -> secure = true
                        attribute.startsWith("HttpOnly", ignoreCase = true) -> httpOnly = true
                        attribute.startsWith("SameSite", ignoreCase = true) -> sameSite = attribute.substringAfter("=")
                    }
                }

                // print cookie information
                println("Cookie Name: $cookieName")
                println("Cookie Value: $cookieValue")
                println("Path: $path")
                println("Domain: $domain")
                println("Max-Age: $maxAge")
                println("Expires: $expires")
                println("Secure: $secure")
                println("HttpOnly: $httpOnly")
                println("SameSite: $sameSite")
                println("-----------")
            }

        } ?: run {
            println("No session available.")
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/