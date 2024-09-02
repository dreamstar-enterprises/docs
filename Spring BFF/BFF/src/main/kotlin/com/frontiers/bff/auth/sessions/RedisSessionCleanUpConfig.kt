package com.frontiers.bff.auth.sessions

import com.frontiers.bff.props.SpringSessionProperties
import org.slf4j.LoggerFactory
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
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant
import java.util.*
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
@EnableScheduling
internal class SessionEvicter(
    private val redisOperations: ReactiveRedisOperations<String, String>,
    springSessionProperties: SpringSessionProperties,
) {

    private val logger = LoggerFactory.getLogger(SessionEvicter::class.java)

    private val redisKeyLocation = springSessionProperties.redis?.expiredSessionsNamespace
        ?: "spring:session:sessions:expirations"

    data class CleanupContext(
        val now: Instant,
        val pastFiveDays: Instant,
        val range: Range<Double>,
        val limit: Limit
    )

    // run every 120 seconds
    @Scheduled(fixedRate = 120, timeUnit = TimeUnit.SECONDS)
    fun cleanup(): Mono<Void> {
        return Mono.fromCallable {
            val now = Instant.now()
            val pastFiveDays = now.minus(Duration.ofDays(5))
            val range = Range.closed(
                pastFiveDays.toEpochMilli().toDouble(),
                now.toEpochMilli().toDouble()
            )
            val limit = Limit.limit().count(500)
            CleanupContext(now, pastFiveDays, range, limit)
        }
        .doOnNext { context ->
            logger.info("Scheduled cleanup execution started at ${Instant.now()}.")
            logger.info("Current time (now): ${context.now}")
            logger.info("Time range start: ${Date(context.pastFiveDays.toEpochMilli())}")
            logger.info("Time range end: ${Date(context.now.toEpochMilli())}")
            logger.info("Limit count: ${context.limit.count}")
            logger.info("Redis key location: $redisKeyLocation")
        }
        .flatMap { context ->
            val zSetOps = redisOperations.opsForZSet()
            zSetOps.reverseRangeByScore(redisKeyLocation, context.range, context.limit)
                .collectList()
                .flatMap { sessionIdsList ->
                    if (sessionIdsList.isNotEmpty()) {
                        logger.info("Found ${sessionIdsList.size} sessions to remove.")
                        zSetOps.remove(
                            redisKeyLocation,
                            *sessionIdsList.toTypedArray()
                        ).doOnSubscribe { logger.info("Started removal of sessions") }
                            .doOnSuccess { logger.info("Successfully removed sessions") }
                            .doOnError { e -> logger.error("Error during removal: ${e.message}") }
                    } else {
                        logger.info("No sessions found to remove.")
                        Mono.empty()
                    }
                }
        }
        .doOnSuccess {
            logger.info("Scheduled session cleanup check completed at ${Instant.now()}.")
        }
        .doOnError { e ->
            logger.error("Error during session cleanup check: ${e.message}")
        }
        .then()
        .doOnTerminate {
            logger.info("Cleanup process terminated at ${Instant.now()}.")
        }
        .subscribeOn(Schedulers.boundedElastic()) // to ensure proper threading
    }


}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/