package com.example.timesheetapi.auth.security.repositories

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.session.CookieWebSessionIdResolver
import org.springframework.web.server.session.DefaultWebSessionManager
import org.springframework.web.server.session.InMemoryWebSessionStore
import org.springframework.web.server.session.WebSessionManager

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class SessionRegistryConfig() {

    @Bean
    fun webSessionManager(
//        cookieWebSessionIdResolver: CookieWebSessionIdResolver
    ): WebSessionManager {
        val sessionManager = DefaultWebSessionManager()
        sessionManager.sessionStore = InMemoryWebSessionStore()
//        sessionManager.sessionIdResolver = cookieWebSessionIdResolver
        return sessionManager
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/