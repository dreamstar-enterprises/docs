package com.example.authorizationserver.auth.sessions

import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.RedisOperations
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository.RedisSession
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.stereotype.Service
import java.security.Principal

/**********************************************************************************************************************/
/************************************************ SESSION CONFIGURATION ***********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#finding-all-user-sessions

@Service
internal class SessionManagementService(
    private val sessions: FindByIndexNameSessionRepository<RedisSession>
) {

    fun getSessions(principal: Principal): Collection<Session> {
        // Retrieve all sessions for a specific user
        val userSessions = sessions.findByPrincipalName(principal.name).values
        return userSessions
    }

    fun removeSession(principal: Principal, sessionIdToDelete: String) {
        // Retrieve all session IDs for a specific user
        val userSessionIds = sessions.findByPrincipalName(principal.name).keys
        if (userSessionIds.contains(sessionIdToDelete)) {
            // Remove the specific session
            sessions.deleteById(sessionIdToDelete)
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/