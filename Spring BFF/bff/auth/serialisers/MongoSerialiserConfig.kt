package com.example.bff.auth.serialisers

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.jackson2.CoreJackson2Module
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
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
internal class MongoSerialiserConfig(
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository
) : BeanClassLoaderAware {

    private var loader: ClassLoader? = null

    /**
     * Customized {@link ObjectMapper} to add mix-in for class that doesn't have default
     * constructors.
     * @return the {@link ObjectMapper} to use
     */
    @Bean
    fun customObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()

        // Register custom serializers and deserializers
        val module = SimpleModule().apply {

            addDeserializer(
                OAuth2AuthorizationRequest::class.java,
                OAuth2AuthorizationRequestDeserializer()
            )

            // No Serialiser for OAuth2AuthorizationRequest (works using the default ObjectMapper!

            addDeserializer(
                OAuth2AuthorizationResponseType::class.java,
                OAuth2AuthorizationResponseTypeDeserializer()
            )

            // No Serialiser for OAuth2AuthorizationResponseType (works using the default ObjectMapper!)

            addDeserializer(
                SecurityContext::class.java,
                SpringSecurityContextDeserializer()
            )

            // No Serialiser for SecurityContext (works using the default ObjectMapper!)

            addDeserializer(
                OAuth2AuthorizedClient::class.java,
                OAuth2AuthorizedClientDeserializer(clientRegistrationRepository)
            )
            addSerializer(
                OAuth2AuthorizedClient::class.java,
                OAuth2AuthorizedClientSerializer()
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

        private val logger = LoggerFactory.getLogger(OAuth2AuthorizationRequestDeserializer::class.java)

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): OAuth2AuthorizationRequest {
            logger.info("Starting deserialization of OAuth2AuthorizationRequest")

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

            return authorizationRequest
        }

    }


    /**
     * Define custom de-serialiser for OAuth2AuthorizationResponseTypeDeserializer
     */
    private class OAuth2AuthorizationResponseTypeDeserializer : JsonDeserializer<OAuth2AuthorizationResponseType>() {

        private val logger = LoggerFactory.getLogger(OAuth2AuthorizationResponseTypeDeserializer::class.java)

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): OAuth2AuthorizationResponseType {
            logger.info("Starting deserialization of OAuth2AuthorizationResponseTypeDeserializer")

            val node: JsonNode = p.codec.readTree(p)
            val value = node.get("value")?.asText() ?: throw IllegalArgumentException("Missing value field")
            return OAuth2AuthorizationResponseType(value)
        }
    }


    /**
     * Define custom de-serialiser for Security Context
     */
    private class SpringSecurityContextDeserializer : JsonDeserializer<SecurityContext>() {

        private val logger = LoggerFactory.getLogger(SpringSecurityContextDeserializer::class.java)

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): SecurityContext {
            logger.info("Starting deserialization of SecurityContext")

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
    private class OAuth2AuthorizedClientDeserializer(
        private val clientRegistrationRepository: ReactiveClientRegistrationRepository
    ): JsonDeserializer<OAuth2AuthorizedClient>() {

        private val logger = LoggerFactory.getLogger(OAuth2AuthorizedClientDeserializer::class.java)

        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): OAuth2AuthorizedClient {
            logger.info("Starting deserialization of OAuth2AuthorizedClient")

            val node = jp.codec.readTree<JsonNode>(jp)

            // extract 'principalName'
            val principalName = node.get("principalName")?.asText() ?: throw IllegalArgumentException("principalName is required")

            // deserialize 'clientRegistration'
            val clientRegistrationNode = node.get("clientRegistration") ?: throw IllegalArgumentException("clientRegistration is required")
            val clientRegistration = jp.codec.treeToValue(clientRegistrationNode, ClientRegistration::class.java)
                ?: throw IllegalArgumentException("Unable to deserialize clientRegistration")

            // retrieve the list of original client registrations which have passwords
            val clientRegistrations = (clientRegistrationRepository as? InMemoryReactiveClientRegistrationRepository)?.toList()
                ?: emptyList()

            // find the original client registration with the same ID
            val originalClientRegistration = clientRegistrations.find {
                it.registrationId == clientRegistration.registrationId
            }

            // add the password back if the original client registration is found
            val updatedClientRegistration = ClientRegistration.withClientRegistration(clientRegistration)
                .clientSecret(originalClientRegistration?.clientSecret ?: clientRegistration.clientSecret)
                .build()

            // deserialize 'accessToken'
            val accessTokenNode = node.get("accessToken") ?: throw IllegalArgumentException("accessToken is required")
            val tokenTypeValue = accessTokenNode.get("tokenType")?.asText()
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

            println("CLIENT SECRET: ${updatedClientRegistration.clientSecret}")

            // return deserialized OAuth2AuthorizedClient object
            return OAuth2AuthorizedClient(
                updatedClientRegistration,
                principalName,
                accessToken,
                refreshToken
            )
        }

    }

    // Custom serializer for OAuth2AuthorizedClient
    private class OAuth2AuthorizedClientSerializer : JsonSerializer<OAuth2AuthorizedClient>() {

        private val logger = LoggerFactory.getLogger(OAuth2AuthorizedClientSerializer::class.java)

        override fun serialize(
            value: OAuth2AuthorizedClient,
            gen: JsonGenerator,
            serializers: SerializerProvider
        ) {
            logger.info("Starting serialisation of OAuth2AuthorizedClient")

            val mapper = gen.codec as ObjectMapper
            val node = mapper.createObjectNode()

            // add @class type information
            node.put("@class", value.javaClass.name)

            // serialize 'clientRegistration'
            val clientRegistrationNode: ObjectNode = mapper.valueToTree(value.clientRegistration)
            node.set<ObjectNode>("clientRegistration", clientRegistrationNode)

            // remove the password field or set it to an empty string!
            clientRegistrationNode.put("clientSecret", "")

            // serialize 'principalName'
            node.put("principalName", value.principalName)

            // serialize 'accessToken'
            val accessToken = value.accessToken
            val accessTokenNode = mapper.createObjectNode()
            accessTokenNode.put("tokenType", accessToken.tokenType.value)
            accessTokenNode.put("tokenValue", accessToken.tokenValue)
            accessTokenNode.put("issuedAt", accessToken.issuedAt?.toString())
            accessTokenNode.put("expiresAt", accessToken.expiresAt?.toString())

            val scopesArrayNode = mapper.createArrayNode()
            accessToken.scopes.forEach { scope ->
                scopesArrayNode.add(scope)
            }
            accessTokenNode.set<ArrayNode>("scopes", scopesArrayNode)

            node.set<ObjectNode>("accessToken", accessTokenNode)

            // serialize 'refreshToken'
            value.refreshToken?.let {
                val refreshTokenNode = mapper.createObjectNode()
                refreshTokenNode.put("tokenValue", it.tokenValue)
                refreshTokenNode.put("issuedAt", it.issuedAt.toString())
                node.set<ObjectNode>("refreshToken", refreshTokenNode)
            }

            // write the JSON node
            gen.writeTree(node)
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