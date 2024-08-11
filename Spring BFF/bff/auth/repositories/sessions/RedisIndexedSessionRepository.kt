package com.example.bff.auth.repositories.sessions

import com.example.bff.auth.sessions.CustomSessionIdGenerator
import com.example.bff.props.SpringSessionProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.session.SaveMode
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.stereotype.Repository
import java.time.Duration

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#choosing-between-regular-and-indexed

@Repository
internal class RedisIndexedSessionRepository (
    private val springSessionProperties: SpringSessionProperties
) {

    @Bean
    @Primary
    fun reactiveRedisIndexedSessionRepository(
        reactiveRedisOperations: ReactiveRedisOperations<String, Any>,
        reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
        eventPublisher: ApplicationEventPublisher
    ): ReactiveRedisIndexedSessionRepository {
        val repository = ReactiveRedisIndexedSessionRepository(reactiveRedisOperations, reactiveRedisTemplate).apply {
            setDefaultMaxInactiveInterval(Duration.ofMinutes(springSessionProperties.timeout.toLong()))
            setRedisKeyNamespace(springSessionProperties.redis?.namespace)
            setSessionIdGenerator(CustomSessionIdGenerator())
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