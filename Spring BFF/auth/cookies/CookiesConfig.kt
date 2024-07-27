package com.example.authorizationserver.auth.security.cookies

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class CookiesConfig {

    @Bean
    fun cookieSerializer(): CookieSerializer {
        val serializer = DefaultCookieSerializer()
        serializer.setCookieName("JSESSIONID")
        serializer.setCookiePath("/")
        serializer.setUseHttpOnlyCookie(true)
        serializer.setUseSecureCookie(true)
        serializer.setSameSite("Strict")
        serializer.setCookieMaxAge(5)
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$")
        return serializer
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/