package com.frontiers.bff.routing

import com.frontiers.bff.props.ServerProperties
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.Supplier

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
        tokenRelayGatewayFilterFactory: TokenRelayGatewayFilterFactory,
        timeLimiterRegistry: TimeLimiterRegistry
    ): RouteLocator {
        return builder.routes()

            // routing for Resource Server
            .route("resource-server") { r ->
                r.path("/api/v1${serverProperties.resourceServerPrefix}/**")
                    .filters { f ->

                        // token relay filter
                        f.filter(tokenRelayGatewayFilterFactory.apply())

                        // circuit breaker configuration
                        f.circuitBreaker { circuitBreakerConfig ->
                            circuitBreakerConfig.setName("resourceServerCircuitBreaker")
                            circuitBreakerConfig.setFallbackUri("forward:/fallback")
                            circuitBreakerConfig.setStatusCodes(
                                setOf(
                                    HttpStatus.INTERNAL_SERVER_ERROR.value().toString(),            // 500
                                    HttpStatus.NOT_IMPLEMENTED.value().toString(),                  // 501
                                    HttpStatus.BAD_GATEWAY.value().toString(),                      // 502
                                    HttpStatus.SERVICE_UNAVAILABLE.value().toString(),              // 503
                                    HttpStatus.GATEWAY_TIMEOUT.value().toString(),                  // 504
                                    HttpStatus.HTTP_VERSION_NOT_SUPPORTED.value().toString(),       // 505
                                    HttpStatus.VARIANT_ALSO_NEGOTIATES.value().toString(),          // 506
                                    HttpStatus.INSUFFICIENT_STORAGE.value().toString(),             // 507
                                    HttpStatus.LOOP_DETECTED.value().toString(),                    // 508
                                    HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.value().toString(),         // 509
                                    HttpStatus.NOT_EXTENDED.value().toString(),                     // 510
                                    HttpStatus.NETWORK_AUTHENTICATION_REQUIRED.value().toString()   // 511
                                )
                            )
                        }

                        // time limiter configuration
                        f.filter { exchange, chain ->
                            val startTime = System.currentTimeMillis()
                            val timeLimiter = timeLimiterRegistry.timeLimiter("resourceServerTimeLimiter")
                            val futureSupplier = Supplier { chain.filter(exchange).toFuture() }
                            val decoratedCallable = timeLimiter.decorateFutureSupplier(futureSupplier)
                            val result = Mono.fromCallable(decoratedCallable)
                            return@filter result.doOnTerminate {
                                val endTime = System.currentTimeMillis()
                                println("Request took ${endTime - startTime} ms")
                            }
                        }

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
                            retryConfig.setStatuses(
                                HttpStatus.INTERNAL_SERVER_ERROR,           // 500
                                HttpStatus.NOT_IMPLEMENTED,                 // 501
                                HttpStatus.BAD_GATEWAY,                     // 502
                                HttpStatus.SERVICE_UNAVAILABLE,             // 503
                                HttpStatus.GATEWAY_TIMEOUT,                 // 504
                                HttpStatus.HTTP_VERSION_NOT_SUPPORTED,      // 505
                                HttpStatus.VARIANT_ALSO_NEGOTIATES,         // 506
                                HttpStatus.INSUFFICIENT_STORAGE,            // 507
                                HttpStatus.LOOP_DETECTED,                   // 508
                                HttpStatus.BANDWIDTH_LIMIT_EXCEEDED,        // 509
                                HttpStatus.NOT_EXTENDED,                    // 510
                                HttpStatus.NETWORK_AUTHENTICATION_REQUIRED  // 511
                            )
                            retryConfig.validate()
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