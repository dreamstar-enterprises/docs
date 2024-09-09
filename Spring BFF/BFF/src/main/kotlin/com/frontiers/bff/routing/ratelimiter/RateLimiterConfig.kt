package com.frontiers.bff.routing.ratelimiter

import com.frontiers.bff.props.SessionProperties
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/**************************************************** RATE LIMITER ****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RateLimiterConfig(
    private val requestRateLimiterGatewayFilterFactory: RequestRateLimiterGatewayFilterFactory,
    private val sessionProperties: SessionProperties
) {

    private val logger = LoggerFactory.getLogger(RateLimiterConfig::class.java)

    @Bean
    fun requestRateLimiterGatewayFilterFactory(): GlobalFilter {

        // create the rate limiter filter with the desired configuration
        val rateLimiterConfig = RequestRateLimiterGatewayFilterFactory.Config().apply {
            rateLimiter = redisRateLimiter()
            keyResolver = defaultKeyResolver()
            denyEmptyKey = true
            statusCode = HttpStatus.TOO_MANY_REQUESTS
            emptyKeyStatus = HttpStatus.BAD_REQUEST.name
        }

        val rateLimiterFilter = requestRateLimiterGatewayFilterFactory.apply(rateLimiterConfig)

        return GlobalFilter { exchange: ServerWebExchange, chain ->
            // log the incoming request URI
            logger.info("Processing request: ${exchange.request.uri}")

            rateLimiterFilter.filter(exchange, chain)
                .doOnSubscribe {
                    logger.info("Rate limiter filter applied to request: ${exchange.request.uri}")
                }
                .doOnSuccess {
                    logger.info("Request successfully processed with rate limiting for: ${exchange.request.uri}")
                }
                .doOnError { throwable ->
                    logger.error("Error processing request with rate limiting for: ${exchange.request.uri}", throwable)
                }
                .then(Mono.fromRunnable {
                    // optionally, add more logging here if needed
                })
        }
    }

    private fun redisRateLimiter(): RedisRateLimiter {
        return RedisRateLimiter(10, 20)
    }

    private fun defaultKeyResolver(): KeyResolver {
        return KeyResolver { exchange: ServerWebExchange ->
            val sessionId = exchange.request.cookies[sessionProperties.SESSION_COOKIE_NAME]?.first()?.value
            Mono.justOrEmpty(sessionId)
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/