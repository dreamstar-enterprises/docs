package com.example.bff.auth

import com.example.bff.auth.configurations.ClientConfigurationSupport
import com.example.bff.auth.configurations.postprocessors.ClientAuthorizeExchangeSpecPostProcessor
import com.example.bff.auth.configurations.postprocessors.ClientReactiveHttpSecurityPostProcessor
import com.example.bff.auth.cors.CORSConfig
import com.example.bff.auth.csrf.CsrfProtectionMatcher
import com.example.bff.auth.filters.csrf.CsrfWebCookieFilter
import com.example.bff.auth.handlers.DelegatingAuthenticationSuccessHandler
import com.example.bff.auth.handlers.csrf.SPACsrfTokenRequestHandler
import com.example.bff.auth.handlers.oauth2.OAuth2ServerAuthenticationFailureHandler
import com.example.bff.auth.handlers.oauth2.OAuth2ServerLogoutSuccessHandler
import com.example.bff.auth.handlers.oauth2.PreAuthorizationCodeServerRedirectStrategy
import com.example.bff.auth.handlers.sessions.CustomMaximumSessionsExceededHandler
import com.example.bff.auth.repositories.RedisAuthorizationRequestRepository
import com.example.bff.auth.repositories.authclients.RedisReactiveOAuth2AuthorizedClientService
import com.example.bff.auth.repositories.authclients.RedisServerOAuth2AuthorizedClientRepository
import com.example.bff.auth.repositories.securitycontext.RedisSecurityContextRepository
import com.example.bff.auth.requestcache.ReactiveRequestCache
import com.example.bff.props.*
import com.example.bff.props.ServerProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.LogoutSpec
import org.springframework.security.core.session.ReactiveSessionRegistry
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.*
import org.springframework.boot.autoconfigure.web.ServerProperties as NettyServerProperties

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebFluxSecurity
@EnableRedisIndexedWebSession
@Order(Ordered.LOWEST_PRECEDENCE - 1)
internal class BffSecurityConfig (
    private val serverProperties: ServerProperties,
) {

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web: WebSecurity ->
            web.debug(false)
                .ignoring()
                .requestMatchers("/favicon.ico")
        }
    }

    @Bean
    fun clientSecurityFilterChain(
        http: ServerHttpSecurity,
        nettyServerProperties: NettyServerProperties,

        loginProperties: LoginProperties,

        serverCsrfTokenRepository: ServerCsrfTokenRepository,
        spaCsrfTokenRequestHandler: SPACsrfTokenRequestHandler,
        csrfProtectionMatcher: CsrfProtectionMatcher,
        csrfCookieWebFilter: CsrfWebCookieFilter,

        corsConfig: CORSConfig,
        corsProperties: CorsProperties,

        reactiveRequestCache: ReactiveRequestCache,
        reactiveSessionRegistry: ReactiveSessionRegistry,
        maximumSessionsExceededHandler: CustomMaximumSessionsExceededHandler,

        authenticationProperties: AuthenticationProperties,
        authorizationProperties: AuthorizationProperties,

        oauthAuthorizationRequestResolver: ServerOAuth2AuthorizationRequestResolver,
        redisAuthorizationRequestRepository: RedisAuthorizationRequestRepository,
        preAuthorizationCodeRedirectStrategy: PreAuthorizationCodeServerRedirectStrategy,
        delegatingAuthenticationSuccessHandler: DelegatingAuthenticationSuccessHandler,
        oauth2ServerAuthenticationFailureHandler: OAuth2ServerAuthenticationFailureHandler,

        redisSecurityContextRepository: RedisSecurityContextRepository,
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
        redisAuthorizedClientRepository: RedisServerOAuth2AuthorizedClientRepository,
        redisReactiveOAuth2AuthorizedClientService: RedisReactiveOAuth2AuthorizedClientService,

        logoutProperties: LogoutProperties,
        logoutHandler: Optional<ServerLogoutHandler>,
        logoutSuccessHandler: OAuth2ServerLogoutSuccessHandler,
        backChannelLogoutProperties: BackChannelLogoutProperties,

        authorizePostProcessor: ClientAuthorizeExchangeSpecPostProcessor,
        httpPostProcessor: ClientReactiveHttpSecurityPostProcessor,
    ): SecurityWebFilterChain {

        // initialise logger
        val log = LoggerFactory.getLogger(SecurityWebFilterChain::class.java)

        // apply security matchers to this filter chain
        val clientRoutes: List<ServerWebExchangeMatcher> = authenticationProperties
            .securityMatchers
            .map { PathPatternParserServerWebExchangeMatcher(it) }
        log.info(
            "Applying client OAuth2 configuration for: {}",
            authenticationProperties.securityMatchers
        )
        http.securityMatcher(OrServerWebExchangeMatcher(clientRoutes))

        // unauthenticated exception handler
        loginProperties.LOGIN_URL.let { loginPath ->
            http.exceptionHandling { exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(
                    RedirectServerAuthenticationEntryPoint(
                        UriComponentsBuilder.fromUri(
                            URI.create(serverProperties.clientUri)
                        ).path(loginPath).build().toString()
                    )
                )
            }
        }

        // enable csrf
        http.csrf { csrf ->
            csrf.csrfTokenRepository(serverCsrfTokenRepository)
            csrf.csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
            csrf.requireCsrfProtectionMatcher(csrfProtectionMatcher)
        }

        // configure cors
        http.cors { cors ->
            cors.configurationSource(
                corsConfig.corsConfigurationSource()
            )
        }

        // configure request cache
        http.requestCache { cache ->
            cache.requestCache(reactiveRequestCache)
        }

        // session management
        // this is also handled in the success handler, delegatingAuthenticationSuccessHandler

        // oauth2.0 client login
        http.oauth2Login { oauth2 ->
            oauth2.authorizationRequestResolver(oauthAuthorizationRequestResolver)
            oauth2.authorizationRequestRepository(redisAuthorizationRequestRepository)

            oauth2.authorizationRedirectStrategy(preAuthorizationCodeRedirectStrategy)
            oauth2.authenticationSuccessHandler(delegatingAuthenticationSuccessHandler)
            oauth2.authenticationFailureHandler(oauth2ServerAuthenticationFailureHandler)

            oauth2.securityContextRepository(redisSecurityContextRepository)
            oauth2.clientRegistrationRepository(reactiveClientRegistrationRepository)
            oauth2.authorizedClientRepository(redisAuthorizedClientRepository)
            oauth2.authorizedClientService(redisReactiveOAuth2AuthorizedClientService)
        }

        // oauth2.0 client
        http.oauth2Client {}

        // logout configuration (with relying-party initiated logout)
        http.logout { logout: LogoutSpec ->
            logoutHandler.ifPresent { handler: ServerLogoutHandler ->
                logout.logoutHandler(handler)
            }
            logout.logoutSuccessHandler(logoutSuccessHandler)
        }

        // oidc backchannel logout configuration
        // automatically creates: /logout/connect/back-channel/{registrationId}
        if(backChannelLogoutProperties.enabled) {
            http.oidcLogout { logout ->
                logout.backChannel { bc ->
                    bc.logoutUri(backChannelLogoutProperties.internalLogoutUri)
                }
//                logout.oidcSessionRegistry()
//                logout.clientRegistrationRepository()
                // what is ReactiveOidcSessionStrategy for?
                // https://docs.spring.io/spring-security/reference/reactive/oauth2/login/logout.html#configure-provider-initiated-oidc-logout
            }
        }

        // other filters
        // apply csrf filter after the logout handler
        http.addFilterAfter(csrfCookieWebFilter, SecurityWebFiltersOrder.LOGOUT)

        // apply additional configuraitons
        ClientConfigurationSupport.configureClient(
            http,
            nettyServerProperties,
            corsProperties,
            authorizationProperties,
            authorizePostProcessor,
            httpPostProcessor
        )

        return http.build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/