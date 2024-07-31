package com.example.authorizationserver.auth.security

import com.example.authorizationserver.auth.security.filters.DocDbAuthenticationFilter
import com.example.authorizationserver.auth.security.filters.PostAuthenticationFilter
import com.example.authorizationserver.auth.security.handlers.DefaultAccessDeniedHandler
import com.example.authorizationserver.auth.security.handlers.SocialLoginSuccessHandler
import com.example.authorizationserver.auth.security.requestcache.CustomRequestCache
import com.example.authorizationserver.auth.security.sessions.CustomInvalidSessionStrategy
import com.example.authorizationserver.auth.security.sessions.CustomSessionAuthenticationStrategy
import com.example.authorizationserver.auth.security.userservice.DocDbUserDetailsManagerImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.SecurityContextRepository

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebSecurity
internal class DefaultSecurityConfig () {

    @Autowired
    private lateinit var servletClientRegistrationRepository: ClientRegistrationRepository

    @Autowired
    private lateinit var servletAuthorizedClientRepository: OAuth2AuthorizedClientRepository

    @Autowired
    private lateinit var servletAuthorizedClientService: OAuth2AuthorizedClientService

    @Bean
    @Order(2)
    @Throws(Exception::class)
    /* security filter chain for authentication & authorization */
    fun defaultSecurityFilterChain(
        http: HttpSecurity,
        socialLoginSuccessHandler: SocialLoginSuccessHandler,
        docDbAuthenticationFilter: DocDbAuthenticationFilter,
        customRequestCache: CustomRequestCache,
        customSecurityContextRepository: SecurityContextRepository,
        customInvalidSessionStrategy: CustomInvalidSessionStrategy,
        customSessionAuthenticationStrategy: CustomSessionAuthenticationStrategy,
        accessDeniedHandler: DefaultAccessDeniedHandler,
    ): SecurityFilterChain {

        http
            // disable csrf
            .csrf { csrf -> csrf.disable() }
            // setup session management - use stateless, and set other configurations
            .sessionManagement { session ->
                // not truly stateless since HttpSessionSecurityContextRepository is used
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                session.enableSessionUrlRewriting(false)
                session.invalidSessionStrategy(customInvalidSessionStrategy)
                session.sessionAuthenticationStrategy(customSessionAuthenticationStrategy)
            }
            // apply security context repository
            .securityContext { context ->
                context.securityContextRepository(customSecurityContextRepository)
            }
            // configure request cache
            .requestCache { requestCache ->
                requestCache.requestCache(customRequestCache)
            }
            // form login handles the redirect to the login page from earlier filter chain
            .formLogin { formLogin ->
                formLogin
                    .permitAll()
            }
            // oauth2.0 client login (google)
            .oauth2Login { oauth ->
                oauth
                    .clientRegistrationRepository(servletClientRegistrationRepository)
                    .authorizedClientRepository(servletAuthorizedClientRepository)
                    .authorizedClientService(servletAuthorizedClientService)
                    .successHandler(socialLoginSuccessHandler)
            }
            // perform cleanup operations on logout (invalidate session, remove cookies & authentication object)
            // (note: this does not invalidate access or refresh tokens - they expire whenever they expire)
            .logout { logout ->
                logout.logoutUrl("/logout")
                logout.deleteCookies("AUTH-SESSIONID")
                logout.clearAuthentication(true)
                logout.invalidateHttpSession(true)
                logout.permitAll()
            }
            // apply DocDb authentication filter
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

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/