package com.frontiers.bff.routing

import com.frontiers.bff.props.ServerProperties
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import java.time.Duration

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

                        // token relay filter
                        f.filter(tokenRelayGatewayFilterFactory.apply())

                        // retry configuration
                        f.retry { retryConfig ->
                            retryConfig.retries = 3
                            retryConfig.setMethods(HttpMethod.GET)
                            retryConfig.setBackoff(
                                Duration.ofMillis(50),
                                Duration.ofMillis(500),
                                2,
                                true
                            )
                        }

                        // pass-through filter (optional, can be omitted if not needed)
                        f.filter { exchange, chain ->
                            chain.filter(exchange)
                        }
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