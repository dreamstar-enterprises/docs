package com.frontiers.bff.auth.configurations.postprocessors

import com.frontiers.bff.auth.configurations.ClientAuthorizeExchangeSpecPostProcessor
import com.frontiers.bff.auth.configurations.ClientReactiveHttpSecurityPostProcessor
import com.frontiers.bff.props.AuthorizationProperties
import com.frontiers.bff.props.CorsProperties
import org.springframework.boot.autoconfigure.web.ServerProperties as NettyServerProperties
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/*************************************************** CLIENT CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/ReactiveConfigurationSupport.java

/**
 * Sets additional configurations for the main OAuth2 Client Security chain
 */
@Component
internal class ClientConfigurationSupport private constructor() {

    companion object {

        @JvmStatic
        // configure OAuth 2.0 client security filter
        fun configureClient(
            http: ServerHttpSecurity,
            nettyServerProperties: NettyServerProperties,
            corsProperties: CorsProperties,
            authorizationProperties: AuthorizationProperties,
            authorizePostProcessor: ClientAuthorizeExchangeSpecPostProcessor,
            httpPostProcessor: ClientReactiveHttpSecurityPostProcessor
        ): ServerHttpSecurity {

            // wrap corsProperties into a List
            val corsProps: List<CorsProperties> = listOf(corsProperties)

            // configure state (default is false)
            configureState(http, false)

            // configure access
            configureAccess(http, authorizationProperties.permitAll, corsProps)

            // configure ssl
            if (nettyServerProperties.ssl != null && nettyServerProperties.ssl.isEnabled) {
                http.redirectToHttps { }
            }

            // security rules for all paths that are not listed in "permit-all"
            http.authorizeExchange { authorizeExchangeSpec ->
                authorizePostProcessor.authorizeHttpRequests(authorizeExchangeSpec)
            }

            // hook to override all or part of HttpSecurity auto-configuration.
            httpPostProcessor.process(http)

            return http
        }

        @JvmStatic
        // function for configuring authorization access
        fun configureAccess(
            http: ServerHttpSecurity,
            permitAll: List<String>,
            corsProperties: List<CorsProperties>
        ): ServerHttpSecurity {

            // allow access for prefetch request - OPTIONS?
            val permittedCorsOptions = corsProperties
                .filter { cors ->
                    (cors.allowedMethods.contains("*") || cors.allowedMethods.contains("OPTIONS")) &&
                            !cors.disableAnonymousOptions
                }
                .map { it.path }

            // configure with defaults
            if (permitAll.isNotEmpty() || permittedCorsOptions.isNotEmpty()) {
                http.anonymous(Customizer.withDefaults())
            }

            // set permitAll path matchers
            if (permitAll.isNotEmpty()) {
                http.authorizeExchange { authorizeExchange ->
                    authorizeExchange.pathMatchers(
                        *permitAll.toTypedArray()
                    ).permitAll()
                }
            }

            // set CORS path matchers
            if (permittedCorsOptions.isNotEmpty()) {
                http.authorizeExchange { authorizeExchange ->
                    authorizeExchange.pathMatchers(
                        HttpMethod.OPTIONS,
                        *permittedCorsOptions.toTypedArray()
                    ).permitAll()
                }
            }

            return http
        }

        @JvmStatic
        // function for configuring state
        fun configureState(http: ServerHttpSecurity, isStateless: Boolean): ServerHttpSecurity {
            if (isStateless) {
                http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            }

            return http
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/