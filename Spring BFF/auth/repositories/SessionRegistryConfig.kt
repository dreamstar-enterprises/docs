package com.example.authorizationserver.auth.repositories

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository.RedisSession
import org.springframework.session.security.SpringSessionBackedSessionRegistry

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/spring-security.html#spring-security-concurrent-sessions

@Configuration
internal class SessionRegistryConfig(
    private val sessionRepository: FindByIndexNameSessionRepository<RedisSession>
) {

    @Bean
    fun sessionRegistry(): SpringSessionBackedSessionRegistry<RedisSession> {
        return SpringSessionBackedSessionRegistry(sessionRepository)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/