package com.frontiers.bff.auth.repositories.authrequests

import com.frontiers.bff.auth.repositories.securitycontext.RedisSecurityContextRepository
import com.frontiers.bff.auth.serialisers.RedisSerialiserConfig
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.stereotype.Repository
import org.springframework.util.Assert
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * Enables the use of Redis as a persistent storage solution for OAuth2 authorization requests
 */
@Repository
internal class RedisAuthorizationRequestRepository(
    private val redisSerialiserConfig: RedisSerialiserConfig
) : ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val logger = LoggerFactory.getLogger(RedisSecurityContextRepository::class.java)
    private val springAuthorizationRequestAttrName: String = "AUTHORIZATION_REQUEST"

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        exchange: ServerWebExchange
    ): Mono<Void> {
        logger.info("SAVING AUTHORIZATION REQUEST")

        return exchange.session
            .doOnNext { session ->
                if (authorizationRequest?.state == null) {
                    logger.info("Authorization Request State cannot be empty")
                } else {
                    session.attributes[springAuthorizationRequestAttrName] = authorizationRequest
                    logger.info("Authorization Request state: ${authorizationRequest.state}")
                    logger.info("Saved Authorization Request $authorizationRequest in WebSession: $session")
                }
            }.then()
    }

    override fun loadAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest?> {
        logger.info("LOADING AUTHORIZATION REQUEST")
        val state = getStateParameter(exchange) ?: return Mono.empty()
        return exchange.session
            .flatMap { session ->
                val authorizationRequest = getAuthorizationRequest(session)
                if (state == authorizationRequest?.state) {
                    logger.info("Loading authorization request")
                    Mono.just(authorizationRequest)
                } else {
                    logger.warn("State in request does not match Authorization Request state in Session: $session")
                    Mono.empty()
                }
            }
    }

    override fun removeAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest?> {
        logger.info("REMOVING AUTHORIZATION REQUEST")
        val state = getStateParameter(exchange) ?: return Mono.empty()
        return exchange.session
            .flatMap { session ->
                val authorizationRequest = getAuthorizationRequest(session)
                if (state == authorizationRequest?.state) {
                    session.attributes.remove(this.springAuthorizationRequestAttrName)
                    logger.info("Removed authorization request")
                    Mono.just(authorizationRequest)
                } else {
                    logger.info("State in request does not match Authorization Request state in Session: $session")
                    Mono.empty()
                }
            }
    }

    // Helper methods
    private fun getStateParameter(exchange: ServerWebExchange): String? {
        requireNotNull(exchange) { "exchange cannot be null" }
        return exchange.request.queryParams[OAuth2ParameterNames.STATE]?.firstOrNull()
    }

    private fun getAuthorizationRequest(session: WebSession): OAuth2AuthorizationRequest? {
        Assert.notNull(session, "session cannot be null")
        val authRequestAttr = session.getAttribute<Map<String, Any>>(springAuthorizationRequestAttrName)
        if (authRequestAttr != null) {
            // Deserialize from Map to OAuth2AuthorizationRequest
            val map = authRequestAttr as? Map<String, Any>
            try {
                val authorizationRequest = redisSerialiserConfig.redisObjectMapper()
                    .convertValue(map, OAuth2AuthorizationRequest::class.java)
                logger.info("Successfully deserialized Authorization Request: $authorizationRequest")
                return authorizationRequest
            } catch (e: Exception) {
                logger.error("Error deserializing Authorization Request: ${e.message}", e)
                return null
            }
        } else {
            logger.warn("No Authorization Request found in WebSession")
            return null
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/