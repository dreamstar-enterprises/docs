package com.example.authorizationserver.auth.redis

import com.example.authorizationserver.api.enums.RoleTypes
import com.example.authorizationserver.auth.objects.authentication.DocDbUserAuthentication
import com.example.authorizationserver.auth.objects.user.DocDbUser
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.jackson2.SecurityJackson2Modules
import java.io.IOException

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

        // register mixin for DocDbUserAuthentication
        mapper.addMixIn(DocDbUserAuthentication::class.java, DocDbUserAuthenticationMixin::class.java)

        // register modules for security if needed
        mapper.registerModules(SecurityJackson2Modules.getModules(loader))

        // register custom deserializer for DocDbUserAuthentication
        mapper.registerModule(
            SimpleModule().addDeserializer(DocDbUserAuthentication::class.java, DocDbUserAuthenticationDeserializer())
        )

        return mapper
    }

    /*
     * @see
     * org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
     */
    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.loader = classLoader
    }
}

// custom mixin for DocDbUserAuthentication object
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "@class")
abstract class DocDbUserAuthenticationMixin {
    @JsonProperty("docDBUser")
    abstract fun getDocDBUser(): DocDbUser?

    @JsonProperty("password")
    abstract fun getPassword(): String?

    @JsonProperty("authorities")
    abstract fun getAuthorities(): Collection<GrantedAuthority>

    @JsonProperty("isAuthenticated")
    abstract fun isAuthenticated(): Boolean
}

// custom de-serialiser for DocDbUserAuthentication object
private class DocDbUserAuthenticationDeserializer : JsonDeserializer<DocDbUserAuthentication>() {

    val logger: Logger = LogManager.getLogger(
        DocDbUserAuthenticationDeserializer::class.java
    )

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): DocDbUserAuthentication? {

        val node: JsonNode = p.codec.readTree(p)

        // deserialize DocDbUser
        val docDBUserNode = node.get("docDBUser")
        val docDBUser = docDBUserNode?.let { userNode ->
            val userId = userNode.get("userId")?.asText()
            val username = userNode.get("username")?.asText() ?: ""
            val password = userNode.get("password")?.asText()
            val isAccountNonExpired = userNode.get("isAccountNonExpired")?.asBoolean()
            val isAccountNonLocked = userNode.get("isAccountNonLocked")?.asBoolean()
            val isCredentialsNonExpired = userNode.get("isCredentialsNonExpired")?.asBoolean()
            val isEnabled = userNode.get("isEnabled")?.asBoolean()

            // deserialize GrantedAuthority list
            val authorities = userNode.get("authorities")?.mapNotNull { authorityNode ->
                if (authorityNode.isTextual) {
                    authorityNode.asText()?.let { auth -> SimpleGrantedAuthority(auth) }
                } else {
                    // Handle unexpected format (optional)
                    logger.warn("Unexpected format for authority: {}", authorityNode)
                    null
                }
            }?.takeIf { it.isNotEmpty() } ?: listOf(SimpleGrantedAuthority(RoleTypes.EMPTY.name))

            // create DocDbUser instance
            DocDbUser(
                userId,
                username,
                password,
                authorities,
                isAccountNonExpired,
                isAccountNonLocked,
                isCredentialsNonExpired,
                isEnabled
            )
        }

        // deserialize other fields
        val isAuthenticated = node.get("isAuthenticated")?.asBoolean() ?: false

        // create DocDbUserAuthentication instance
        return docDBUser?.let {
            docDBUser.authorities.let { auths ->
                DocDbUserAuthentication(
                    it,
                    docDBUser.password,
                    auths,
                    isAuthenticated)
            }
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/