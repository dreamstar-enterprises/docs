package com.frontiers.bff.auth.repositories.sessions

import com.frontiers.bff.auth.sessions.CustomSessionIdGenerator
import com.frontiers.bff.props.SpringSessionProperties
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.session.FindByIndexNameSessionRepository
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
internal class RedisIndexedSessionRepositoryConfig (
    private val springSessionProperties: SpringSessionProperties
) {

    private val logger = LoggerFactory.getLogger(RedisIndexedSessionRepositoryConfig::class.java)

    @Bean
    @Primary
    fun reactiveRedisIndexedSessionRepository(
        reactiveRedisOperations: ReactiveRedisOperations<String, Any>,
        reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
        eventPublisher: ApplicationEventPublisher
    ): ReactiveRedisIndexedSessionRepository {
        val repository = ReactiveRedisIndexedSessionRepository(reactiveRedisOperations, reactiveRedisTemplate).apply {
            setDefaultMaxInactiveInterval(Duration.ofSeconds(springSessionProperties.timeout.toLong()))
            setRedisKeyNamespace(springSessionProperties.redis?.sessionNamespace)
            setSessionIdGenerator(CustomSessionIdGenerator())
            setCleanupInterval(Duration.ofSeconds(120))
            setEventPublisher(eventPublisher)
            setSaveMode(SaveMode.ON_SET_ATTRIBUTE)
        }

        repository.setIndexResolver { session ->
            val indexes = mutableMapOf<String, String>()

            // safely handle potential null values
            val principalName = session.getAttribute<String>("principalName")

            // use safe calls or provide default values if necessary
            if (principalName != null) {
                indexes[FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME] = principalName
                logger.info("Indexing principalName: $principalName")
            }

            indexes
        }

        return repository
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/