package com.example.authorizationserver.auth.cookies

import com.example.authorizationserver.props.SessionProperties
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
internal class CookiesConfig(
    private val sessionProperties: SessionProperties
) {

    @Bean
    fun cookieSerializer(): CookieSerializer {
        val serializer = DefaultCookieSerializer()
        serializer.setCookieName(sessionProperties.SESSION_COOKIE_NAME)
        serializer.setUseHttpOnlyCookie(sessionProperties.SESSION_COOKIE_HTTP_ONLY)
        serializer.setUseSecureCookie(sessionProperties.SESSION_COOKIE_SECURE)
        serializer.setSameSite(sessionProperties.SESSION_COOKIE_SAME_SITE)
        serializer.setCookieMaxAge(sessionProperties.SESSION_COOKIE_MAX_AGE)
        serializer.setCookiePath(sessionProperties.SESSION_COOKIE_PATH)
        return serializer
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/