package com.example.bff.auth.repositories.securitycontext

import com.example.bff.auth.BffSecurityIgnoreConfig
import com.example.bff.auth.serialisers.RedisSerialiserConfig
import com.example.bff.props.SpringSessionProperties
import com.fasterxml.jackson.core.type.TypeReference
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
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
    private val uriEndPointFilter: BffSecurityIgnoreConfig,
    private val redisSerialiserConfig: RedisSerialiserConfig
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
        return exchange.session
            .doOnNext { session ->
                println("SAVING SECURITY CONTEXT")
                if (context == null) {
                    session.attributes.remove(springSecurityContextAttrName)
                    logger.info("Removed SecurityContext from WebSession: $session")
                } else {
                    session.attributes[springSecurityContextAttrName] = context
                    logger.info("Saved SecurityContext $context in WebSession: $session")
                }
            }
            .flatMap { session -> session.changeSessionId() }
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        val requestPath = exchange.request.uri.path
        // skip processing for static resources
        if (uriEndPointFilter.shouldSkipSecurityContextLoading(requestPath)) {
            println("Skipping security context loading for static resource: $requestPath")
            return Mono.empty()
        }

        return exchange.session.flatMap { session ->
            println("LOADING SECURITY CONTEXT")
            println("FOR REQUEST PATH: $requestPath")
            val contextAttr = session.getAttribute<Map<String, Any>>(springSecurityContextAttrName)
            if (contextAttr != null) {
                // Deserialize from Map to SecurityContext
                val map = contextAttr as? Map<String, Any>
                try {
                    val securityContext = redisSerialiserConfig.redisObjectMapper()
                        .convertValue(map, SecurityContext::class.java)
                    logger.info("Successfully deserialized SecurityContext: $securityContext")
                    return@flatMap Mono.just(securityContext)
                } catch (e: Exception) {
                    logger.error("Error deserializing SecurityContext: ${e.message}", e)
                }
            }
            logger.info("No SecurityContext found in WebSession: $session")
            Mono.empty()
        }.let { if (cacheSecurityContext) it.cache() else it }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/