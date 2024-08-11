package com.example.bff.auth.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.Limit
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.session.config.ReactiveSessionRepositoryCustomizer
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.config.ConfigureReactiveRedisAction
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/reactive-redis-indexed.html

@Configuration
internal class RedisCleanUpConfig {

    @Bean
    // configuring Redis to Send Keyspace Events
    fun configureReactiveRedisAction(): ConfigureReactiveRedisAction {
        return ConfigureReactiveRedisAction.NO_OP
    }

    @Bean
    // configuring frequency of session clean-ups
    fun reactiveSessionRepositoryCustomizer(): ReactiveSessionRepositoryCustomizer<ReactiveRedisIndexedSessionRepository> {
        return ReactiveSessionRepositoryCustomizer { sessionRepository: ReactiveRedisIndexedSessionRepository ->
            sessionRepository.setCleanupInterval(
                Duration.ofSeconds(30)
            )
        }
    }
}


@Component
// for custom cleanup operations (e.g. removing session IDs from a ZSet in Redis)
internal class SessionEvicter(private val redisOperations: ReactiveRedisOperations<String, String>) {

    // run every 60 seconds
    @Scheduled(fixedRate = 60000)
    fun cleanup(): Mono<Void> {
        val now = Instant.now()
        val oneMinuteAgo = now.minus(Duration.ofMinutes(1))
        val range = Range.closed(
            (oneMinuteAgo.toEpochMilli()).toDouble(),
            (now.toEpochMilli()).toDouble()
        )
        val limit = Limit.limit().count(1000)

        // get the ZSet operations
        val zSetOps = redisOperations.opsForZSet()

        return zSetOps.reverseRangeByScore("spring:session:sessions:expirations", range, limit)
            .collectList()
            .flatMap { sessionIdsList ->
                if (sessionIdsList.isNotEmpty()) {
                    val removal = zSetOps.remove(
                        "spring:session:sessions:expirations",
                        *sessionIdsList.toTypedArray()
                    )
                    removal
                } else {
                    Mono.empty()
                }
            }
            .then()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/