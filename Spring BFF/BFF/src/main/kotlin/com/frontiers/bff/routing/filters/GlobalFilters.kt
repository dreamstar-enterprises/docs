package com.frontiers.bff.routing.filters

import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.factory.SaveSessionGatewayFilterFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Configuration
internal class DedupeResponseFilterConfig {

    @Bean
    fun dedupeResponseHeaderFilter(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            chain.filter(exchange).then(
                Mono.fromRunnable {
                    val headers = exchange.response.headers
                    headers.dedupeResponseHeader(
                        "Access-Control-Allow-Credentials",
                        "RETAIN_UNIQUE"
                    )
                    headers.dedupeResponseHeader(
                        "Access-Control-Allow-Origin",
                        "RETAIN_UNIQUE"
                    )
                }
            )
        }
    }
}

private fun HttpHeaders.dedupeResponseHeader(headerName: String, strategy: String) {
    val headerValues = this[headerName] ?: return

    val uniqueValues = when (strategy) {
        "RETAIN_UNIQUE" -> headerValues.toSet().toList()
        else -> headerValues
    }

    this[headerName] = uniqueValues
}

@Configuration
internal class SaveSessionFilterConfig(
    private val saveSessionGatewayFilterFactory: SaveSessionGatewayFilterFactory
) {

    @Bean
    fun saveSessionGlobalFilter(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            saveSessionGatewayFilterFactory.apply {
                // optionally configure futher if needed
            }.filter(exchange, chain)
        }
    }
}

@Configuration
internal class CustomHeaderFilterConfig {

    @Bean
    fun customHeaderFilter(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            chain.filter(exchange).then(
                Mono.fromRunnable {
                    exchange.response.headers.add(
                        "X-Powered-By",
                        "DreamStar Enterprises"
                    )
                }
            )
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/