package com.example.bff.auth

import com.example.bff.auth.csrf.SPACsrfTokenRequestHandler
import com.example.bff.auth.filters.CsrfCookieFilter
import com.example.bff.auth.filters.PostLoginUriFilter
import com.example.bff.auth.handlers.LoginFailureHandler
import com.example.bff.auth.handlers.LoginSuccessHandler
import com.example.bff.auth.requestcache.RequestCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.OidcLogoutSpec
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.ServerMaximumSessionsExceededHandler
import org.springframework.security.web.server.authentication.SessionLimit
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository
//import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry
import org.springframework.web.cors.CorsConfiguration
import reactor.core.publisher.Mono
import java.util.function.Consumer

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebFluxSecurity
//@EnableRedisWebSession
@Order(Ordered.LOWEST_PRECEDENCE - 1)
internal class BffSecurityConfig () {

    @Autowired
    private lateinit var reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository

    @Autowired
    private lateinit var reactiveAuthorizedClientRepository: ServerOAuth2AuthorizedClientRepository

    @Autowired
    private lateinit var reactiveAuthorizedClientService: ReactiveOAuth2AuthorizedClientService

    @Value("\${reverse-proxy-uri}")
    private lateinit var reverseProxyUri: String

    @Value("\${bff-uri}")
    private lateinit var bffUri: String

    @Bean
    fun clientSecurityFilterChain(
        http: ServerHttpSecurity,
        spaCsrfTokenRequestHandler: SPACsrfTokenRequestHandler,
        serverCsrfTokenRepository: ServerCsrfTokenRepository,
        csrfCookieFilter: CsrfCookieFilter,
        requestCache: RequestCache,
//        sessionRegistry: SpringSessionBackedReactiveSessionRegistry<ReactiveRedisIndexedSessionRepository.RedisSession>,
//        maximumSessionsExceededHandler: ServerMaximumSessionsExceededHandler,
        pkceResolver: ServerOAuth2AuthorizationRequestResolver,
        postLoginUriFilter: PostLoginUriFilter,
        loginSuccessHandler: LoginSuccessHandler,
        loginFailureHandler: LoginFailureHandler,
    ): SecurityWebFilterChain {

        http
            // enable csrf
            .csrf { csrf ->
                csrf.csrfTokenRepository(serverCsrfTokenRepository)
                csrf.csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
            }
            // configure cors
            .cors { cors ->
                cors.configurationSource {
                    CorsConfiguration().apply {
                        // ensure this matches the Angular app URL
                        allowedOrigins = listOf(reverseProxyUri)
                        allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        allowedHeaders = listOf("Content-Type", "Authorization", "X-XSRF-TOKEN")
                        exposedHeaders = listOf("Content-Type", "Authorization", "X-XSRF-TOKEN")
                        // required if credentials (cookies, authorization headers) are involved
                        allowCredentials = true
                    }
                }
            }
            // configure request cache
//            .requestCache { cache ->
//                cache.requestCache(requestCache)
//            }
            // session management
//            .sessionManagement {sessionManagement ->
//                sessionManagement.concurrentSessions { sessionConcurrency ->
//                    sessionConcurrency
//                        .maximumSessions(SessionLimit.of(1))
//                        .maximumSessionsExceededHandler(maximumSessionsExceededHandler)
////                        .sessionRegistry(sessionRegistry)
//                }
//            }
            // oauth2.0 client login
            .oauth2Login { oauth2 ->
                oauth2
                    .clientRegistrationRepository(reactiveClientRegistrationRepository)
                    .authorizedClientRepository(reactiveAuthorizedClientRepository)
                    .authorizedClientService(reactiveAuthorizedClientService)
//                    .authorizationRequestResolver(pkceResolver)
//                    .authenticationSuccessHandler(loginSuccessHandler)
//                    .authenticationFailureHandler(loginFailureHandler)
            }
            // authorizations (all end points, apart from login and logout not permitted, unless authenticated)
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/api/**", "/login/**", "/oauth2/**", "/logout/**").permitAll()
                    .pathMatchers("/login-options").permitAll()
                    .anyExchange().authenticated()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler { exchange, authentication ->
                        // indicate that the logout was successful
                        exchange.exchange.response.statusCode = HttpStatus.OK
                        Mono.empty()
                    }
            }
            .oidcLogout { logout ->
                logout.backChannel { bc ->
                    bc.logoutUri(bffUri + "/logout")
                }
            }
            // add post login filter
//            .addFilterAfter(postLoginUriFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            // apply csrf filter after the logout handler
            .addFilterAfter(csrfCookieFilter, SecurityWebFiltersOrder.LOGOUT)

        return http.build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/