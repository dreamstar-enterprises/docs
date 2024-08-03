package com.example.authorizationserver.auth.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RedisTemplateConfig {

    @Bean
    @Primary
    //  redis Template for sessions
    fun reactiveRedisTemplate(
        connectionFactory: RedisConnectionFactory,
        springSessionDefaultRedisSerializer: RedisSerializer<Any>
    ): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = springSessionDefaultRedisSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = springSessionDefaultRedisSerializer
        }
    }

    @Bean
    // redis Operations for sessions
    fun reactiveRedisOperations(
        connectionFactory: RedisConnectionFactory,
        springSessionDefaultRedisSerializer: RedisSerializer<Any>
    ): RedisOperations<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = springSessionDefaultRedisSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = springSessionDefaultRedisSerializer
        }
    }

    @Bean
    // redis Template for oauth2AuthorizationRequests
    fun oauth2AuthorizationRequestRedisTemplate(
        connectionFactory: RedisConnectionFactory,
        oauth2AuthorizationRequestRedisSerializer: RedisSerializer<OAuth2AuthorizationRequest>
    ): RedisTemplate<String, OAuth2AuthorizationRequest> {
        return RedisTemplate<String, OAuth2AuthorizationRequest>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = oauth2AuthorizationRequestRedisSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = oauth2AuthorizationRequestRedisSerializer
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/