package com.example.bff.auth.filters

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class PostLoginUriFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request

        // extract URIs from headers or request parameters
        val successUri = request.headers.getFirst("X-POST-LOGIN-SUCCESS-URI")
            ?: request.queryParams.getFirst("post_login_success_uri")
        val failureUri = request.headers.getFirst("X-POST-LOGIN-FAILURE-URI")
            ?: request.queryParams.getFirst("post_login_failure_uri")

        // save URIs in session if they are present
        return exchange.session.flatMap { session ->

            // create a new session with updated attributes
            val updatedSession = session.apply {
                successUri?.let { this.attributes["postLoginSuccessUri"] = it }
                failureUri?.let { this.attributes["postLoginFailureUri"] = it }
            }

            // continue the filter chain with the updated session
            chain.filter(exchange)
                .contextWrite { context -> context.put(WebSession::class.java, updatedSession) }
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/