package com.example.bff.auth.repositories.securitycontext

import com.example.bff.auth.serialisers.RedisSerialiserConfig
import com.example.bff.props.SpringSessionProperties
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Repository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * Enables the use of Redis as a persistent storage solution for Spring Security Context objects
 */
@Repository
internal class RedisSecurityContextRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    springSessionProperties: SpringSessionProperties,
    private val redisSerialiserConfig: RedisSerialiserConfig
) : ServerSecurityContextRepository {

    private val redisKeyPrefix = springSessionProperties.redis?.securityContextNameSpace

    override fun save(
        exchange: ServerWebExchange,
        context: SecurityContext
    ): Mono<Void> {

        return constructRedisKey(exchange).flatMap { redisKey ->
            println("SAVING SECURITY CONTEXT")
            println("Redis Key: $redisKey")

            val hashOperations = redisTemplate.opsForHash<String, Any>()
            val fieldsMap = redisSerialiserConfig.redisObjectMapper().convertValue(
                context,
                object : TypeReference<Map<String, Any?>>() {}
            )

            println("Security Context: $context")
            println("Fields Map: $fieldsMap")

            hashOperations.putAll(redisKey, fieldsMap).doOnSuccess {
                println("Successfully saved security cotext to Redis")
            }.doOnError { e ->
                println("Error saving security context to Redis: ${e.message}")
            }.then()
        }
    }

    override fun load(
        exchange: ServerWebExchange
    ): Mono<SecurityContext> {
        return constructRedisKey(exchange).flatMap { redisKey ->
            println("LOADING AUTHORIZATION REQUEST")
            println("Redis Key: $redisKey")

            redisTemplate.opsForHash<String, Any>().entries(redisKey)
                .doOnNext { entries ->
                    println("Redis Entries: $entries")
                }
                .collectMap({ it.key as String }, { it.value })
                .doOnSuccess { map ->
                    println("Loaded Map from Redis: $map")
                }
                .mapNotNull { map ->
                    if (map.isEmpty()) {
                        println("Loaded map is empty, returning null")
                        null
                    } else {
                        try {
                            val securityContext = redisSerialiserConfig
                                .redisObjectMapper()
                                .convertValue(map, SecurityContext::class.java)
                            println("Deserialized SecurityContext: $securityContext")
                            securityContext
                        } catch (e: Exception) {
                            println("Error deserializing SecurityContext: ${e.message}")
                            null
                        }
                    }
                }
                .doOnError { e ->
                    println("Error loading security context: ${e.message}")
                }
        }
    }

    // Helper method to construct the Redis key using a unique identifier from the exchange
    private fun constructRedisKey(exchange: ServerWebExchange): Mono<String> {
        return exchange.session
            .map { it.id }
            .map { "$redisKeyPrefix:$it" }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/