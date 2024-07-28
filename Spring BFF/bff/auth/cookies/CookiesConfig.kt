package com.example.bff.auth.cookies

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
internal class CookiesConfig {

    @Bean
    fun cookieWebSessionIdResolver(): CookieWebSessionIdResolver {
        return CookieWebSessionIdResolver().apply {
            setCookieName("SESSION")
            setCookieMaxAge(Duration.ofMinutes(30))
            addCookieInitializer { cookie ->
                cookie.path("/")
                cookie.httpOnly(true)
                cookie.secure(true)
                cookie.sameSite("Strict")
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/