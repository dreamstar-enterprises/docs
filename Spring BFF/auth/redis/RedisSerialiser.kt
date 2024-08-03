package com.example.authorizationserver.auth.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#serializing-session-using-json

@Configuration
internal class RedisSerialiser : BeanClassLoaderAware {

    private var loader: ClassLoader? = null

    /**
     * Note that the bean name for this bean is intentionally
     * {@code springSessionDefaultRedisSerializer}. It must be named this way to override
     * the default {@link RedisSerializer} used by Spring Session.
     */
    @Bean
    // setting a custom session serialiser for Redis
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
        return object : GenericJackson2JsonRedisSerializer(objectMapper()) {
            override fun serialize(value: Any?): ByteArray {
                println("Serializing: $value")
                return super.serialize(value)
            }

            override fun deserialize(bytes: ByteArray?): Any {
                val result = super.deserialize(bytes)
                println("Deserialized: $result")
                return result
            }
        }
    }

    /**
     * Customized {@link ObjectMapper} to add mix-in for class that doesn't have default
     * constructors.
     * @return the {@link ObjectMapper} to use
     */
    private fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.registerModules(SecurityJackson2Modules.getModules(loader))
        return mapper
    }

    @Bean
    // setting a custom OAuth2AuthorizationRequest serializer for Redis
    fun oauth2AuthorizationRequestRedisSerializer(objectMapper: ObjectMapper): RedisSerializer<OAuth2AuthorizationRequest> {
        return object : RedisSerializer<OAuth2AuthorizationRequest> {
            override fun serialize(t: OAuth2AuthorizationRequest?): ByteArray? {
                println("Serializing OAuth2AuthorizationRequest: $t")
                return t?.let {
                    val bytes = objectMapper.writeValueAsBytes(it)
                    println("Serialized bytes: ${bytes.joinToString(", ") { String.format("%02X", it) }}")
                    bytes
                }
            }

            override fun deserialize(bytes: ByteArray?): OAuth2AuthorizationRequest? {
                println("Deserializing bytes: ${bytes?.joinToString(", ") { String.format("%02X", it) }}")
                return bytes?.let {
                    val result = objectMapper.readValue(it, OAuth2AuthorizationRequest::class.java)
                    println("Deserialized OAuth2AuthorizationRequest: $result")
                    result
                }
            }
        }
    }

    /*
     * @see
     * org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
     */
    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.loader = classLoader
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/