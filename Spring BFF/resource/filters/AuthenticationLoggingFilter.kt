package com.example.timesheetapi.auth.security.filters

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class AuthenticationLoggingFilter : WebFilter {

    private val logger = LoggerFactory.getLogger(AuthenticationLoggingFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestId = exchange.request.headers.getFirst("Request-Id")

        return chain.filter(exchange)
            .doOnSuccess {
                logger.info("Completed processing request with id: $requestId")
            }
            .doOnError {
                logger.info("Error processing request with id: $requestId")
            }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/