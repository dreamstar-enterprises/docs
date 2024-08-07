package com.example.authorizationserver.auth.repositories.sessions

import com.example.authorizationserver.auth.sessions.CustomRedisSessionMapper
import com.example.authorizationserver.auth.sessions.CustomSessionIdGenerator
import com.example.authorizationserver.props.SpringSessionProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.SaveMode
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository.RedisSession
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.stereotype.Repository
import java.time.Duration

// WARNING!!!
// When using RedisIndexedSessionRepository with Redis Cluster you must be aware that it only subscribe
// to events from one random redis node in the cluster, which can cause some session indexes not
// being cleaned up if the event happened in a different node.

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#configuring-redis-session-mapper
// https://docs.spring.io/spring-session/reference/configuration/redis.html#choosing-between-regular-and-indexed

@Configuration
internal class RedisIndexedSessionRepositoryConfig (
    private val springSessionProperties: SpringSessionProperties
) {

    /**
     * Customizes the RedisIndexedSessionRepository to use the CustomRedisSessionMapper.
     */
    @Bean
    fun redisIndexedSessionRepositoryCustomizer(
        redisIndexedSessionRepository: RedisIndexedSessionRepository
    ): SessionRepositoryCustomizer<RedisIndexedSessionRepository> {
        return SessionRepositoryCustomizer { repository ->
            repository.setRedisSessionMapper(CustomRedisSessionMapper(repository))
        }
    }

    /**
     * Session repository configuration for Redis.
     **/
    @Bean
    fun redisIndexedSessionRepository(
        redisOperations: RedisOperations<String, Any>,
        redisTemplate: RedisTemplate<String, Any>,
        eventPublisher: ApplicationEventPublisher
    ):  FindByIndexNameSessionRepository<RedisSession> {
        val repository = RedisIndexedSessionRepository(redisOperations).apply {
            setDefaultMaxInactiveInterval(Duration.ofMinutes(springSessionProperties.timeout.toLong()))
            setRedisKeyNamespace(springSessionProperties.redis?.namespace)
            setSaveMode(SaveMode.ON_SET_ATTRIBUTE)
            setSessionIdGenerator(CustomSessionIdGenerator())
            setApplicationEventPublisher(eventPublisher)
        }

        repository.setIndexResolver { session ->
            val indexes = mutableMapOf<String, String>()

            // safely handle potential null values
            val principalName = session.getAttribute<String>("principalName")
            val role = session.getAttribute<String>("role")

            // use safe calls or provide default values if necessary
            if (principalName != null) {
                indexes["PRINCIPAL_NAME_INDEX_NAME"] = principalName
            }
            if (role != null) {
                indexes["ROLE_INDEX_NAME"] = role
            }

            indexes
        }

        @Suppress("UNCHECKED_CAST")
        return repository as FindByIndexNameSessionRepository<RedisSession>
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/