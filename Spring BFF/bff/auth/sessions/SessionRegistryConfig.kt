package com.example.bff.auth.sessions

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.session.ReactiveSessionInformation
import org.springframework.security.core.session.ReactiveSessionRegistry
import org.springframework.session.Session
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/************************************************** SESSION CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#spring-session-backed-reactive-session-registry
// https://docs.spring.io/spring-session/reference/spring-security.html#spring-security-concurrent-sessions

@Configuration
internal class SessionRegistryConfig {

    /**
     * Registers a bean for CustomSpringSessionReactiveSessionRegistry
     */
    @Bean
    fun sessionRegistry(
        reactiveRedisIndexedSessionRepository: ReactiveRedisIndexedSessionRepository,
    ): CustomSpringSessionReactiveSessionRegistry<ReactiveRedisIndexedSessionRepository.RedisSession> {

        val delegate = SpringSessionBackedReactiveSessionRegistry(
            reactiveRedisIndexedSessionRepository,
            reactiveRedisIndexedSessionRepository
        )
        return CustomSpringSessionReactiveSessionRegistry(delegate)
    }

}


/**
 * Extends the SpringSessionBackedReactiveSessionRegistry<S> registry
 */
internal class CustomSpringSessionReactiveSessionRegistry<S : Session>(
    private val delegate: SpringSessionBackedReactiveSessionRegistry<S>
) : ReactiveSessionRegistry {

    override fun getAllSessions(principal: Any?): Flux<ReactiveSessionInformation> {
        // Custom logic before delegating
        println("Custom getAllSessions logic")
        return delegate.getAllSessions(principal)
    }

    override fun saveSessionInformation(information: ReactiveSessionInformation): Mono<Void> {
        // Custom logic before delegating
        println("Custom saveSessionInformation logic")
        return delegate.saveSessionInformation(information)
    }

    override fun getSessionInformation(sessionId: String): Mono<ReactiveSessionInformation> {
        // Custom logic before delegating
        println("Custom getSessionInformation logic")
        return delegate.getSessionInformation(sessionId)
    }

    override fun removeSessionInformation(sessionId: String): Mono<ReactiveSessionInformation> {
        // Custom logic before delegating
        println("Custom removeSessionInformation logic")
        return delegate.removeSessionInformation(sessionId)
    }

    override fun updateLastAccessTime(sessionId: String): Mono<ReactiveSessionInformation> {
        // Custom logic before delegating
        println("Custom updateLastAccessTime logic")
        return delegate.updateLastAccessTime(sessionId)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/