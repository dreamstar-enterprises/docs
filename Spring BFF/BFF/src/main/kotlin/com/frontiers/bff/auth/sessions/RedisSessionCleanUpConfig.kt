package com.frontiers.bff.auth.sessions

import com.frontiers.bff.props.SpringSessionProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.Limit
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.session.config.ReactiveSessionRepositoryCustomizer
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.config.ConfigureReactiveRedisAction
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
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
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    private val  springSessionProperties: SpringSessionProperties,
) {

    private val redisKeyExpirations = springSessionProperties.redis?.expiredSessionsNamespace
        ?: "spring:session:sessions:expirations"
    private val redisKeyNameSpace = springSessionProperties.redis?.sessionNamespace
        ?: "spring:session:sessions"

    companion object {
        private const val duration : Long = 300
        private const val LOCK_KEY = "session-cleanup-lock"
        private val LOCK_EXPIRY: Duration = Duration.ofSeconds(duration)
        private val logger = LoggerFactory.getLogger(SessionEvicter::class.java)
    }

    data class CleanupContext(
        val now: Instant,
        val pastFiveDays: Instant,
        val range: Range<Double>,
        val limit: Limit
    )

    // run every 300 seconds (5 minutes)
    @Scheduled(fixedRate = duration, timeUnit = TimeUnit.SECONDS)
    fun cleanup() {
        val lockValue = UUID.randomUUID().toString()

        acquireLock(lockValue)
            .flatMap { acquired ->
                if (acquired) {
                    // lock acquired, perform the cleanup task
                    performCleanup()
                    // delete orphaned index keys
                    .then(cleanupOrphanedIndexedKeys())
                    // release lock 10s before duration time
                    .then(Mono.delay(Duration.ofSeconds(duration - 10)))
                    .then(releaseLock(lockValue))
                    .onErrorResume { e ->
                        // handle errors here
                        logger.error("Error during cleanup or lock release", e)
                        Mono.empty()
                    }
                } else {
                    // lock not acquired, skip cleanup
                    Mono.empty()
                }
            }
            .onErrorResume { e ->
                // handle errors here
                logger.error("Error during lock acquisition or cleanup", e)
                Mono.empty()
            }
            .subscribe()
    }

    // acquire lock
    private fun acquireLock(lockValue: String): Mono<Boolean> {

        // key namespace
        val lockNamespace = "${springSessionProperties.redis?.sessionNamespace}:sessions:"
        val fullLockKey = "$lockNamespace$LOCK_KEY"

        logger.info("Attempting to acquire lock with key: $fullLockKey and value: $lockValue")

        val script = """
            return redis.call('set', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2])
        """
        val redisScript = RedisScript.of(script, String::class.java)
        return redisOperations.execute(
            redisScript,
            listOf(fullLockKey),
            listOf(lockValue, LOCK_EXPIRY.seconds.toString())
        )
        .next() // convert Flux<String> to Mono<String>
        .map { result -> result == "OK" }
    }

    // release lock
    private fun releaseLock(lockValue: String): Mono<Boolean> {

        // key namespace
        val lockNamespace = "${springSessionProperties.redis?.sessionNamespace}:sessions:"
        val fullLockKey = "$lockNamespace$LOCK_KEY"

        val script = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
        """
        val redisScript = RedisScript.of(script, Long::class.java)
        return redisOperations.execute(
            redisScript,
            listOf(fullLockKey),
            listOf(lockValue)
        )
            .next() // convert Flux<Long> to Mono<Long>
            .map { result -> result == 1L }
    }

    // clean up sessions from expirations (sorted set)
    private fun performCleanup(): Mono<Void> {
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
            logger.info("Redis key location: $redisKeyExpirations")
        }
        .flatMap { context ->
            val zSetOps = redisOperations.opsForZSet()
            zSetOps.reverseRangeByScore(redisKeyExpirations, context.range, context.limit)
                .collectList()
                .flatMap { sessionIdsList ->
                    if (sessionIdsList.isNotEmpty()) {
                        logger.info("Found ${sessionIdsList.size} sessions to remove.")
                        zSetOps.remove(
                            redisKeyExpirations,
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

    // clean up any orphaned index keys
    fun cleanupOrphanedIndexedKeys(): Mono<Void> {

        // find all indexed keys that match the pattern `namespace:sessions:*:idx`
        val pattern = "$redisKeyNameSpace:sessions:*:idx"
        val scanOptions = ScanOptions.scanOptions().match(pattern).build()

        return redisTemplate.execute { connection ->
            val scanPublisher = connection.keyCommands().scan(scanOptions)

            Flux.from(scanPublisher)
                // process each ByteBuffer to extract the indexed key
                .flatMap { byteBuffer: ByteBuffer ->
                    val indexedKey = decodeByteBuffer(byteBuffer)
                    val sessionId = extractSessionIdFromIndexedKey(indexedKey)
                    val sessionKey = "${springSessionProperties.redis?.sessionNamespace}:sessions:$sessionId"

                    // check if the session key exists
                    redisTemplate.hasKey(sessionKey)
                        .flatMap { exists ->
                            if (!exists) {
                                redisTemplate.opsForSet().members(indexedKey)
                                    .collectList()
                                    .flatMap { members ->
                                        if (members.isNotEmpty()) {
                                            // create a list of removal operations
                                            val removalOps = members.map { member ->
                                                redisTemplate.opsForSet().remove(member.toString(), sessionId)
                                                    .then(Mono.fromRunnable<Void> {
                                                        logger.info("Session ID $sessionId from index set $member has been queued for removal")
                                                    })
                                            }
                                            // return the removal operations
                                            Mono.just(Pair(removalOps, indexedKey))
                                        } else {
                                            Mono.just(Pair(emptyList(), indexedKey))
                                        }
                                    }
                            } else {
                                Mono.just(Pair(emptyList(), null))
                            }
                        }
                }
                .collectList()
                .flatMap { allRemovalOpsAndKeys ->
                    // flatten and execute all removal operations in parallel or in a single batch
                    val (removalOpsList, indexedKeys) = allRemovalOpsAndKeys
                        .fold(Pair(mutableListOf<Mono<Void>>(), mutableListOf<String?>())) { acc, pair ->
                            val (removalOps, indexedKey) = pair
                            acc.first.addAll(removalOps)
                            acc.second.add(indexedKey)
                            acc
                        }

                    // flatten and execute all removal operations in parallel
                    if (removalOpsList.isNotEmpty()) {
                        Flux.merge(removalOpsList)
                            .then(Mono.fromRunnable<Void> {
                                logger.info("All session IDs removed from all principal username indexed keys.")
                            })
                            .then(Mono.defer {
                                // perform a single batch deletion of indexed keys
                                val keysToDelete = indexedKeys.map { it }
                                if (keysToDelete.isNotEmpty()) {
                                    redisTemplate.delete(Flux.fromIterable(keysToDelete))
                                        .doOnSuccess {
                                            logger.info("Deleted orphaned indexed keys: $keysToDelete")
                                        }
                                        .then()
                                } else {
                                    Mono.empty()
                                }
                            })
                    } else {
                        Mono.empty()
                    }
                }
        }.then()

    }

    // function to extract sessionId from the indexed key
    private fun extractSessionIdFromIndexedKey(indexedKey: String): String {
        // indexed key format: namespace:sessions:<sessionId>:idx
        return indexedKey.split(":")[5]  // extract sessionId
    }

    // utility function to decode ByteBuffer to String
    private fun decodeByteBuffer(byteBuffer: ByteBuffer): String {
        return StandardCharsets.UTF_8.decode(byteBuffer).toString()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/