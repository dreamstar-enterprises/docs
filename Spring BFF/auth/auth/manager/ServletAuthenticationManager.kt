package com.example.authorizationserver.auth.manager

import com.example.authorizationserver.auth.providers.DocDbAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity

/**********************************************************************************************************************/
/********************************************** AUTHENTICATION MANAGER ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class ServletAuthenticationManagerConfig {

    @Bean
    // authentication manager (adds providers to the manager)
    fun servletAuthenticationManager(
        http: HttpSecurity,
        docDbAuthenticationProvider: DocDbAuthenticationProvider,
    ): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder.authenticationProvider(docDbAuthenticationProvider)
        return authenticationManagerBuilder.build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/