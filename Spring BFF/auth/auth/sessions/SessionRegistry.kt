package com.example.authorizationserver.auth.sessions

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.session.FindByIndexNameSessionRepository
//import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository.RedisSession
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/spring-security.html#spring-security-concurrent-sessions

//@Component
//internal class SessionRegistry(
//    sessionRepository: FindByIndexNameSessionRepository<RedisSession>
//) : SpringSessionBackedSessionRegistry<RedisSession>(sessionRepository)

@Configuration
internal class SessionRegistryConfig {

    @Bean
    fun sessionRegistry(): SessionRegistry {
        return SessionRegistryImpl()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/