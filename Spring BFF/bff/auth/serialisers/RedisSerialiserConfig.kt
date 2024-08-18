package com.example.bff.auth.serialisers

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.jackson2.CoreJackson2Module
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import java.time.Instant
import java.util.*

/**********************************************************************************************************************/
/**************************************************** SERIALISERS *****************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/reactive-redis-indexed.html

/**
 * User for Serialising to JSON and De-Serialising from JSON, when sending and retrieving from Redis
 */
@Configuration
internal class RedisSerialiserConfig : BeanClassLoaderAware {

    private var loader: ClassLoader? = null

    /**
     * Note that the bean name for this bean is intentionally
     * {@code springSessionDefaultRedisSerializer}. It must be named this way to override
     * the default {@link RedisSerializer} used by Spring Session.
     */
    @Bean
    // setting a custom session serialiser for Redis
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
        return object : GenericJackson2JsonRedisSerializer(redisObjectMapper()) {
            override fun serialize(value: Any?): ByteArray {
                value.let{
                    println("Serializing: $value of type: ${value!!::class.java}")
                }
                println("Serializing: $value")
                return super.serialize(value)
            }

            override fun deserialize(bytes: ByteArray?): Any {
                if (bytes == null || bytes.isEmpty()) {
                    println("Deserialization: Received null or empty byte array")
                    return Any()
                }
                val result = super.deserialize(bytes)
                return result
            }
        }
    }

    /**
     * Customized {@link ObjectMapper} to add mix-in for class that doesn't have default
     * constructors.
     * @return the {@link ObjectMapper} to use
     */
    fun redisObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()

        // Register custom serializers and deserializers
        val module = SimpleModule().apply {
            addDeserializer(
                OAuth2AuthorizationRequest::class.java,
                OAuth2AuthorizationRequestDeserializer()
            )
            addDeserializer(
                OAuth2AuthorizationResponseType::class.java,
                OAuth2AuthorizationResponseTypeDeserializer()
            )
            addDeserializer(
                SecurityContext::class.java,
                SpringSecurityContextDeserializer()
            )
            addDeserializer(
                OAuth2AuthorizedClient::class.java,
                OAuth2AuthorizedClientDeserializer()
            )
        }
        mapper.registerModule(module)

        // register security-related modules
        mapper.registerModule(CoreJackson2Module())
        mapper.registerModules(SecurityJackson2Modules.getModules(this::class.java.classLoader))

        // deactivate default typing if it is enabled
        mapper.deactivateDefaultTyping()

        // additional configurations
        mapper.registerModule(JavaTimeModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return mapper
    }

    /**
     * Define custom de-serialiser for OAuth2AuthorizationRequest
     */
    private class OAuth2AuthorizationRequestDeserializer : JsonDeserializer<OAuth2AuthorizationRequest>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): OAuth2AuthorizationRequest {
            println("Starting deserialization of OAuth2AuthorizationRequest")

            val node = jp.codec.readTree<JsonNode>(jp)

            // Extract values from JSON
            val authorizationUri = node.get("authorizationUri")?.asText() ?: throw IllegalArgumentException("authorizationUri is required")
            val clientId = node.get("clientId")?.asText() ?: throw IllegalArgumentException("clientId is required")
            val redirectUri = node.get("redirectUri")?.asText()
            val state = node.get("state")?.asText()
            val scopes = node.get("scopes")?.elements()?.asSequence()?.map { it.asText() }?.toSet() ?: emptySet()
            val additionalParameters = node.get("additionalParameters")?.fields()?.asSequence()?.associate { it.key to it.value.asText() } ?: emptyMap()
            val attributes = node.get("attributes")?.fields()?.asSequence()?.associate { it.key to it.value.asText() } ?: emptyMap()
            val authorizationRequestUri = node.get("authorizationRequestUri")?.asText()

            // Log extracted values
            println(
                "Extracted values from JSON: " +
                "authorizationUri=$authorizationUri, " +
                "clientId=$clientId, " +
                "redirectUri=$redirectUri, " +
                "state=$state, " +
                "scopes=$scopes, " +
                "additionalParameters=$additionalParameters, " +
                "attributes=$attributes, " +
                "authorizationRequestUri=$authorizationRequestUri"
            )

            // Initialize Builder with extracted values
            val builder = OAuth2AuthorizationRequest
                .authorizationCode() // Assuming the default grant type for the builder
                .authorizationUri(authorizationUri)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .scopes(scopes)
                .state(state)
                .additionalParameters(additionalParameters)
                .attributes(attributes)
                .authorizationRequestUri(authorizationRequestUri ?: "")

            val authorizationRequest = builder.build()

            // Log final deserialized object
            println("Successfully deserialized OAuth2AuthorizationRequest: $authorizationRequest")

            return authorizationRequest
        }

    }


    /**
     * Define custom de-serialiser for OAuth2AuthorizationResponseTypeDeserializer
     */
    private class OAuth2AuthorizationResponseTypeDeserializer : JsonDeserializer<OAuth2AuthorizationResponseType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): OAuth2AuthorizationResponseType {
            val node: JsonNode = p.codec.readTree(p)

            val value = node.get("value")?.asText() ?: throw IllegalArgumentException("Missing value field")
            return OAuth2AuthorizationResponseType(value)
        }
    }


    /**
     * Define custom de-serialiser for Security Context
     */
    private class SpringSecurityContextDeserializer : JsonDeserializer<SecurityContext>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): SecurityContext {
            val node: JsonNode = jp.codec.readTree(jp)

            // extract the authentication node
            val authenticationNode = node.get("authentication")

            // extract principal details
            val principalNode = authenticationNode.get("principal")

            // extract authorities
            val authorities = principalNode.get("authorities")
                .map { authNode ->
                    val authority = authNode.get("authority").asText()
                    if (authNode.has("idToken")) {
                        val idTokenNode = authNode.get("idToken")
                        val idToken = deserializeOidcIdToken(idTokenNode)
                        val userInfo = OidcUserInfo(idToken?.claims ?: emptyMap())
                        OidcUserAuthority(authority, idToken, userInfo)
                    } else {
                        SimpleGrantedAuthority(authority)
                    }
                }.toSet()

            // deserialize the OidcIdToken at the principal level
            val idTokenNode = principalNode.get("idToken")
            val principalIdToken = deserializeOidcIdToken(idTokenNode)

            // create the OidcUser principal
            val nameAttributeKey = principalNode.get("nameAttributeKey").asText()
            val principal = DefaultOidcUser(authorities, principalIdToken, nameAttributeKey)

            // extract the authorizedClientRegistrationId
            val authorizedClientRegistrationId = authenticationNode.get("authorizedClientRegistrationId").asText()

            // create the Authentication object (assumed OAuth2AuthenticationToken)
            val authentication = OAuth2AuthenticationToken(principal, authorities, authorizedClientRegistrationId)

            // create and return the SecurityContextImpl object
            return SecurityContextImpl(authentication)
        }

        private fun deserializeOidcIdToken(idTokenNode: JsonNode?): OidcIdToken? {
            idTokenNode ?: return null

            val tokenValue = idTokenNode.get("tokenValue").asText()
            val issuedAt = Instant.parse(idTokenNode.get("issuedAt").asText())
            val expiresAt = Instant.parse(idTokenNode.get("expiresAt").asText())

            // extract claims
            val claimsNode = idTokenNode.get("claims")
            val claims = claimsNode.fields().asSequence()
                .associate { it.key to it.value.asText() }

            return OidcIdToken(tokenValue, issuedAt, expiresAt, claims)
        }
    }


    /**
     * Define custom de-serialiser for Authorized Client
     */
    private class OAuth2AuthorizedClientDeserializer(): JsonDeserializer<OAuth2AuthorizedClient>() {

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): OAuth2AuthorizedClient {
            println("Starting deserialization of OAuth2AuthorizedClient")

            val node = jp.codec.readTree<JsonNode>(jp)

            // extract 'principalName'
            val principalName = node.get("principalName")?.asText() ?: throw IllegalArgumentException("principalName is required")

            // deserialize 'clientRegistration'
            val clientRegistrationNode = node.get("clientRegistration") ?: throw IllegalArgumentException("clientRegistration is required")
            val clientRegistration = jp.codec.treeToValue(clientRegistrationNode, ClientRegistration::class.java)
                ?: throw IllegalArgumentException("Unable to deserialize clientRegistration")

            // deserialize 'accessToken'
            val accessTokenNode = node.get("accessToken") ?: throw IllegalArgumentException("accessToken is required")
            val tokenTypeValue = accessTokenNode.get("tokenType")?.get("value")?.asText()
                ?: throw IllegalArgumentException("tokenType value is required")
            val tokenType = when (tokenTypeValue.uppercase(Locale.getDefault())) {
                "BEARER" -> OAuth2AccessToken.TokenType.BEARER
                else -> throw IllegalArgumentException("Unknown token type: $tokenTypeValue")
            }
            val accessToken = OAuth2AccessToken(
                tokenType,
                accessTokenNode.get("tokenValue")?.asText() ?: throw IllegalArgumentException("tokenValue is required"),
                accessTokenNode.get("issuedAt")?.asText()?.let { Instant.parse(it) },
                accessTokenNode.get("expiresAt")?.asText()?.let { Instant.parse(it) },
                accessTokenNode.get("scopes")?.elements()?.asSequence()?.map { it.asText() }?.toSet() ?: emptySet()
            )

            // deserialize 'refreshToken'
            val refreshTokenNode = node.get("refreshToken")
            val refreshToken = refreshTokenNode?.let {
                OAuth2RefreshToken(
                    it.get("tokenValue")?.asText() ?: throw IllegalArgumentException("tokenValue is required"),
                    it.get("issuedAt")?.asText()?.let { date -> Instant.parse(date) }
                )
            }

            // log extracted values
            println(
                "Extracted values from JSON: " +
                "principalName=$principalName, " +
                "clientRegistration=$clientRegistration, " +
                "accessToken=$accessToken, " +
                "refreshToken=$refreshToken"
            )

            // return deserialized OAuth2AuthorizedClient object
            return OAuth2AuthorizedClient(
                clientRegistration,
                principalName,
                accessToken,
                refreshToken
            ).also {
                println("Successfully deserialized OAuth2AuthorizedClient: $it")
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