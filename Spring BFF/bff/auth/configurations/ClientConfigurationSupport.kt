package com.example.bff.auth.configurations

import com.example.bff.auth.configurations.postprocessors.ClientAuthorizeExchangeSpecPostProcessor
import com.example.bff.auth.configurations.postprocessors.ClientReactiveHttpSecurityPostProcessor
import com.example.bff.props.AuthorizationProperties
import com.example.bff.props.CorsProperties
import org.springframework.boot.autoconfigure.web.ServerProperties as NettyServerProperties
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/*************************************************** CLIENT CONFIGURATION *********************************************/
/**********************************************************************************************************************/

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

            val corsProps: List<CorsProperties> = listOf(corsProperties)

            // configure state
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