package com.frontiers.bff.auth.csrf

import org.springframework.context.annotation.Configuration
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/************************************************** REQUEST MATCHER ***************************************************/
/**********************************************************************************************************************/

/**
 * Matcher implementation is designed to apply CSRF protection only to non-GET requests.
 * It will produce a match result for all requests EXCEPT FOR: GET requests
 */
@Configuration
internal class CsrfProtectionMatcher : ServerWebExchangeMatcher {
    override fun matches(exchange: ServerWebExchange): Mono<ServerWebExchangeMatcher.MatchResult>? {
        val method = exchange.request.method.name().uppercase()
        return if (method != "GET") {
            ServerWebExchangeMatcher.MatchResult.match()
        } else {
            Mono.empty()
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/