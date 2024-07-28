package com.example.bff.auth.cors

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

/**********************************************************************************************************************/
/**************************************************** CORS CONFIGURATION **********************************************/
/**********************************************************************************************************************/

@Configuration
// applies to web browser clients only - not server-to-server
class CORSConfig {

    @Value("\${reverse-proxy-uri}")
    private lateinit var reverseProxyUri: String

    @Bean
    fun corsConfig(): WebFluxConfigurer {
        return object : WebFluxConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins(reverseProxyUri)
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
                        "X-XSRF-TOKEN",
                    )
                    .exposedHeaders(
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.AUTHORIZATION,
                        "X-XSRF-TOKEN",
                    )
                    .allowCredentials(true)
            }
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/