package com.example.authorizationserver.auth.templates

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**********************************************************************************************************************/
/***************************************************** TEMPLATES ******************************************************/
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

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/