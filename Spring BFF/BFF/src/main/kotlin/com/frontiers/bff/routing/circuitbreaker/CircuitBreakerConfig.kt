package com.example.bff.gateway.circuitbreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**********************************************************************************************************************/
/***************************************************** CIRCUIT BREAKER ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class CircuitBreakerConfig {

    // default configuraiton
    @Bean
    fun customCircuitBreakerConfig(): CircuitBreakerConfig {
        return CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .slidingWindowType(SlidingWindowType.TIME_BASED)
            .permittedNumberOfCallsInHalfOpenState(5)
            .failureRateThreshold(50F)
            .waitDurationInOpenState(Duration.ofSeconds(20))
            .maxWaitDurationInHalfOpenState(Duration.ofSeconds(15))
            .minimumNumberOfCalls(20)
            .slowCallRateThreshold(50F)
            .slowCallDurationThreshold(Duration.ofSeconds(7.5.toLong()))
            .writableStackTraceEnabled(true)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordExceptions(Exception::class.java)
            .ignoreExceptions(IllegalArgumentException::class.java)
            .build()
    }

    @Bean
    fun customCircuitBreakerRegistry(
        customCircuitBreakerConfig: CircuitBreakerConfig
    ): CircuitBreakerRegistry {
        return CircuitBreakerRegistry.of(customCircuitBreakerConfig)
    }

    @Bean
    fun resourceServerCircuitBreaker(
        circuitBreakerRegistry: CircuitBreakerRegistry,
        customCircuitBreakerConfig: CircuitBreakerConfig
    ): CircuitBreaker {
        return circuitBreakerRegistry.circuitBreaker("resourceServerCircuitBreaker", customCircuitBreakerConfig)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/