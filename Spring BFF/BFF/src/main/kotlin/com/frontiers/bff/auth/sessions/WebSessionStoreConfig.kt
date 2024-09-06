package com.frontiers.bff.auth.sessions

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository.RedisSession
import org.springframework.session.web.server.session.SpringSessionWebSessionStore
import org.springframework.web.server.session.CookieWebSessionIdResolver
import org.springframework.web.server.session.DefaultWebSessionManager
import org.springframework.web.server.session.WebSessionManager

/**********************************************************************************************************************/
/************************************************** SESSION CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// see more here:
// https://docs.spring.io/spring-session/reference/web-session.html
// https://docs.spring.io/spring-session/reference/web-session.html#websession-how

/**
 * Configures the session management setup for a Spring WebFlux application, integrating Spring Session with Redis.
 */
@Configuration
internal class WebSessionStoreConfig {

    /**
     * Adapts ReactiveRedisIndexedSessionRepository (which stores sessions in Redis) to be usable
     * as a WebSessionStore in WebFlux.
     */
    @Bean
    fun webSessionStore(
        reactiveRedisIndexedSessionRepository: ReactiveRedisIndexedSessionRepository
    ): SpringSessionWebSessionStore<ReactiveRedisIndexedSessionRepository.RedisSession> {
        return SpringSessionWebSessionStore(reactiveRedisIndexedSessionRepository)
    }

    /**
     * Configures how sessions are managed in WebFlux, using cookies to store session IDs
     * and Redis to store session data
     */
    @Bean
    fun webSessionManager(
        cookieWebSessionIdResolver: CookieWebSessionIdResolver,
        webSessionStore: SpringSessionWebSessionStore<RedisSession>
    ): WebSessionManager {
        val sessionManager = DefaultWebSessionManager()
        sessionManager.sessionStore = webSessionStore
        sessionManager.sessionIdResolver = cookieWebSessionIdResolver
        return sessionManager
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/