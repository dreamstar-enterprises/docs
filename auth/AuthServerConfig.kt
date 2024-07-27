package com.example.authorizationserver.auth.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.token.*
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import java.util.*

/**********************************************************************************************************************/
/********************************************* AUTHORIZATION SERVER CONFIGURATION *************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebSecurity
internal class AuthServerConfig () {

    @Bean
    @Order(1)
    @Throws(Exception::class)
    /* security filter chain for protocol endpoints */
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {

        // apply default http security settings to oauth 2.0 (e.g. default endpoints)
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)

        // enable OpenID Connect 1.0
        val authorizationServerConfigurer = http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
        authorizationServerConfigurer.oidc(withDefaults())

        // redirect to the login page when not authenticated
        http
            // configure cors
            .cors { cors ->
                cors.configurationSource {
                    CorsConfiguration().apply {
                        // ensure this matches your Angular app URL
                        allowedOrigins = listOf("http://localhost:4200")
                        allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        allowedHeaders = listOf("Content-Type", "Authorization", "X-XSRF-TOKEN")
                        exposedHeaders = listOf("Content-Type", "Authorization", "X-XSRF-TOKEN")
                        // required if credentials (cookies, authorization headers) are involved
                        allowCredentials = true
                    }
                }
            }
            // handlers for any exceptions not handled elsewhere
            .exceptionHandling {
                it.defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/login"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            }
        return http.build()
    }

    @Bean
    // for configuring Spring Authorization Server (e.g. customising URLs for exposed endpoints)
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/