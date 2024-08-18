package com.example.bff.auth.redis

import com.example.bff.props.SpringSessionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.Limit
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.session.config.ReactiveSessionRepositoryCustomizer
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.config.ConfigureReactiveRedisAction
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/reactive-redis-indexed.html#_configuring_redis_to_send_keyspace_events
// https://docs.spring.io/spring-session/reference/configuration/reactive-redis-indexed.html#how-spring-session-cleans-up-expired-sessions

@Configuration
@EnableScheduling
internal class RedisCleanUpConfig {

    /**
     * No specific configuration or action should be taken regarding Redis keyspace notifications.
     */
    @Bean
    fun configureReactiveRedisAction(): ConfigureReactiveRedisAction {
        return ConfigureReactiveRedisAction.NO_OP
    }

    /**
     * Disables the default clean up task
     */
    @Bean
    fun reactiveSessionRepositoryCustomizer(): ReactiveSessionRepositoryCustomizer<ReactiveRedisIndexedSessionRepository> {
        return ReactiveSessionRepositoryCustomizer { sessionRepository: ReactiveRedisIndexedSessionRepository ->
            sessionRepository.disableCleanupTask()
        }
    }
}

/**
 * For cleanup operations (i.e. removing expired session from a ZSet (Sorted Sets) in Redis)
 * Spring's scheduling mechanism will automatically call the cleanup method according to the schedule
 * defined by the @Scheduled annotation.
 */
@Component
internal class SessionEvicter(
    private val redisOperations: ReactiveRedisOperations<String, String>,
    springSessionProperties: SpringSessionProperties,
) {

    private val redisKeyLocation = springSessionProperties.redis?.expiredSessionsNameSpace
        ?: "spring:session:sessions:expirations"

    // run every 120 seconds
    @Scheduled(fixedRate = 120, timeUnit = TimeUnit.SECONDS)
    fun cleanup(): Mono<Void> {
        val now = Instant.now()
        val pastFiveMinutes = now.minus(Duration.ofMinutes(5))
        val range = Range.closed(
            (pastFiveMinutes.toEpochMilli()).toDouble(),
            (now.toEpochMilli()).toDouble()
        )
        val limit = Limit.limit().count(500)

        // get the ZSet (Sorted Set) operations
        val zSetOps = redisOperations.opsForZSet()

        return zSetOps.reverseRangeByScore(redisKeyLocation, range, limit)
            .collectList()
            .flatMap { sessionIdsList ->
                if (sessionIdsList.isNotEmpty()) {
                    println("Found ${sessionIdsList.size} sessions to remove.")
                    val removal = zSetOps.remove(
                        redisKeyLocation,
                        *sessionIdsList.toTypedArray()
                    )
                    removal
                } else {
                    println("No sessions found to remove.")
                    Mono.empty()
                }
            }
            .doOnSuccess {
                println("Cleanup operation completed.")
            }
            .doOnError { e ->
                println("Error during cleanup operation: ${e.message}")
            }
            .then()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/