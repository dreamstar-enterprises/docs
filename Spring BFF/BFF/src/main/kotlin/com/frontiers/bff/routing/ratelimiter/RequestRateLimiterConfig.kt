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
internal class RequestRateLimiterConfig(
    private val requestRateLimiterGatewayFilterFactory: RequestRateLimiterGatewayFilterFactory,
    private val redisRateLimiter: RedisRateLimiter,
    private val defaultKeyResolver: KeyResolver
) {

    private val logger = LoggerFactory.getLogger(RequestRateLimiterConfig::class.java)

    @Bean
    fun requestRateLimiter(): GlobalFilter {

        // rate limiter filter
        val rateLimiterConfig = RequestRateLimiterGatewayFilterFactory.Config().apply {
            rateLimiter = redisRateLimiter
            keyResolver = defaultKeyResolver
            denyEmptyKey = true
            statusCode = HttpStatus.TOO_MANY_REQUESTS
            emptyKeyStatus = HttpStatus.BAD_REQUEST.name
        }

        val rateLimiterFilter = requestRateLimiterGatewayFilterFactory.apply(rateLimiterConfig)

        return GlobalFilter { exchange, chain ->
            logger.info("Processing request: ${exchange.request.uri}")
            rateLimiterFilter.filter(exchange, chain)
        }
    }
}


@Configuration
internal class RedisRateLimiterConfig(
    private val sessionProperties: SessionProperties
) {

    private val logger = LoggerFactory.getLogger(RedisRateLimiterConfig::class.java)

    @Bean
    fun redisRateLimiter(): RedisRateLimiter {
        return RedisRateLimiter(10, 20, 1)
    }

    @Bean
    fun defaultKeyResolver(): KeyResolver {
        return KeyResolver { exchange: ServerWebExchange ->
            val sessionId = exchange.request.cookies[sessionProperties.SESSION_COOKIE_NAME]?.first()?.value
            logger.info("Resolved session ID for Rate Limiting: $sessionId")
            Mono.justOrEmpty(sessionId)
        }
    }

}


/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/