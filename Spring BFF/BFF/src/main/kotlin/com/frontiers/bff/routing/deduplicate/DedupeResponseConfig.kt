package com.frontiers.bff.routing.deduplicate

import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/*************************************************** DEDUPE RESPONSE **************************************************/
/**********************************************************************************************************************/

@Configuration
internal class DedupeResponseConfig {

    @Bean
    fun dedupeResponseHeaderFilter(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            val headers = exchange.response.headers
            dedupeHeaders(headers)
            chain.filter(exchange)
        }
    }

    private fun dedupeHeaders(headers: HttpHeaders) {
        // Deduplicate specific response headers
        headers.dedupeResponseHeader(
            "Access-Control-Allow-Credentials",
            "RETAIN_UNIQUE"
        )
        headers.dedupeResponseHeader(
            "Access-Control-Allow-Origin",
            "RETAIN_UNIQUE"
        )
    }

    private fun HttpHeaders.dedupeResponseHeader(headerName: String, strategy: String) {
        val headerValues = this[headerName] ?: return

        val uniqueValues = when (strategy) {
            "RETAIN_UNIQUE" -> headerValues.toSet().toList()
            else -> headerValues
        }

        this[headerName] = uniqueValues
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/