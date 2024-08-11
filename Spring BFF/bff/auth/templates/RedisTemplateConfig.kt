package com.example.bff.auth.redis

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

/**********************************************************************************************************************/
/***************************************************** TEMPLATES ******************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RedisTemplateConfig {

    @Bean
    // reactive Redis Template for sessions
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory,
        springSessionDefaultRedisSerializer: RedisSerializer<Any>
    ): ReactiveRedisTemplate<String, Any> {
        val serializationContext = RedisSerializationContext.newSerializationContext<String, Any>()
            .key(StringRedisSerializer())
            .value(springSessionDefaultRedisSerializer)
            .hashKey(StringRedisSerializer())
            .hashValue(springSessionDefaultRedisSerializer)
            .build()
        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }

    @Bean
    // reactive Redis Operations for sessions
    fun reactiveRedisOperations(
        connectionFactory: ReactiveRedisConnectionFactory,
        springSessionDefaultRedisSerializer: RedisSerializer<Any>
    ): ReactiveRedisOperations<String, Any> {
        val serializationContext = RedisSerializationContext.newSerializationContext<String, Any>()
            .key(StringRedisSerializer())
            .value(springSessionDefaultRedisSerializer)
            .hashKey(StringRedisSerializer())
            .hashValue(springSessionDefaultRedisSerializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }

    @Bean
    // reactive Redis Template for oauth2AuthorizationRequests
    fun oauth2AuthorizationRequestReactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory,
        oauth2AuthorizationRequestRedisSerializer: RedisSerializer<OAuth2AuthorizationRequest>
    ): ReactiveRedisTemplate<String, OAuth2AuthorizationRequest> {
        val serializationContext = RedisSerializationContext.newSerializationContext<String, OAuth2AuthorizationRequest>()
            .key(StringRedisSerializer())
            .value(oauth2AuthorizationRequestRedisSerializer)
            .hashKey(StringRedisSerializer())
            .hashValue(oauth2AuthorizationRequestRedisSerializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/