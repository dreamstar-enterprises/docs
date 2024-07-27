package com.example.authorizationserver.auth.security.cors

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**********************************************************************************************************************/
/**************************************************** CORS CONFIGURATION ***********************************************/
/**********************************************************************************************************************/

//@Configuration
//// applies to web browser clients only - not server-to-server
//class CORSConfig {
//
//    @Bean
//    fun corsConfigurer(): WebMvcConfigurer {
//        return object : WebMvcConfigurer {
//            override fun addCorsMappings(registry: CorsRegistry) {
//                // apply CORS settings to the specific endpoint
//                registry.addMapping("/oauth2/authorize")
//                    .allowedOrigins("http://localhost:4200")
//                    .allowedMethods(
//                        HttpMethod.GET.name(),
//                        HttpMethod.POST.name(),
//                        HttpMethod.PUT.name(),
//                        HttpMethod.PATCH.name(),
//                        HttpMethod.DELETE.name(),
//                        HttpMethod.OPTIONS.name(),
//                    )
//                    .allowedHeaders(
//                        HttpHeaders.CONTENT_TYPE,
//                        HttpHeaders.AUTHORIZATION
//                    )
//                    .exposedHeaders(
//                        HttpHeaders.CONTENT_TYPE,
//                        HttpHeaders.AUTHORIZATION
//                    )
//                    .allowCredentials(true)
//
//                // apply restrictive CORS settings to other endpoints
//                registry.addMapping("/**")
//                    .allowedOrigins()  // No origins allowed
//                    .allowedMethods(
//                        HttpMethod.GET.name(),
//                        HttpMethod.POST.name(),
//                        HttpMethod.PUT.name(),
//                        HttpMethod.PATCH.name(),
//                        HttpMethod.DELETE.name(),
//                        HttpMethod.OPTIONS.name(),
//                    )
//                    .allowedHeaders(
//                        HttpHeaders.CONTENT_TYPE,
//                        HttpHeaders.AUTHORIZATION,
//                        "X-XSRF-TOKEN"
//                    )
//                    .exposedHeaders(
//                        HttpHeaders.CONTENT_TYPE,
//                        HttpHeaders.AUTHORIZATION,
//                        "X-XSRF-TOKEN"
//                    )
//                    .allowCredentials(true)
//                    .maxAge(0)  // disallow pre-flight requests
//            }
//        }
//    }
//}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/