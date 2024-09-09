package com.frontiers.bff.routing.headers

import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HEADERS *******************************************************/
/**********************************************************************************************************************/

@Configuration
internal class HeaderFilterConfig {

    @Bean
    fun headerFilter(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            addCustomHeaders(exchange)
            chain.filter(exchange)
        }
    }

    private fun addCustomHeaders(exchange: ServerWebExchange) {
        val headers = exchange.response.headers
        headers.add(
            "X-Powered-By",
            "DreamStar Enterprises"
        )
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/