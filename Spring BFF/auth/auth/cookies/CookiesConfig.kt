package com.example.authorizationserver.auth.cookies

import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#customizing-session-cookie

@Configuration
internal class CookiesConfig {

    @Bean
    fun cookieSerializer(): CookieSerializer {
        val serializer = DefaultCookieSerializer()
        serializer.setCookieName("AUTH-SESSIONID-VIA-BEAN")
        serializer.setUseHttpOnlyCookie(true)
        serializer.setUseSecureCookie(false) // scope is not just on secure connections
        serializer.setSameSite("Strict")
        serializer.setCookieMaxAge(5)
        serializer.setCookiePath("/")
        return serializer
    }

    @Bean
    fun sessionCookieConfig(): ServletContextInitializer {
        return ServletContextInitializer { servletContext ->
            val sessionCookieConfig = servletContext.sessionCookieConfig
            sessionCookieConfig.name = "AUTH-SESSIONID"
            sessionCookieConfig.isHttpOnly = true
            sessionCookieConfig.isSecure = false // scope is not just on secure connections
            sessionCookieConfig.maxAge = 5
            sessionCookieConfig.path = "/"
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/