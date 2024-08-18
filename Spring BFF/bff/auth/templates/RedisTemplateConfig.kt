package com.example.bff.auth.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**********************************************************************************************************************/
/***************************************************** TEMPLATES ******************************************************/
/**********************************************************************************************************************/

/**
 * Spring Template and Operations level abstractions for working with Redis, an external key-value store.
 */
@Configuration
internal class RedisTemplateConfig {

    /**
     * RedisTemplate: This is the primary abstraction for interacting with Redis in Spring. It provides various methods
     * to perform Redis operations, such as setting and getting values, performing transactions, and working with different
     * data structures like strings, lists, sets, and hashes. It is a high-level API that simplifies working with Redis.
     */

    @Bean
    @Primary
    // reactive Redis Template for sessions
    fun reactiveSessionRedisTemplate(
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

    /**
     * RedisOperations: This is a common interface that RedisTemplate implements. It defines the basic operations
     * that can be performed on Redis, such as CRUD (Create, Read, Update, Delete) operations and working with
     * different Redis data structures.
     */
    @Bean
    // reactive Redis Operations for sessions
    fun reactiveSessionRedisOperations(
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