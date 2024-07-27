package com.example.gateway.auth.repositories

import com.example.gateway.auth.sessions.SessionIdGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.session.SaveMode
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import java.time.Duration

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RedisSessionRepository {

    @Value("\${spring.session.redis.namespace}")
    private lateinit var redisNamespace: String

    @Value("\${spring.session.timeout}")
    private var sessionTimeout: Int = 30

    @Bean
    fun reactiveRedisIndexedSessionRepository(
        reactiveRedisOperations: ReactiveRedisOperations<String, Any>,
        reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
        eventPublisher: ApplicationEventPublisher
    ): ReactiveRedisIndexedSessionRepository {
        val repository = ReactiveRedisIndexedSessionRepository(reactiveRedisOperations, reactiveRedisTemplate).apply {
            setDefaultMaxInactiveInterval(Duration.ofMinutes(sessionTimeout.toLong()))
            setRedisKeyNamespace(redisNamespace)
            setSessionIdGenerator(SessionIdGenerator())
            setEventPublisher(eventPublisher)
            setSaveMode(SaveMode.ON_SET_ATTRIBUTE)
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
        return repository
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/