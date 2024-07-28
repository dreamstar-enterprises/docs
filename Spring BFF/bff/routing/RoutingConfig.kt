package com.example.bff.routing

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

    @Value("\${resource-server-uri}")
    private lateinit var resourceServerUri: String

    @Value("\${resource-server-prefix}")
    private lateinit var resourceServerPrefix: String

    @Bean
    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // routing for Resource Server
            .route("resource-server") { r ->
                r.path("/api$resourceServerPrefix/**")
                    .filters { f ->
                        f.filter { exchange, chain ->
                            // Add your custom filter logic here if needed
                            chain.filter(exchange)
                        }
                            .tokenRelay()
                            .saveSession()
                            .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                            .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                    }
                    .uri(resourceServerUri)
            }
            .build()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/