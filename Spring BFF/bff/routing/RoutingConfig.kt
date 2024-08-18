package com.example.bff.routing

import com.example.bff.auth.BffSecurityIgnoreConfig
import com.example.bff.props.ServerProperties
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory
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
    private val uriEndPointFilter: BffSecurityIgnoreConfig
) {

    @Bean
    fun routeLocator(
        builder: RouteLocatorBuilder,
        tokenRelayGatewayFilterFactory: TokenRelayGatewayFilterFactory
    ): RouteLocator {
        return builder.routes()

            // routing for skipping security context
            .route("static-resources") { r ->
                r.path("/api${serverProperties.resourceServerPrefix}/**").and().predicate { exchange ->
                    val requestPath = exchange.request.uri.path
                    uriEndPointFilter.shouldSkipStaticResources(requestPath)
                }
                    .filters { f ->
                        f.filter { exchange, chain ->
                            println("Handling resource: ${exchange.request.uri.path}")
                            chain.filter(exchange)
                        }
                    }
                    .uri(serverProperties.reverseProxyUri)
            }

            // routing for Resource Server
            .route("resource-server") { r ->
                r.path("/api${serverProperties.resourceServerPrefix}/**")
                    .filters { f ->
                        f.filter { exchange, chain ->
                            chain.filter(exchange)
                        }
                        f.filter(tokenRelayGatewayFilterFactory.apply())
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