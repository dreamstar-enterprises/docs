package com.example.authorizationserver.auth.security

import com.example.authorizationserver.auth.security.filters.DocDbAuthenticationFilter
import com.example.authorizationserver.auth.security.handlers.DefaultAccessDeniedHandler
import com.example.authorizationserver.auth.security.handlers.SocialLoginSuccessHandler
import com.example.authorizationserver.auth.security.providers.DocDbAuthenticationProvider
import com.example.authorizationserver.auth.security.requestcache.CustomRequestCache
import com.example.authorizationserver.auth.security.sessions.CustomInvalidSessionStrategy
import com.example.authorizationserver.auth.security.userservice.DocDbUserDetailsManagerImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.cors.CorsConfiguration

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebSecurity
internal class DefaultSecurityConfig () {

    // google client id
    @Value("\${oauth2.client.registration.google.client-id}")
    lateinit var googleClientId: String

    // google client secret
    @Value("\${oauth2.client.registration.google.client-secret}")
    lateinit var googleClientSecret: String

    @Bean
    @Order(2)
    @Throws(Exception::class)
    /* security filter chain for authentication & authorization */
    fun defaultSecurityFilterChain(
        http: HttpSecurity,
        socialLoginSuccessHandler: SocialLoginSuccessHandler,
        docDbAuthenticationFilter: DocDbAuthenticationFilter,
        docDbUserDetailsManagerImpl: DocDbUserDetailsManagerImpl,
        accessDeniedHandler: DefaultAccessDeniedHandler,
        customSecurityContextRepository: SecurityContextRepository
    ): SecurityFilterChain {

        http
            // disable csrf
            .csrf { csrf -> csrf.disable() }
            // configure cords
//            .cors { cors ->
//                cors.configurationSource {
//                    CorsConfiguration().apply {
//                        // ensure this matches your Angular app URL
//                        allowedOrigins = listOf("http://localhost:4200")
//                        allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                        allowedHeaders = listOf("Content-Type", "Authorization", "X-XSRF-TOKEN")
//                        exposedHeaders = listOf("Content-Type", "Authorization", "X-XSRF-TOKEN")
//                        // required if credentials (cookies, authorization headers) are involved
//                        allowCredentials = true
//                    }
//                }
//            }
            // setup session management - use stateless, and set other configurations
            .sessionManagement { session ->
                // not truly stateless since HttpSessionSecurityContextRepository is used
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                session.sessionFixation().migrateSession()
                session.maximumSessions(1).maxSessionsPreventsLogin(true)
                session.enableSessionUrlRewriting(false)
                session.invalidSessionStrategy(CustomInvalidSessionStrategy())
            }
            // apply security context repository
            .securityContext { context ->
                context.securityContextRepository(customSecurityContextRepository)
            }
            // configure request cache
            .requestCache { requestCache ->
                requestCache.requestCache(CustomRequestCache())
            }
            // form login handles the redirect to the login page from earlier filter chain
            .formLogin { formLogin ->
                formLogin
                    .permitAll()
            }
            // oauth2.0 client login
            .oauth2Login { oauth ->
                oauth
                    .successHandler { request, response, authentication ->  }
                    .successHandler(socialLoginSuccessHandler)
            }
            // perform cleanup operations on logout (invalidate session, remove cookies & authentication object)
            // (note: this does not invalidate access or refresh tokens - they expire whenever they expire)
            .logout { logout ->
                logout.logoutUrl("/logout")
                logout.invalidateHttpSession(true)
                logout.deleteCookies("JSESSIONID")
                logout.clearAuthentication(true)
                logout.permitAll()
            }
            // apply DocDb authentication filter (internally has custom success & failure handlers)
            .addFilterBefore(
                docDbAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            // authorizations (all end points, apart from login and logout not permitted, unless authenticated)
            .authorizeHttpRequests { authorize ->
                authorize
                    .anyRequest().authenticated()
            }
            // handlers for any exceptions not handled elsewhere
            .exceptionHandling { exceptionHandling ->
                exceptionHandling.accessDeniedHandler(accessDeniedHandler)
            }

        return http.build()
    }

    @Bean
    // authentication manager (adds providers to the manager)
    fun authenticationManager(
        http: HttpSecurity,
        docDbAuthenticationProvider: DocDbAuthenticationProvider,
    ): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder.authenticationProvider(docDbAuthenticationProvider)
        return authenticationManagerBuilder.build()
    }

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        return InMemoryClientRegistrationRepository(googleClientRegistration())
    }

    private fun googleClientRegistration(): ClientRegistration {
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
            .clientId(googleClientId)
            .clientSecret(googleClientSecret)
            .build()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/