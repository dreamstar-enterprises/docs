package com.example.authorizationserver.auth

import com.example.authorizationserver.auth.csrf.CsrfProtectionMatcher
import com.example.authorizationserver.auth.filters.DocDbAuthenticationFilter
import com.example.authorizationserver.auth.handlers.DefaultAccessDeniedHandler
import com.example.authorizationserver.auth.handlers.SocialLoginSuccessHandler
import com.example.authorizationserver.auth.repositories.tokens.CustomServletCsrfTokenRepository
//import com.example.authorizationserver.auth.repositories.tokens.RedisRememberMeTokenRepository
import com.example.authorizationserver.auth.requestcache.ServletRequestCache
import com.example.authorizationserver.auth.csrf.CustomCsrfAuthenticationStrategy
import com.example.authorizationserver.auth.sessions.CustomInvalidSessionStrategy
import com.example.authorizationserver.auth.sessions.CustomSessionAuthenticationStrategy
import com.example.authorizationserver.props.LogoutProperties
import com.example.authorizationserver.props.SessionProperties
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
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
//import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebSecurity
//@EnableRedisHttpSession( )
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
        csrfProtectionMatcher: CsrfProtectionMatcher,
//        sessionRememberMeServices: SpringSessionRememberMeServices,
        socialLoginSuccessHandler: SocialLoginSuccessHandler,
        docDbAuthenticationFilter: DocDbAuthenticationFilter,
        servletRequestCache: ServletRequestCache,
        customSecurityContextRepository: SecurityContextRepository,
        sessionProperties: SessionProperties,
        customInvalidSessionStrategy: CustomInvalidSessionStrategy,
        customSessionAuthenticationStrategy: CustomSessionAuthenticationStrategy,
//        redisRememberMeTokenRepository: RedisRememberMeTokenRepository,
        logoutProperties: LogoutProperties,
        accessDeniedHandler: DefaultAccessDeniedHandler,
    ): SecurityFilterChain {

        // enable csrf
        http.csrf { csrf ->
            csrf.csrfTokenRepository(customServletCsrfTokenRepository)
            csrf.sessionAuthenticationStrategy(customCsrfAuthenticationStrategy)
            csrf.requireCsrfProtectionMatcher(csrfProtectionMatcher)
        }

        // setup session management - use stateless, and set other configurations
        http.sessionManagement { session ->
            // not truly stateless since HttpSessionSecurityContextRepository is used
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            session.enableSessionUrlRewriting(false)
//            session.invalidSessionStrategy(customInvalidSessionStrategy)
//            session.sessionAuthenticationStrategy(customSessionAuthenticationStrategy)
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
            requestCache.requestCache(servletRequestCache)
        }

        // form login handles the redirect to the login page from earlier filter chain
        http.formLogin { formLogin ->
            formLogin
                .permitAll()
        }

        // oauth2.0 client login (google)
        http.oauth2Login { oauth ->
            oauth.clientRegistrationRepository(servletClientRegistrationRepository)
            oauth.authorizedClientRepository(servletAuthorizedClientRepository)
            oauth.authorizedClientService(servletAuthorizedClientService)
            oauth.successHandler(socialLoginSuccessHandler)
        }

        // apply DocDb authentication filter
        http.addFilterBefore(
            docDbAuthenticationFilter,
            UsernamePasswordAuthenticationFilter::class.java
        )

        // authorizations (lock all endpoints apart from)
        http.authorizeHttpRequests { authorize ->
            // login endpoint
            authorize.requestMatchers("/login/**").permitAll()
            // logout endpoint
            authorize.requestMatchers("/logout/**").permitAll()
            // authorization endpoints
            authorize.requestMatchers("/oauth2/**").permitAll()
            // userinfo endpoint
            authorize.requestMatchers("/userinfo").permitAll()
            // logout endpoint
            authorize.requestMatchers("/connect/logout").permitAll()
            // all other endpoints
            authorize.anyRequest().authenticated()
        }

        // perform cleanup operations on logout (invalidate session, remove cookies & authentication object)
        // (note: this does not invalidate access or refresh tokens - they expire whenever they expire)
        http.logout { logout ->
            logout.logoutUrl(logoutProperties.LOGOUT_URL)
            logout.invalidateHttpSession(logoutProperties.INVALIDATE_HTTP_SESSION)
            logout.clearAuthentication(logoutProperties.CLEAR_AUTHENTICATION)
            logout.deleteCookies(sessionProperties.SESSION_COOKIE_NAME)
            logout.addLogoutHandler(HeaderWriterLogoutHandler(ClearSiteDataHeaderWriter(COOKIES)))
            logout.permitAll()
        }

        // unauthorized exception handler
        http.exceptionHandling { exceptionHandling ->
            exceptionHandling.accessDeniedHandler(accessDeniedHandler)
        }

        return http.build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/