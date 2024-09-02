package com.frontiers.bff.auth.sessions

import com.frontiers.bff.auth.serialisers.RedisSerialiserConfig
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.session.ReactiveSessionInformation
import org.springframework.security.core.session.ReactiveSessionRegistry
import org.springframework.session.ReactiveFindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/************************************************** SESSION CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#spring-session-backed-reactive-session-registry
// https://docs.spring.io/spring-session/reference/spring-security.html#spring-security-concurrent-sessions

/**
 * Implements a custom version of SpringSessionBackedReactiveSessionRegistry<S> registry
 */
@Component
internal class CustomSpringSessionReactiveSessionRegistry<S : Session>(
    private val reactiveRedisIndexedSessionRepository: ReactiveRedisIndexedSessionRepository,
    private val redisSerialiserConfig: RedisSerialiserConfig
) : ReactiveSessionRegistry {

    /* NOTE - NOT REALLY PROPERLY IMPLEMENTED BY SPRING SECURITY - HERE FOR REFERENCE ONLY*/
    private val delegate = SpringSessionBackedReactiveSessionRegistry(
        reactiveRedisIndexedSessionRepository,
        reactiveRedisIndexedSessionRepository
    )

    private val logger = LoggerFactory.getLogger(CustomSpringSessionReactiveSessionRegistry::class.java)

    override fun getAllSessions(principal: Any?): Flux<ReactiveSessionInformation> {
        logger.info("Running getAllSessions logic")
        val authenticationToken = principal?.let { getAuthenticationToken(it) }
        return reactiveRedisIndexedSessionRepository.findByPrincipalName(authenticationToken?.name)
            .flatMapMany { sessionMap -> Flux.fromIterable(sessionMap.entries) }
            .map { entry -> SpringSessionBackedReactiveSessionInformation(entry.value) }
    }

    override fun saveSessionInformation(information: ReactiveSessionInformation): Mono<Void> {
        logger.info("Running saveSessionInformation logic")
        return Mono.empty()
    }

    override fun getSessionInformation(sessionId: String?): Mono<ReactiveSessionInformation> {
        logger.info("Running getSessionInformation logic")
        return reactiveRedisIndexedSessionRepository.findById(sessionId)
            .map { session -> SpringSessionBackedReactiveSessionInformation(session) }
    }

    override fun removeSessionInformation(sessionId: String): Mono<ReactiveSessionInformation> {
        logger.info("Running removeSessionInformation logic")
        return Mono.empty()
    }

    override fun updateLastAccessTime(sessionId: String): Mono<ReactiveSessionInformation> {
        logger.info("Running updateLastAccessTime logic")
        return Mono.empty()
    }

    // get SpringSessionBackedReactiveSessionInformation
    inner class SpringSessionBackedReactiveSessionInformation<S : Session>(
        session: S
    ) : ReactiveSessionInformation(
        ResolvePrincipalName().resolvePrincipalName(session),
        session.id,
        session.lastAccessedTime
    ) {

        override fun invalidate(): Mono<Void> {
            return super.invalidate()
                .then(Mono.defer {
                    reactiveRedisIndexedSessionRepository.deleteById(sessionId)
                })
        }
    }

    // helper class to resolve principalName
    inner class ResolvePrincipalName() {

        private val logger = LoggerFactory.getLogger(ResolvePrincipalName::class.java)
        fun resolvePrincipalName(session: Session): String {
            val principalName: String? = session.getAttribute(ReactiveFindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME)
            if (principalName != null) {
                return principalName
            }
            val contextAttr = session.getAttribute<Map<String, Any>>("SPRING_SECURITY_CONTEXT")
            val map = contextAttr as? Map<String, Any>
            return try {
                val securityContext = redisSerialiserConfig.redisObjectMapper()
                    .convertValue(map, SecurityContext::class.java)
                logger.info("Successfully deserialized SecurityContext: $securityContext")
                securityContext?.authentication?.name ?: ""
            } catch (e: Exception) {
                logger.error("Error deserializing SecurityContext: ${e.message}", e)
                ""
            }
        }
    }

    // get Authentication Token
    private fun getAuthenticationToken(principal: Any): Authentication {
        return object : AbstractAuthenticationToken(AuthorityUtils.NO_AUTHORITIES) {
            override fun getCredentials(): Any? {
                return null
            }
            override fun getPrincipal(): Any {
                return principal
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/