package com.example.bff.auth.sessions

import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry
import org.springframework.session.web.server.session.SpringSessionWebSessionStore
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/************************************************** SESSION CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// see more here:
// https://docs.spring.io/spring-security/reference/reactive/authentication/concurrent-sessions-control.html#reactive-concurrent-sessions-control-manually-invalidating-sessions

/**
 * Session control class for invalidation session(s) and then removing them
 */
@Component
internal class SessionControl(
    private val reactiveSessionRegistry: CustomSpringSessionReactiveSessionRegistry<ReactiveRedisIndexedSessionRepository.RedisSession>,
    private val reactiveRedisIndexedSessionRepository: ReactiveRedisIndexedSessionRepository,
    private val webSessionStore: SpringSessionWebSessionStore<ReactiveRedisIndexedSessionRepository.RedisSession>
) {

    fun invalidateSessions(username: String): Mono<Void> {
        return reactiveSessionRegistry.getAllSessions(username)
            .flatMap { session ->
                session.invalidate() // invalidate each session
                    .then(webSessionStore.removeSession(session.sessionId)) // remove each from WebSessionStore
                    .then(Mono.just(session)) // ensure the session object is returned for logging or further processing if needed
            }
            .then()
            .onErrorResume { e ->
                // handle errors, e.g., logging
                println("Error invalidating sessions: ${e.message}")
                Mono.empty() // return empty Mono to signify completion even if an error occurred
            }
    }

    fun invalidateSession(sessionId: String): Mono<Void> {
        return reactiveSessionRegistry.getSessionInformation(sessionId)
            .flatMap { session ->
                // handle the session invalidation process
                session.invalidate() // invalidate the session
                    .then(webSessionStore.removeSession(sessionId)) // remove from WebSessionStore

            }
            .then()
            .onErrorResume { e ->
                // handle errors, e.g., logging
                println("Error invalidating session with ID $sessionId: ${e.message}")
                Mono.empty() // return empty Mono to signify completion even if an error occurred
            }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/