package com.example.timesheetapi.auth.security

import com.example.timesheetapi.auth.security.filters.IPWhiteListFilter
import com.example.timesheetapi.auth.security.filters.ValidEndPointFilter
import com.example.timesheetapi.auth.security.handlers.AccessDeniedHandler
import com.example.timesheetapi.auth.security.handlers.AuthenticationEntryPoint
import com.nimbusds.jose.JOSEObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenReactiveAuthenticationManager
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


/**********************************************************************************************************************/
/************************************************** SECURITY CONFIGURATION ********************************************/
/**********************************************************************************************************************/

@Configuration
@EnableWebFluxSecurity
@EnableSpringWebSession
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
internal class ResourceServerConfig() {

    // introspection URI (an authorization server endpoint)
    @Value("\${auth.server.introspection.uri}")
    lateinit var introspectionUri: String

    // public key for jw key set for verifying jwt tokens (an authorization server endpoint)
    @Value("\${auth.server.jwkeyset.uri}")
    lateinit var jwkSetUri: String

    // resource server client id
    @Value("\${resourceserver.client.id}")
    lateinit var resourceServerClientID: String

    // resource server client secret
    @Value("\${resourceserver.secret}")
    lateinit var resourceServerSecret: String

    @Bean
    /* security filter chain for authentication & authorization (reactive) */
    /* this should be webSession stateless */
    fun resourceServerSecurityFilterChain(
        http: ServerHttpSecurity,
        authenticationEntryPoint: AuthenticationEntryPoint,
        ipWhiteListFilter: IPWhiteListFilter,
        accessDeniedHandler: AccessDeniedHandler,
        validEndPointFilter: ValidEndPointFilter,
        opaqueTokenConverter: ReactiveOpaqueTokenAuthenticationConverter,
        customSecurityContextRepository: ServerSecurityContextRepository,
    ): SecurityWebFilterChain {

         http 
             // disable csrf
             .csrf { csrf ->
                 csrf.disable()
             }
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
             // configure HTTP Basic Authentication
             .httpBasic { httpBasic ->
                 httpBasic.authenticationEntryPoint(authenticationEntryPoint)
             }
             // apply security context repository (direct assignment)
             .securityContextRepository(customSecurityContextRepository)
             // authorizations
             .authorizeExchange { authorize ->
                 authorize
                     .pathMatchers("/index.html", "/", "/home", "/login").permitAll()
                     .pathMatchers("/logout").permitAll()
                     .anyExchange().permitAll()
             }
             // configure oauth 2.0 resource server
//             .oauth2ResourceServer { oauth2 ->
//                 // choose appropriate authentication manager
//                 oauth2.authenticationManagerResolver(
//                     authenticationManagerResolver(
//                         jwtDecoder(),
//                         opaqueTokenIntrospector(),
//                         opaqueTokenConverter
//                     )
//                 ).authenticationEntryPoint(authenticationEntryPoint)
//             }
             // apply ip-whitelist filter before http basic authentication
             .addFilterBefore(ipWhiteListFilter, SecurityWebFiltersOrder.HTTP_BASIC)
             // apply valid end-point filter before http basic authentication
             .addFilterBefore(validEndPointFilter, SecurityWebFiltersOrder.HTTP_BASIC)
             // handlers for any exceptions not handled elsewhere
             .exceptionHandling { exceptionHandling ->
                 exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
                 exceptionHandling.accessDeniedHandler(accessDeniedHandler)
             }

        return http.build()
    }

    @Bean
    fun userDetailsService(): ReactiveUserDetailsService {
        val userDetails = User.withUsername("user")
            .password("{noop}password") // {noop} indicates no password encoding
            .roles("USER")
            .build()

        return ReactiveUserDetailsService { username ->
            Mono.justOrEmpty(userDetails.takeIf { it.username == username })
        }
    }


//    @Bean
    // for choosing (resolving to) appropriate authentication manager
    // useful for multi-tenant configurations
    fun authenticationManagerResolver(
        jwtDecoder: ReactiveJwtDecoder,
        opaqueTokenIntrospector: NimbusReactiveOpaqueTokenIntrospector,
        opaqueTokenConverter: ReactiveOpaqueTokenAuthenticationConverter
    ): ReactiveAuthenticationManagerResolver<ServerWebExchange> {

        // authentication manager for jwt tokens (with no converter)
        val jwtAuthManager = JwtReactiveAuthenticationManager(jwtDecoder)

        // authentication manager for opaque tokens (with token >>> authentication object, converter)
        val opaqueAuthManager = OpaqueTokenReactiveAuthenticationManager(opaqueTokenIntrospector)
        opaqueAuthManager.setAuthenticationConverter(opaqueTokenConverter)

        // resolving conditions
        return ReactiveAuthenticationManagerResolver { exchange ->
            isJwtValid(exchange)
                .flatMap { isValidJwt ->
                    if (isValidJwt) {
                        Mono.just(jwtAuthManager)
                    } else {
                        Mono.just(opaqueAuthManager)
                    }
                }
        }

    }

    // helper function for checking if access token is a JSON WEB TOKEN (J.W.T.)
    private fun isJwtValid(exchange: ServerWebExchange): Mono<Boolean> {
        // resolve Bearer Token asynchronously
        return Mono.fromCallable {
            val headers: HttpHeaders = exchange.request.headers
            val authorizationHeader: String? = headers.getFirst(HttpHeaders.AUTHORIZATION)

            authorizationHeader?.let {
                if (it.startsWith("Bearer ")) {
                    return@fromCallable it.substring(7) // extract token excluding "Bearer "
                }
            }

            // if Authorization header is missing or not in expected format
            return@fromCallable null
        }
        .flatMap { accessToken ->
            if (accessToken != null) {
                // validate JWT asynchronously
                Mono.fromCallable {
                    if (!accessToken.isNotBlank()) {
                        return@fromCallable false
                    }

                    try {
                        val parts = JOSEObject.split(accessToken)
                        parts.size == 3
                    } catch (ignored: Exception) {
                        false
                    }
                }
            } else {
                // handle case where Bearer token is not found or in expected format
                Mono.just(false)
            }
        }
    }

    @Bean
    // for introspecting opaque access tokens with the authorisation server
    fun opaqueTokenIntrospector(): NimbusReactiveOpaqueTokenIntrospector {
        return NimbusReactiveOpaqueTokenIntrospector(introspectionUri, resourceServerClientID, resourceServerSecret)
    }

    @Bean
    // for decoding JWT tokens
    fun jwtDecoder() : ReactiveJwtDecoder  {
        return NimbusReactiveJwtDecoder
        .withJwkSetUri(jwkSetUri)
        .build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/