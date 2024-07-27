package com.example.gateway.auth

import com.example.gateway.auth.csrf.SPACsrfTokenRequestHandler
import com.example.gateway.auth.filters.CsrfCookieFilter
import com.example.gateway.auth.requestcache.RequestCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.ServerMaximumSessionsExceededHandler
import org.springframework.security.web.server.authentication.SessionLimit
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry
import org.springframework.web.cors.CorsConfiguration
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebFluxSecurity
//@EnableRedisWebSession
internal class GatewaySecurityConfig () {

    @Autowired
    private lateinit var reactiveClientRegistrationRepository: InMemoryReactiveClientRegistrationRepository

    @Autowired
    private lateinit var reactiveAuthorizedClientService: ReactiveOAuth2AuthorizedClientService

    @Bean
    fun apiGatewayServerSecurityFilterChain(
        http: ServerHttpSecurity,
        spaCsrfTokenRequestHandler: SPACsrfTokenRequestHandler,
        serverCsrfTokenRepository: ServerCsrfTokenRepository,
        csrfCookieFilter: CsrfCookieFilter,
        requestCache: RequestCache,
        sessionRegistry: SpringSessionBackedReactiveSessionRegistry<ReactiveRedisIndexedSessionRepository.RedisSession>,
        maximumSessionsExceededHandler: ServerMaximumSessionsExceededHandler
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
                        allowedOrigins = listOf("http://localhost:4200")
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
            .sessionManagement {sessionManagement ->
                sessionManagement.concurrentSessions { sessionConcurrency ->
                    sessionConcurrency
                        .maximumSessions(SessionLimit.of(1))
                        .maximumSessionsExceededHandler(maximumSessionsExceededHandler)
                        .sessionRegistry(sessionRegistry)
                }
            }
            // oauth2.0 client login
            .oauth2Login { oauth2 ->
                oauth2
                    .clientRegistrationRepository(reactiveClientRegistrationRepository)
                    .authorizedClientService(reactiveAuthorizedClientService)
            }
            // authorizations (all end points, apart from login and logout not permitted, unless authenticated)
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/login/**", "/fallback").permitAll()
                    .pathMatchers("/ui/**").permitAll()
                    .pathMatchers("/logout").permitAll()
                    .anyExchange().authenticated()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutHandler { exchange, authentication ->
                        // perform custom logout handling here if needed
                        Mono.empty()
                    }
                    .logoutSuccessHandler { exchange, authentication ->
                        // indicate that the logout was successful
                        exchange.exchange.response.statusCode = HttpStatus.OK
                        Mono.empty()
                    }
            }
            // apply csrf filter after the logout handler
            .addFilterAfter(csrfCookieFilter, SecurityWebFiltersOrder.LOGOUT)

        return http.build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/