package com.example.gateway.auth.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RedisTemplateConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            findAndRegisterModules() // automatically register Jackson modules
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false) // avoid errors on empty beans
        }
    }

    @Bean
    // setting a custom serialiser for Redis
    fun springSessionDefaultRedisSerializer(objectMapper: ObjectMapper): RedisSerializer<Any> {
        return GenericJackson2JsonRedisSerializer(objectMapper)
    }

    @Bean
    @Primary
    // reactive Redis Template
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
    // reactive Redis Operations
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

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/