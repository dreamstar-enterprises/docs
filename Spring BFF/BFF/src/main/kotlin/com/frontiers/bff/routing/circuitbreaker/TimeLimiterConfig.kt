package com.frontiers.bff.routing.circuitbreaker

import io.github.resilience4j.timelimiter.TimeLimiter
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**********************************************************************************************************************/
/**************************************************** RATE LIMITER ****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class TimeLimiterConfig {

    // default configuraiton
    @Bean
    fun customTimeLimiterConfig(): TimeLimiterConfig {
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10))
            .cancelRunningFuture(true)
            .build()
    }

    @Bean
    fun customTimeLimiterRegistry(
        customTimeLimiterConfig: TimeLimiterConfig
    ): TimeLimiterRegistry {
        return TimeLimiterRegistry.of(customTimeLimiterConfig)
    }

    @Bean
    fun resourceServerTimeLimiter(
        timeLimiterRegistry: TimeLimiterRegistry,
        customTimeLimiterConfig: TimeLimiterConfig
    ): TimeLimiter {
        return timeLimiterRegistry.timeLimiter("resourceServerTimeLimiter", customTimeLimiterConfig)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/