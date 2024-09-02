package com.frontiers.bff.auth.sessions

import org.slf4j.LoggerFactory
import org.springframework.session.ReactiveFindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository.RedisSession
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal

/**********************************************************************************************************************/
/************************************************ SESSION CONFIGURATION ***********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#finding-all-user-sessions

/**
 * Service to retrieve all sessions of a particular user, and remove a session of a particular use
 */
@Service
internal class SessionService(
    private val sessions: ReactiveFindByIndexNameSessionRepository<RedisSession>,
    private val redisIndexedSessionRepository: ReactiveRedisIndexedSessionRepository
) {

    private val logger = LoggerFactory.getLogger(SessionService::class.java)

    /**
     * Retrieves all sessions for a specific user.
     * @param principal the principal whose sessions need to be retrieved
     * @return a Flux of sessions for the specified user
     */
    fun getSessions(principal: Principal): Flux<Session> {

        logger.info("Getting all sessions for: ${principal.name}")

        return sessions.findByPrincipalName(principal.name)
            .flatMapMany { sessionsMap ->
                Flux.fromIterable(sessionsMap.values)
            }
    }

    /**
     * Removes a specific session for a user.
     * @param principal the principal whose session needs to be removed
     * @param sessionIdToDelete the ID of the session to be removed
     * @return a Mono indicating completion or error
     */
    fun removeSession(principal: Principal, sessionIdToDelete: String): Mono<Void> {

        logger.info("Removing session for: ${principal.name}, with session id: $sessionIdToDelete")

        return sessions.findByPrincipalName(principal.name)
            .flatMap { userSessions ->
                if (userSessions.containsKey(sessionIdToDelete)) {
                    redisIndexedSessionRepository.deleteById(sessionIdToDelete)
                } else {
                    Mono.empty()
                }
            }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/