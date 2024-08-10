package com.example.bff.auth.csrf

import org.springframework.context.annotation.Configuration
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/************************************************** REQUEST MATCHER ***************************************************/
/**********************************************************************************************************************/

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