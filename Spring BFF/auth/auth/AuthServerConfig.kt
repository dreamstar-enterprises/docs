package com.example.authorizationserver.auth

import com.example.authorizationserver.props.LoginProperties
import com.example.authorizationserver.props.ServerProperties
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
import java.util.*

/**********************************************************************************************************************/
/********************************************* AUTHORIZATION SERVER CONFIGURATION *************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebSecurity
internal class AuthServerConfig (
    private val serverProperties: ServerProperties,
    private val loginProperties: LoginProperties
) {

    @Bean
    @Order(1)
    @Throws(Exception::class)
    /* security filter chain for protocol endpoints */
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {

        // disable csrf
        http.csrf { csrf -> csrf.disable() }

        // apply default http security settings to oauth 2.0 (e.g. default endpoints)
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)

        // enable OpenID Connect 1.0
        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .oidc(withDefaults())

        // unauthenticated exception handler
        // redirect to the login form page when not authenticated
        http.exceptionHandling { exceptionHandling ->
            exceptionHandling.defaultAuthenticationEntryPointFor(
                LoginUrlAuthenticationEntryPoint(loginProperties.LOGIN_FORM_URL),
                MediaTypeRequestMatcher(MediaType.TEXT_HTML)
            )
        }

        return http.build()
    }

    @Bean
    // for configuring Spring Authorization Server (e.g. customising URLs for exposed endpoints)
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder()
            .issuer(serverProperties.inHouseIssuerUri)
            .build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/