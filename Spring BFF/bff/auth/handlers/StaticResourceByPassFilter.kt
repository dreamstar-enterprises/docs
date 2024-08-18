package com.example.bff.auth.handlers

import com.example.bff.auth.BffSecurityIgnoreConfig
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@Component
internal class StaticResourceBypassFilter(
    private val uriEndPointFilter: BffSecurityIgnoreConfig
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestPath = exchange.request.uri.path

        // check if the request path matches any of the static resource patterns
        if (uriEndPointFilter.shouldSkipStaticResources(requestPath)) {
            // skip further processing and directly return the response
            return exchange.response.setComplete()
        }

        // proceed with the security filters for non-static resources
        return chain.filter(exchange)
    }
}