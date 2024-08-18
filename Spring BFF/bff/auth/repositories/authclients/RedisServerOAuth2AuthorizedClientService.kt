package com.example.bff.auth.repositories.authclients

import com.example.bff.auth.serialisers.RedisSerialiserConfig
import com.example.bff.props.SpringSessionProperties
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * Enables the use of Redis as a persistent storage solution for Authorized Client objects
 */
@Service
internal class RedisReactiveOAuth2AuthorizedClientService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    springSessionProperties: SpringSessionProperties,
    private val redisSerialiserConfig: RedisSerialiserConfig
) : ReactiveOAuth2AuthorizedClientService {

    private val redisKeyPrefix = springSessionProperties.redis?.authorizedClientNameSpace

    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(
        clientRegistrationId: String,
        principalName: String,
    ): Mono<T> {
        return constructRedisKey(clientRegistrationId, principalName).flatMap { redisKey ->
            println("LOADING AUTHORIZED CLIENT - SERVICE")
            println("Redis Key: $redisKey")

            redisTemplate.opsForHash<String, Any>().entries(redisKey)
                .doOnNext { entries ->
                    //
                }
                .collectMap({ it.key as String }, { it.value })
                .doOnSuccess { map ->
                    println("Loaded Map from Redis")
                }
                .mapNotNull { map ->
                    if (map.isEmpty()) {
                        println("Loaded map is empty, returning null")
                        null
                    } else {
                        try {
                            val authorizedClient = redisSerialiserConfig
                                .redisObjectMapper()
                                .convertValue(map, OAuth2AuthorizedClient::class.java) as T
                            println("Deserialized Authorized Client: $authorizedClient")
                            authorizedClient
                        } catch (e: Exception) {
                            println("Error deserializing Authorized Client: ${e.message}")
                            null
                        }
                    }
                }
                .doOnError { e ->
                    println("Error loading authorized client: ${e.message}")
                }
        }
    }

    override fun saveAuthorizedClient(
        authorizedClient: OAuth2AuthorizedClient,
        principal: Authentication
    ): Mono<Void> {

        val clientRegistrationId = authorizedClient.clientRegistration.registrationId

        return constructRedisKey(clientRegistrationId, principal.name).flatMap { redisKey ->
            println("SAVING AUTHORIZED CLIENT - SERVICE")
            println("Redis Key: $redisKey")

            val fieldsMap = redisSerialiserConfig.redisObjectMapper().convertValue(
                authorizedClient,
                object : TypeReference<Map<String, Any?>>() {}
            )

            // remove the clientSecret from the fieldsMap if present
            @Suppress("UNCHECKED_CAST")
            (fieldsMap["clientRegistration"] as? MutableMap<String, Any?>)?.apply {
                if (this.containsKey("clientSecret")) {
                    this["clientSecret"] = ""
                    println("Client secret set to empty string in clientRegistration map.")
                } else {
                    println("No client secret found in clientRegistration map.")
                }
            }

            redisTemplate.opsForHash<String, Any>().putAll(redisKey, fieldsMap)
                .doOnSuccess {
                    println("Successfully saved authorized client to Redis")
                }
                .doOnError { e ->
                    println("Error saving authorized client to Redis: ${e.message}")
                }
                .then()
        }
    }

    override fun removeAuthorizedClient(
        clientRegistrationId: String,
        principalName: String
    ): Mono<Void> {
        return constructRedisKey(clientRegistrationId, principalName).flatMap { redisKey ->
            println("REMOVING AUTHORIZED CLIENT - SERVICE")
            println("Redis Key: $redisKey")

            redisTemplate.opsForHash<String, Any>().delete(redisKey)
                .doOnSuccess {
                    println("Successfully removed authorized client from Redis")
                }
                .doOnError { e ->
                    println("Error removing authorized client from Redis: ${e.message}")
                }
                .then()
        }
    }

    private fun constructRedisKey(clientRegistrationId: String, principalName: String): Mono<String> {
        return Mono.just("$redisKeyPrefix:$clientRegistrationId:$principalName")
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/