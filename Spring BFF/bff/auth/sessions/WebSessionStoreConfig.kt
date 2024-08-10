//package com.example.bff.auth.sessions
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
//import org.springframework.session.web.server.session.SpringSessionWebSessionStore
//import org.springframework.web.server.session.CookieWebSessionIdResolver
//import org.springframework.web.server.session.DefaultWebSessionManager
//import org.springframework.web.server.session.WebSessionManager
//
///**********************************************************************************************************************/
///************************************************** SESSION CONFIGURATION *********************************************/
///**********************************************************************************************************************/
//
//// see more here:
//// https://docs.spring.io/spring-session/reference/web-session.html
//// https://docs.spring.io/spring-session/reference/web-session.html#websession-how
//
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
//
///**********************************************************************************************************************/
///**************************************************** END OF KOTLIN ***************************************************/
///**********************************************************************************************************************/