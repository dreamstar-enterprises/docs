package com.example.authorizationserver.auth

import com.example.authorizationserver.auth.filters.DocDbAuthenticationFilter
import com.example.authorizationserver.auth.handlers.DefaultAccessDeniedHandler
import com.example.authorizationserver.auth.handlers.SocialLoginSuccessHandler
import com.example.authorizationserver.auth.repositories.CustomServletCsrfTokenRepository
import com.example.authorizationserver.auth.requestcache.CustomRequestCache
import com.example.authorizationserver.auth.sessions.CustomCsrfAuthenticationStrategy
import com.example.authorizationserver.auth.sessions.CustomInvalidSessionStrategy
import com.example.authorizationserver.auth.sessions.CustomSessionAuthenticationStrategy
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
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository

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
        customServletCsrfTokenRepository : CustomServletCsrfTokenRepository,
        customCsrfAuthenticationStrategy: CustomCsrfAuthenticationStrategy,
        socialLoginSuccessHandler: SocialLoginSuccessHandler,
        docDbAuthenticationFilter: DocDbAuthenticationFilter,
        customRequestCache: CustomRequestCache,
        customSecurityContextRepository: SecurityContextRepository,
        customInvalidSessionStrategy: CustomInvalidSessionStrategy,
        customSessionAuthenticationStrategy: CustomSessionAuthenticationStrategy,
        accessDeniedHandler: DefaultAccessDeniedHandler,
    ): SecurityFilterChain {

        // enable csrf
        http.csrf { csrf ->
            csrf.csrfTokenRepository(customServletCsrfTokenRepository)
            csrf.sessionAuthenticationStrategy(customCsrfAuthenticationStrategy)
            csrf.requireCsrfProtectionMatcher { request ->
                !request.method.equals("GET", ignoreCase = true)
            }
        }

        // setup session management - use stateless, and set other configurations
        http.sessionManagement { session ->
            // not truly stateless since HttpSessionSecurityContextRepository is used
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            session.enableSessionUrlRewriting(false)
            session.invalidSessionStrategy(customInvalidSessionStrategy)
            session.sessionAuthenticationStrategy(customSessionAuthenticationStrategy)
        }

        // apply security context repository
        http.securityContext { context ->
            context.securityContextRepository(customSecurityContextRepository)
        }

        // configure request cache
        http.requestCache { requestCache ->
            requestCache.requestCache(customRequestCache)
        }

        // form login handles the redirect to the login page from earlier filter chain
        http.formLogin { formLogin ->
            formLogin
                .permitAll()
        }

        // oauth2.0 client login (google)
        http.oauth2Login { oauth ->
            oauth
                .clientRegistrationRepository(servletClientRegistrationRepository)
                .authorizedClientRepository(servletAuthorizedClientRepository)
                .authorizedClientService(servletAuthorizedClientService)
                .successHandler(socialLoginSuccessHandler)
        }

        // apply DocDb authentication filter
        http.addFilterBefore(
            docDbAuthenticationFilter,
            UsernamePasswordAuthenticationFilter::class.java
        )

        // authorizations (all end points, apart from login and logout not permitted, unless authenticated)
        http.authorizeHttpRequests { authorize ->
            authorize
                .anyRequest().authenticated()
        }

        // perform cleanup operations on logout (invalidate session, remove cookies & authentication object)
        // (note: this does not invalidate access or refresh tokens - they expire whenever they expire)
        http.logout { logout ->
            logout.logoutUrl("/logout")
            logout.invalidateHttpSession(true)
            logout.clearAuthentication(true)
            logout.deleteCookies("AUTH-SESSIONID")
            logout.permitAll()
        }

        // handlers for any exceptions not handled elsewhere
        http.exceptionHandling { exceptionHandling ->
            exceptionHandling.accessDeniedHandler(accessDeniedHandler)
        }

        return http.build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/