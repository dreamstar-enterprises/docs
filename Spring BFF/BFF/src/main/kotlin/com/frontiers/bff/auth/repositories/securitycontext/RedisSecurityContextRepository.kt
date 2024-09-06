package com.frontiers.bff.auth.repositories.securitycontext

import com.frontiers.bff.auth.serialisers.RedisSerialiserConfig
import com.frontiers.bff.routing.filters.IgnoreFilter
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Repository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * Enables the use of Redis as a persistent storage solution for Spring Security Context objects
 */
@Repository
internal class RedisSecurityContextRepository(
    private val uriEndPointFilter: IgnoreFilter,
    private val redisSerialiserConfig: RedisSerialiserConfig,
) : ServerSecurityContextRepository {

    private val logger = LoggerFactory.getLogger(RedisSecurityContextRepository::class.java)

    // default session attribute name to save and load the SecurityContext
    private var springSecurityContextAttrName = "SPRING_SECURITY_CONTEXT"

    // flag to cache the SecurityContext to avoid multiple lookups
    private var cacheSecurityContext: Boolean = true

    fun setCacheSecurityContext(cache: Boolean) {
        this.cacheSecurityContext = cache
    }

    override fun save(
        exchange: ServerWebExchange,
        context: SecurityContext?
    ): Mono<Void> {
        return exchange.session.flatMap { session ->
            logger.info("SAVING SECURITY CONTEXT")
            if (context == null) {
                session.attributes.remove(springSecurityContextAttrName)
                logger.info("Removed SecurityContext from WebSession: $session")
            } else {
                session.attributes[springSecurityContextAttrName] = context

                // extract principalName and roles from the SecurityContext
                val authentication = context.authentication
                val principalName = authentication.name

                // save them as session attributes (needed for indexing)
                session.attributes["principalName"] = principalName

                logger.info("Saved SecurityContext $context in WebSession: $session")
                logger.info("Set session attributes principalName=$principalName")
            }
            session.changeSessionId()
        }

    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        val requestPath = exchange.request.uri.path
        // skip processing for certain defined request paths
        if (uriEndPointFilter.shouldSkipRequestPath(requestPath)) {
            logger.info("Skipping security context loading for static resource: $requestPath")
            return Mono.empty()
        }

        return exchange.session.flatMap { session ->
            logger.info("LOADING SECURITY CONTEXT")
            logger.info("FOR REQUEST PATH: $requestPath")

            // attempt to retrieve the security context from the session
            val contextAttr = session.getAttribute<Map<String, Any>>(springSecurityContextAttrName)
            if (contextAttr != null) {
                try {
                    // deserialize from Map to SecurityContext
                    val securityContext = redisSerialiserConfig.redisObjectMapper()
                        .convertValue(contextAttr, SecurityContext::class.java)
                    logger.info("Successfully deserialized SecurityContext: $securityContext")
                    return@flatMap Mono.just(securityContext)
                } catch (e: Exception) {
                    logger.error("Error deserializing SecurityContext: ${e.message}", e)
                }
            }

            logger.warn("No SecurityContext found in WebSession: $session")
            Mono.empty()
        }.let { if (cacheSecurityContext) it.cache() else it }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/