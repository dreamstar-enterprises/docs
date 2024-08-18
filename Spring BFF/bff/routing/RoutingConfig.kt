package com.example.bff.routing

import com.example.bff.props.ServerProperties
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**********************************************************************************************************************/
/***************************************************** GATEWAY ROUTING ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RoutingConfig(
    private val serverProperties: ServerProperties,
) {

    @Bean
    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // routing for Resource Server
            .route("resource-server") { r ->
                r.path("/api${serverProperties.resourceServerPrefix}/**")
                    .filters { f ->
                        f.filter { exchange, chain ->
                            println("Request Headers: ${exchange.request.headers}")
                            // add custom filter logic here if needed
                            chain.filter(exchange)
                        }
                            .removeRequestHeader("Cookie")
                            .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                            .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                    }
                    .uri(serverProperties.resourceServerUri)
            }
            .build()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/