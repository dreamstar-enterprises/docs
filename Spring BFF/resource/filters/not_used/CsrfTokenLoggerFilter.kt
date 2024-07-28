package com.example.timesheetapi.auth.security.filters.not_used

import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.logging.Logger

/**********************************************************************************************************************/
/************************************************* CSRF LOGGING FILTER ************************************************/
/**********************************************************************************************************************/

@Component
class CsrfTokenLoggerFilter: WebFilter {

    private val logger: Logger = Logger.getLogger(CsrfTokenLoggerFilter::class.java.name)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val csrfTokenMono: Mono<CsrfToken>? = exchange.getAttribute(CsrfToken::class.java.name)

        return csrfTokenMono?.flatMap { token ->
            if (token != null) {
                logger.info("CSRF token ${token.token}")
            }
            chain.filter(exchange)
        } ?: chain.filter(exchange)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/