package com.example.bff.auth.repositories

import com.example.bff.auth.serialisers.RedisSerialiserConfig
import com.example.bff.props.SpringSessionProperties
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Repository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * Enables the use of Redis as a persistent storage solution for OAuth2 authorization requests
 */
@Repository
internal class RedisAuthorizationRequestRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    springSessionProperties: SpringSessionProperties,
    private val redisSerialiserConfig: RedisSerialiserConfig
) : ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val redisKeyPrefix = springSessionProperties.redis?.oauth2RequestNameSpace

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        exchange: ServerWebExchange
    ): Mono<Void> {
        return constructRedisKey(exchange).flatMap { redisKey ->
            println("SAVING AUTHORIZATION REQUEST")
            println("Redis Key: $redisKey")

            if (authorizationRequest != null) {

                val hashOperations = redisTemplate.opsForHash<String, Any>()
                val fieldsMap = redisSerialiserConfig.redisObjectMapper().convertValue(
                    authorizationRequest,
                    object : TypeReference<Map<String, Any?>>() {}
                )

                println("Authorization Request: $authorizationRequest")
                println("Fields Map: $fieldsMap")

                hashOperations.putAll(redisKey, fieldsMap).doOnSuccess {
                    println("Successfully saved authorization request to Redis")
                }.doOnError { e ->
                    println("Error saving authorization request to Redis: ${e.message}")
                }.then()
            } else {
                println("Authorization request is null, deleting Redis key: $redisKey")
                redisTemplate.opsForHash<String, Any>().delete(redisKey)
                    .doOnSuccess {
                        println("Successfully deleted authorization request from Redis")
                    }.doOnError { e ->
                        println("Error deleting authorization request from Redis: ${e.message}")
                    }.then()
            }
        }
    }

    override fun loadAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest?> {
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
                            val authorizationRequest = redisSerialiserConfig
                                .redisObjectMapper()
                                .convertValue(map, OAuth2AuthorizationRequest::class.java)
                            println("Deserialized OAuth2AuthorizationRequest: $authorizationRequest")
                            authorizationRequest
                        } catch (e: Exception) {
                            println("Error deserializing OAuth2AuthorizationRequest: ${e.message}")
                            null
                        }
                    }
                }
                .doOnError { e ->
                    println("Error loading authorization request: ${e.message}")
                }
        }
    }

    override fun removeAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest?> {
        println("REMOVING AUTHORIZATION REQUEST")
        return constructRedisKey(exchange).flatMap { redisKey ->
            println("Attempting to remove Authorization Request with key: $redisKey")
            redisTemplate.opsForHash<String, Any>().entries(redisKey)
                .doOnNext { entries ->
                    println("Current Redis Entries: $entries")
                }
                .collectMap({ it.key as String }, { it.value })
                .doOnNext { map ->
                    println("Removing Authorization Request with data: $map")
                }
                .flatMap { map ->
                    redisTemplate.opsForHash<String, Any>().delete(redisKey)
                        .then(Mono.fromCallable {
                            try {
                                val authorizationRequest = redisSerialiserConfig
                                    .redisObjectMapper()
                                    .convertValue(map, OAuth2AuthorizationRequest::class.java)
                                println("Successfully removed Authorization Request from Redis. Data: $authorizationRequest")
                                authorizationRequest
                            } catch (e: Exception) {
                                println("Error deserializing Authorization Request after removal: ${e.message}")
                                null
                            }
                        })
                }
                .onErrorResume { e ->
                    println("Error occurred while removing Authorization Request: ${e.message}")
                    Mono.empty()
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