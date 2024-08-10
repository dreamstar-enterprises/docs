package com.example.bff.auth.sessions

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.Session
//import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.web.server.session.SpringSessionWebSessionStore
import org.springframework.web.server.session.CookieWebSessionIdResolver
import org.springframework.web.server.session.DefaultWebSessionManager
import org.springframework.web.server.session.InMemoryWebSessionStore
import org.springframework.web.server.session.WebSessionManager

/**********************************************************************************************************************/
/************************************************** SESSION CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// see more here:
// https://docs.spring.io/spring-session/reference/web-session.html
// https://docs.spring.io/spring-session/reference/web-session.html#websession-how

//@Configuration
//internal class WebSessionStoreConfig {
//
//    @Bean(name = ["webSessionStore"])
//    fun webSessionStore(
//        reactiveRedisIndexedSessionRepository: ReactiveRedisIndexedSessionRepository
//    ): SpringSessionWebSessionStore<ReactiveRedisIndexedSessionRepository.RedisSession> {
//        return SpringSessionWebSessionStore(reactiveRedisIndexedSessionRepository)
//    }
//
//    @Bean(name = ["webSessionManager"])
//    fun webSessionManager(
//        cookieWebSessionIdResolver: CookieWebSessionIdResolver,
//        webSessionStore: SpringSessionWebSessionStore<ReactiveRedisIndexedSessionRepository.RedisSession>
//    ): WebSessionManager {
//        val sessionManager = DefaultWebSessionManager()
//        sessionManager.sessionStore = webSessionStore
//        sessionManager.sessionIdResolver = cookieWebSessionIdResolver
//        return sessionManager
//    }
//}

@Configuration
internal class WebSessionStoreConfig {

    @Bean
    fun reactiveSessionRepository(): ReactiveMapSessionRepository {
        // Use an in-memory map to store sessions
        return ReactiveMapSessionRepository(mutableMapOf())
    }

    @Bean(name = ["webSessionStore"])
    fun webSessionStore(
        sessionRepository: ReactiveMapSessionRepository
    ): SpringSessionWebSessionStore<out Session> {
        return SpringSessionWebSessionStore(sessionRepository)
    }

//    @Bean(name = ["webSessionStore"])
//    fun webSessionStore(): InMemoryWebSessionStore {
//        // Create and return an in-memory session store
//        return InMemoryWebSessionStore()
//    }

//    @Bean(name = ["webSessionManager"])
//    fun webSessionManager(
//        cookieWebSessionIdResolver: CookieWebSessionIdResolver,
//        webSessionStore: SpringSessionWebSessionStore<out Session>
//    ): WebSessionManager {
//        val sessionManager = DefaultWebSessionManager()
//        sessionManager.sessionStore = webSessionStore
//        sessionManager.sessionIdResolver = cookieWebSessionIdResolver
//        return sessionManager
//    }

    @Bean
    fun webSessionManager(): WebSessionManager {
        val sessionManager = DefaultWebSessionManager()
        sessionManager.sessionIdResolver = CookieWebSessionIdResolver()
        return sessionManager
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/