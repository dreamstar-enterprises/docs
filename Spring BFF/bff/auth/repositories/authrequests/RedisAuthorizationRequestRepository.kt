package com.example.bff.auth.repositories

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Repository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Repository
internal class RedisAuthorizationRequestRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, OAuth2AuthorizationRequest>,
) : ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val redisKey = "oauth2:authorization-request"

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        exchange: ServerWebExchange
    ): Mono<Void> {
        return if (authorizationRequest != null) {
            redisTemplate.opsForValue().set(redisKey, authorizationRequest).then()
        } else {
            redisTemplate.opsForValue().delete(redisKey).then()
        }
    }

    override fun loadAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest?> {
        return redisTemplate.opsForValue().get(redisKey)
            .map { it }
    }

    override fun removeAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest?> {
        return redisTemplate.opsForValue().get(redisKey)
            .flatMap { serializedRequest ->
                redisTemplate.opsForValue().delete(redisKey)
                    .then(Mono.just(serializedRequest))
            }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/