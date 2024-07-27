package com.example.gateway.auth.filters

import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

// needed for SPAs according to this:
// https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa

@Component
internal class CsrfCookieFilter() : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {

        val csrfTokenMono: Mono<CsrfToken>? = exchange.getAttribute(CsrfToken::class.java.name)
        return csrfTokenMono
            ?.flatMap { csrfToken ->
                // render the token value to a cookie by causing the deferred token to be loaded
                csrfToken.token

                // proceed with the request and manipulate the response if needed
                chain.filter(exchange)
            } ?: chain.filter(exchange)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/