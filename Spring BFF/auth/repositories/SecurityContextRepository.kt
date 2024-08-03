package com.example.authorizationserver.auth.repositories

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class SecurityContextConfig() {

    @Bean
    fun customSecurityContextRepository(): SecurityContextRepository {
        return HttpSessionSecurityContextRepository()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/