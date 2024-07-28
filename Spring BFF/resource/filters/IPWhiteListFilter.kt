package com.example.timesheetapi.auth.security.filters

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class IPWhiteListFilter : WebFilter {

    private val allowedIps = setOf(
        "127.0.0.1"
    )

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val remoteIp = exchange.request.remoteAddress?.address?.hostAddress

        return if (remoteIp in allowedIps) {
            chain.filter(exchange)
        } else {
            exchange.response.statusCode = HttpStatus.FORBIDDEN
            exchange.response.setComplete()
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/