package com.frontiers.bff.auth.repositories.authclients

import com.frontiers.bff.auth.serialisers.RedisSerialiserConfig
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.stereotype.Repository
import org.springframework.util.Assert
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * Enables the use of Redis as a persistent storage solution for Authorized Client objects
 */
@Repository
internal class RedisServerOAuth2AuthorizedClientRepository(
    private val redisSerialiserConfig: RedisSerialiserConfig
) : ServerOAuth2AuthorizedClientRepository {

    private val logger = LoggerFactory.getLogger(RedisServerOAuth2AuthorizedClientRepository::class.java)
    private val springAuthorizedClientAttrName: String = "AUTHORIZED_CLIENTS"

    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(
        clientRegistrationId: String,
        principal: Authentication,
        exchange: ServerWebExchange
    ): Mono<T> {
        logger.info("LOADING AUTHORIZED CLIENT - REPOSITORY")
        logger.info("Loading authorized client for clientRegistrationId: ${clientRegistrationId}")
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty")
        Assert.notNull(exchange, "exchange cannot be null")

        return exchange.session
            .mapNotNull { getAuthorizedClients(it) }
            .flatMap { clients ->
                @Suppress("UNCHECKED_CAST")
                val client = clients?.get(clientRegistrationId) as? T
                if (client == null) {
                    logger.warn("No authorized client found for clientRegistrationId: $clientRegistrationId")
                }
                Mono.justOrEmpty(client)
            }
    }

    override fun saveAuthorizedClient(
        authorizedClient: OAuth2AuthorizedClient,
        principal: Authentication,
        exchange: ServerWebExchange
    ): Mono<Void> {
        logger.info("SAVING AUTHORIZED CLIENT - REPOSITORY")
        logger.info("Saving authorized client for clientRegistrationId: ${authorizedClient.clientRegistration.registrationId}")
        Assert.notNull(authorizedClient, "authorizedClient cannot be null")
        Assert.notNull(exchange, "exchange cannot be null")

        return exchange.session
            .doOnSuccess { session ->
                val authorizedClients = getAuthorizedClients(session) ?: mutableMapOf()
                authorizedClients[authorizedClient.clientRegistration.registrationId] = authorizedClient
                session.attributes[springAuthorizedClientAttrName] = authorizedClients
            }
            .then(Mono.empty())
    }

    override fun removeAuthorizedClient(
        clientRegistrationId: String,
        principal: Authentication,
        exchange: ServerWebExchange
    ): Mono<Void> {
        logger.info("REMOVING AUTHORIZED CLIENT - REPOSITORY")
        logger.info("Removing authorized client for clientRegistrationId: $clientRegistrationId")
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty")
        Assert.notNull(exchange, "exchange cannot be null")

        return exchange.session
            .doOnSuccess { session ->
                val authorizedClients = getAuthorizedClients(session)
                authorizedClients?.remove(clientRegistrationId)
                if (authorizedClients?.isEmpty() == true) {
                    session.attributes.remove(springAuthorizedClientAttrName)
                } else {
                    session.attributes[springAuthorizedClientAttrName] = authorizedClients
                }
            }
            .then(Mono.empty())
    }

    // Helper function to get authorized clients
    private fun getAuthorizedClients(session: WebSession): MutableMap<String, OAuth2AuthorizedClient>? {
        Assert.notNull(session, "session cannot be null")

        val authorizedClientAttr = session.getAttribute<Map<String, Map<String, Any>>>(springAuthorizedClientAttrName)
        val authorizedClients: MutableMap<String, OAuth2AuthorizedClient> = mutableMapOf()
        if (authorizedClientAttr != null) {
            try {
                // iterate over each entry in the map and deserialize it to OAuth2AuthorizedClient
                for ((key, value) in authorizedClientAttr) {
                    val authorizedClient = redisSerialiserConfig.redisObjectMapper()
                        .convertValue(value, OAuth2AuthorizedClient::class.java)
                    logger.info("Successfully deserialized OAuth2AuthorizedClient for key: $key")
                    authorizedClients[key] = authorizedClient
                }
                return authorizedClients
            } catch (e: Exception) {
                logger.error("Error deserializing OAuth2AuthorizedClient: ${e.message}", e)
                return null
            }
        } else {
            logger.warn("No AuthorizedClients Map found in WebSession")
            return null
        }

    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/