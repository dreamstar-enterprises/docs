package com.example.gateway.routing

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**********************************************************************************************************************/
/***************************************************** GATEWAY ROUTING ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RoutingConfig {

    @Value("\${resource-url}")
    private lateinit var resourceUrl: String

    @Value("\${public-spa-url}")
    private lateinit var publicSpaUrl: String


//    @Bean
//    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator {
//        return builder.routes()
//            // routing for Resource Server
//            .route("resource-server") { r ->
//                r.path("/resource/**")
//                    .filters { f ->
//                        f.prefixPath("/api")
//                            .filter { exchange, chain ->
//                                // Add your custom filter logic here if needed
//                                chain.filter(exchange)
//                            }
//                            .tokenRelay()
////                            .saveSession()
//                    }
//                    .uri(resourceUrl)
//            }
//            // routing for User Interface
//            .route("public-spa") { r ->
//                r.path("/ui/**")
//                    .uri(publicSpaUrl)
//            }
//            .build()
//    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/