package com.example.authorizationserver.auth.sessions

import org.springframework.context.annotation.Configuration
import org.springframework.session.MapSession
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.session.data.redis.RedisSessionMapper
import java.util.function.BiFunction

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#configuring-redis-session-mapper

@Configuration
internal class CustomRedisSessionMapper(
    private val sessionRepository: RedisIndexedSessionRepository
): BiFunction<String, Map<String, Any>, MapSession?>{

    private val delegate = RedisSessionMapper()

    /**
     * Custom session mapper that delegates to the default RedisSessionMapper.
     * If an exception occurs, the session is deleted from Redis.
     */
    override fun apply(sessionId: String, map: Map<String, Any>): MapSession? {
        return try {
            delegate.apply(sessionId, map)
        } catch (ex: IllegalStateException) {
            sessionRepository.deleteById(sessionId)
            null
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/