package com.frontiers.bff.auth.filters.csrf

import org.slf4j.LoggerFactory
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

/*
 * Ensures that the CSRF token is loaded and processed as part of the request lifecycle.
 * By accessing csrfToken.token, the filter causes any deferred operations related to the token to be executed
 * And then included as an exchange response cookie
 */
@Component
internal class CsrfWebCookieFilter : WebFilter {

    private val logger = LoggerFactory.getLogger(CsrfWebCookieFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return exchange.getAttributeOrDefault(CsrfToken::class.java.name, Mono.empty<CsrfToken>())
            .doOnNext { csrfToken ->
                // render the token value to a cookie by causing the deferred token to be loaded
                logger.info("Rendering CSRF token: ${csrfToken}")
                csrfToken.token
            }
            .then(chain.filter(exchange))
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/