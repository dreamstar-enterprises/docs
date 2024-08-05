package com.example.authorizationserver.auth

import com.example.authorizationserver.auth.filters.DocDbAuthenticationFilter
import com.example.authorizationserver.auth.handlers.DefaultAccessDeniedHandler
import com.example.authorizationserver.auth.handlers.SocialLoginSuccessHandler
import com.example.authorizationserver.auth.repositories.tokens.CustomServletCsrfTokenRepository
import com.example.authorizationserver.auth.repositories.tokens.RedisRememberMeTokenRepository
import com.example.authorizationserver.auth.requestcache.CustomRequestCache
import com.example.authorizationserver.auth.csrf.CustomCsrfAuthenticationStrategy
import com.example.authorizationserver.auth.sessions.CustomInvalidSessionStrategy
import com.example.authorizationserver.auth.sessions.CustomSessionAuthenticationStrategy
import com.example.authorizationserver.props.SecurityProperties
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
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive.COOKIES
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebSecurity
@EnableRedisHttpSession( )
internal class DefaultSecurityConfig (
    private val securityProperties: SecurityProperties
) {

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
        sessionRememberMeServices: SpringSessionRememberMeServices,
        socialLoginSuccessHandler: SocialLoginSuccessHandler,
        docDbAuthenticationFilter: DocDbAuthenticationFilter,
        customRequestCache: CustomRequestCache,
        customSecurityContextRepository: SecurityContextRepository,
        customInvalidSessionStrategy: CustomInvalidSessionStrategy,
        customSessionAuthenticationStrategy: CustomSessionAuthenticationStrategy,
        redisRememberMeTokenRepository: RedisRememberMeTokenRepository,
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

        // creates a more persistent rememberMe token, that isn't lost when browser closes
        // (unlike a session cookie, that will be lost)
//        http.rememberMe { rememberMe ->
//            rememberMe.rememberMeServices(sessionRememberMeServices)
//            rememberMe.useSecureCookie(false) // scope is not just on secure connections
//            rememberMe.key(securityProperties.rememberMeKey)
//            rememberMe.rememberMeCookieName("REMEMBER-ME-SESSIONID")
//            rememberMe.tokenRepository(redisRememberMeTokenRepository)
//            // rememberMe.userDetailsService() - NEED TO IMPLEMENT
//        }

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

        // authorizations (lock all endpoints apart from)
        http.authorizeHttpRequests { authorize ->
            authorize
                .requestMatchers("/login/**").permitAll()
                .requestMatchers("/logout/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/userinfo").permitAll()
                .requestMatchers("/connect/logout").permitAll()
                .anyRequest().authenticated()
        }

        // perform cleanup operations on logout (invalidate session, remove cookies & authentication object)
        // (note: this does not invalidate access or refresh tokens - they expire whenever they expire)
        http.logout { logout ->
            logout.logoutUrl("/logout")
            logout.invalidateHttpSession(true)
            logout.clearAuthentication(true)
            logout.deleteCookies("AUTH-SESSIONID")
            logout.addLogoutHandler(HeaderWriterLogoutHandler(ClearSiteDataHeaderWriter(COOKIES)))
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