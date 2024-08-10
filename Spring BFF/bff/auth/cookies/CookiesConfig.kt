package com.example.bff.auth.cookies

import com.example.bff.props.SessionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.session.CookieWebSessionIdResolver
import java.time.Duration

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#custom-cookie-in-webflux

@Configuration
internal class CookiesConfig(
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
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/