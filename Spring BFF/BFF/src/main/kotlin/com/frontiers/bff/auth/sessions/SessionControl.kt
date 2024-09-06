package com.frontiers.bff.auth.sessions

import com.frontiers.bff.props.SpringSessionProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.web.server.session.SpringSessionWebSessionStore
import org.springframework.stereotype.Component
import org.springframework.web.server.WebSession
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
    private val reactiveSessionRedisOperations: ReactiveRedisOperations<String, Any>,
    private val reactiveSessionRegistry: CustomSpringSessionReactiveSessionRegistry<ReactiveRedisIndexedSessionRepository.RedisSession>,
    private val webSessionStore: SpringSessionWebSessionStore<ReactiveRedisIndexedSessionRepository.RedisSession>,
    private val springSessionProperties: SpringSessionProperties
) {

    private val logger = LoggerFactory.getLogger(SessionControl::class.java)

    fun invalidateSessions(username: String): Mono<Void> {
        return reactiveSessionRegistry.getAllSessions(username)
            .flatMap { session ->
                logger.info("Invalidating sessions of user {}", username)
                // handle the sessions invalidation process
                session.invalidate()
                    .then(webSessionStore.removeSession(session.sessionId))
                    .then(Mono.just(session)) // ensure the session object is returned for logging or further processing if needed
            }
            .then()
            .onErrorResume { e ->
                logger.error("Error invalidating sessions: ${e.message}")
                Mono.empty() // return empty Mono to signify completion even if an error occurred
            }
    }

    fun invalidateSession(session: WebSession): Mono<Void> {
        logger.info("Invalidating sessionId: ${session.id}")
        // handle the session invalidation process
        return session.invalidate()
            .then(
                webSessionStore.removeSession(session.id)
            )
            .doOnSuccess {
                logger.info("Session invalidated and removed: ${session.id}")
            }
            .doOnError { error ->
                logger.error("Error invalidating session: ${session.id}", error)
            }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/