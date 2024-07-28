package com.example.bff.auth.sessions

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry

/**********************************************************************************************************************/
/************************************************** SESSION CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#spring-session-backed-reactive-session-registry
// https://docs.spring.io/spring-session/reference/spring-security.html#spring-security-concurrent-sessions

@Configuration
internal class SessionRegistryConfig {

    @Bean
    fun sessionRegistry(
        reactiveRedisIndexedSessionRepository: ReactiveRedisIndexedSessionRepository,
    ): SpringSessionBackedReactiveSessionRegistry<ReactiveRedisIndexedSessionRepository.RedisSession> {
        return SpringSessionBackedReactiveSessionRegistry(
            reactiveRedisIndexedSessionRepository,
            reactiveRedisIndexedSessionRepository,
        )
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/