package com.frontiers.bff.auth.cors

import com.frontiers.bff.props.CorsProperties
import com.frontiers.bff.props.CsrfProperties
import com.frontiers.bff.props.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

/**********************************************************************************************************************/
/**************************************************** CORS CONFIGURATION **********************************************/
/**********************************************************************************************************************/

/**
 * Configures all CORS Properties
 */
@Configuration
// applies to web browser clients only - not server-to-server
internal class CORSConfig (
    private val serverProperties : ServerProperties,
    private val corsProperties: CorsProperties,
    private val csrfProperties: CsrfProperties
){

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration().apply {
            // ensure this matches the Angular app URI
            allowedOrigins = corsProperties.allowedOriginPatterns
            allowedMethods = corsProperties.allowedMethods
            allowedHeaders = corsProperties.allowedHeaders
            exposedHeaders = corsProperties.exposedHeaders
            maxAge = corsProperties.maxAge
            allowCredentials = corsProperties.allowCredentials
        }
        source.registerCorsConfiguration(corsProperties.path, config)
        return source
    }

    @Bean
    fun corsFilter(): CorsWebFilter {
        return CorsWebFilter(corsConfigurationSource())
    }

    @Bean
    fun corsConfig(): WebFluxConfigurer {
        return object : WebFluxConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping(corsProperties.path)
                    .allowedOrigins(serverProperties.reverseProxyUri)
                    .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name(),
                    )
                    .allowedHeaders(
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.AUTHORIZATION,
                        csrfProperties.CSRF_HEADER_NAME,
                    )
                    .exposedHeaders(
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.AUTHORIZATION,
                        csrfProperties.CSRF_HEADER_NAME,
                    )
                    .allowCredentials(true)
            }
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/