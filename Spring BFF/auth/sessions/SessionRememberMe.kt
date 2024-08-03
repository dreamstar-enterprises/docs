package com.example.authorizationserver.auth.sessions

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class SessionRememberMeConfig() {

    @Bean
    fun rememberMeServices(): SpringSessionRememberMeServices {
        val rememberMeServices =
            SpringSessionRememberMeServices()

        // optionally customize
        rememberMeServices.setAlwaysRemember(true)
        rememberMeServices.setRememberMeParameterName("remember-me")
        rememberMeServices.setValiditySeconds(57600) // set to 16 hours

        return rememberMeServices
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/
