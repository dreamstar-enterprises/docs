package com.example.bff.gateway.circuitbreaker

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**********************************************************************************************************************/
/***************************************************** CIRCUIT BREAKER ************************************************/
/**********************************************************************************************************************/

@Configuration
class CircuitBreakerConfig {

    @Bean
    fun defaultCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory> {
        return Customizer<ReactiveResilience4JCircuitBreakerFactory> { factory ->
            factory.configureDefault { id ->
                Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                    .timeLimiterConfig(
                        TimeLimiterConfig.custom()
                            .timeoutDuration(
                                // 4.5 seconds
                                Duration.ofSeconds(4, 500_000_000L)
                            )
                            .build()
                    )
                    .build()
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/