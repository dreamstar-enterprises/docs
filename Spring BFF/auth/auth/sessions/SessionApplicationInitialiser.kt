package com.example.authorizationserver.auth.sessions

import com.example.authorizationserver.auth.connections.RedisConnectionFactoryConfig
import org.springframework.context.annotation.Configuration
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/http-session.html#httpsession-redis-jc
// https://docs.spring.io/spring-session/reference/http-session.html#_servlet_container_initialization_2

@Component
internal class SessionApplicationInitialiser
    : AbstractHttpSessionApplicationInitializer(RedisConnectionFactoryConfig::class.java)

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/