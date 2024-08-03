package com.example.authorizationserver.auth

import org.apache.catalina.Context
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**********************************************************************************************************************/
/*********************************************** SERVLET CONFIGURATION ************************************************/
/**********************************************************************************************************************/

@Configuration
@Profile("ssl") // only active when 'ssl' profile is active
internal class TomcatConfig {
    @Bean
    fun tomcatCustomizer(): TomcatServletWebServerFactory {
        val factory = TomcatServletWebServerFactory()

        // disable SSL
        factory.ssl = null

        // customize session timeout
        factory.addContextCustomizers(TomcatContextCustomizer { context: Context ->
            context.sessionTimeout = 5
        })

        return factory
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/