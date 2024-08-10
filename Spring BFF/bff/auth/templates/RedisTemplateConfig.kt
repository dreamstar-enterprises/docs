//package com.example.bff.auth.redis
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.SerializationFeature
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
//import org.springframework.data.redis.core.ReactiveRedisOperations
//import org.springframework.data.redis.core.ReactiveRedisTemplate
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
//import org.springframework.data.redis.serializer.RedisSerializationContext
//import org.springframework.data.redis.serializer.RedisSerializer
//import org.springframework.data.redis.serializer.StringRedisSerializer
//import org.springframework.lang.Nullable
//import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
//
/**********************************************************************************************************************/
/***************************************************** TEMPLATES ******************************************************/
/**********************************************************************************************************************/
//
//@Configuration
//internal class RedisTemplateConfig {
//
//    @Bean
//    fun objectMapper(): ObjectMapper {
//        return ObjectMapper().apply {
//            findAndRegisterModules() // automatically register Jackson modules
//            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false) // avoid errors on empty beans
//        }
//    }
//
//    @Bean
//    // setting a custom session serialiser for Redis
//    fun springSessionDefaultRedisSerializer(objectMapper: ObjectMapper): RedisSerializer<Any> {
//        return object : GenericJackson2JsonRedisSerializer(objectMapper) {
//            override fun serialize(value: Any?): ByteArray {
//                println("Serializing: $value")
//                return super.serialize(value)
//            }
//
//            override fun deserialize(bytes: ByteArray?): Any {
//                val result = super.deserialize(bytes)
//                println("Deserialized: $result")
//                return result
//            }
//        }
//    }
//
//    @Bean
//    // setting a custom OAuth2AuthorizationRequest serializer for Redis
//    fun oauth2AuthorizationRequestRedisSerializer(objectMapper: ObjectMapper): RedisSerializer<OAuth2AuthorizationRequest> {
//        return object : RedisSerializer<OAuth2AuthorizationRequest> {
//            override fun serialize(t: OAuth2AuthorizationRequest?): ByteArray? {
//                println("Serializing OAuth2AuthorizationRequest: $t")
//                return t?.let {
//                    val bytes = objectMapper.writeValueAsBytes(it)
//                    println("Serialized bytes: ${bytes.joinToString(", ") { String.format("%02X", it) }}")
//                    bytes
//                }
//            }
//
//            override fun deserialize(bytes: ByteArray?): OAuth2AuthorizationRequest? {
//                println("Deserializing bytes: ${bytes?.joinToString(", ") { String.format("%02X", it) }}")
//                return bytes?.let {
//                    val result = objectMapper.readValue(it, OAuth2AuthorizationRequest::class.java)
//                    println("Deserialized OAuth2AuthorizationRequest: $result")
//                    result
//                }
//            }
//        }
//    }
//
//    @Bean
//    @Primary
//    // reactive Redis Template for sessions
//    fun reactiveRedisTemplate(
//        connectionFactory: ReactiveRedisConnectionFactory,
//        springSessionDefaultRedisSerializer: RedisSerializer<Any>
//    ): ReactiveRedisTemplate<String, Any> {
//        val serializationContext = RedisSerializationContext.newSerializationContext<String, Any>()
//            .key(StringRedisSerializer())
//            .value(springSessionDefaultRedisSerializer)
//            .hashKey(StringRedisSerializer())
//            .hashValue(springSessionDefaultRedisSerializer)
//            .build()
//        return ReactiveRedisTemplate(connectionFactory, serializationContext)
//    }
//
//
//    @Bean
//    // reactive Redis Operations for sessions
//    fun reactiveRedisOperations(
//        connectionFactory: ReactiveRedisConnectionFactory,
//        springSessionDefaultRedisSerializer: RedisSerializer<Any>
//    ): ReactiveRedisOperations<String, Any> {
//        val serializationContext = RedisSerializationContext.newSerializationContext<String, Any>()
//            .key(StringRedisSerializer())
//            .value(springSessionDefaultRedisSerializer)
//            .hashKey(StringRedisSerializer())
//            .hashValue(springSessionDefaultRedisSerializer)
//            .build()
//
//        return ReactiveRedisTemplate(connectionFactory, serializationContext)
//    }
//    @Bean
//    // reactive Redis Template for oauth2AuthorizationRequests
//    fun oauth2AuthorizationRequestReactiveRedisTemplate(
//        connectionFactory: ReactiveRedisConnectionFactory,
//        oauth2AuthorizationRequestRedisSerializer: RedisSerializer<OAuth2AuthorizationRequest>
//    ): ReactiveRedisTemplate<String, OAuth2AuthorizationRequest> {
//        val serializationContext = RedisSerializationContext.newSerializationContext<String, OAuth2AuthorizationRequest>()
//            .key(StringRedisSerializer())
//            .value(oauth2AuthorizationRequestRedisSerializer)
//            .hashKey(StringRedisSerializer())
//            .hashValue(oauth2AuthorizationRequestRedisSerializer)
//            .build()
//
//        return ReactiveRedisTemplate(connectionFactory, serializationContext)
//    }
//
//}
//
///**********************************************************************************************************************/
///**************************************************** END OF KOTLIN ***************************************************/
///**********************************************************************************************************************/