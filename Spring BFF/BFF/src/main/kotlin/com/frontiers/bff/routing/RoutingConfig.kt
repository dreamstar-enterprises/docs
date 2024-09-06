package com.frontiers.bff.routing

import com.frontiers.bff.props.ServerProperties
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
) {

    @Bean
    fun routeLocator(
        builder: RouteLocatorBuilder,
        tokenRelayGatewayFilterFactory: TokenRelayGatewayFilterFactory
    ): RouteLocator {
        return builder.routes()

            // routing for Resource Server
            .route("resource-server") { r ->
                r.path("/api${serverProperties.resourceServerPrefix}/**")
                    .filters { f ->
                        f.filter { exchange, chain ->
                            chain.filter(exchange)
                        }
                        f.filter(tokenRelayGatewayFilterFactory.apply())
                            .removeRequestHeader("Cookie")
                    }
                    .uri(serverProperties.resourceServerUri)
            }
            .build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/