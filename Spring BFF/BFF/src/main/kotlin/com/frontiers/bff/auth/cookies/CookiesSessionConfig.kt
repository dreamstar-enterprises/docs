package com.frontiers.bff.auth.cookies

import com.frontiers.bff.props.SessionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.session.CookieWebSessionIdResolver

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#custom-cookie-in-webflux

/**
 * Configures the Session Cookie Properties
 */
@Configuration
internal class CookiesSessionConfig(
    private val sessionProperties: SessionProperties
) {

    @Bean
    fun cookieWebSessionIdResolver(): CookieWebSessionIdResolver {
        return CookieWebSessionIdResolver().apply {
            setCookieName(sessionProperties.SESSION_COOKIE_NAME)
            addCookieInitializer { cookie ->
                cookie.httpOnly(sessionProperties.SESSION_COOKIE_HTTP_ONLY)
                cookie.secure(sessionProperties.SESSION_COOKIE_SECURE)
                cookie.sameSite(sessionProperties.SESSION_COOKIE_SAME_SITE)
                cookie.maxAge(sessionProperties.SESSION_COOKIE_MAX_AGE)
                cookie.path(sessionProperties.SESSION_COOKIE_PATH)
                cookie.domain(sessionProperties.SESSION_COOKIE_DOMAIN)
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/