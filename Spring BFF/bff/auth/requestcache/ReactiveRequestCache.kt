package com.example.bff.auth.requestcache

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.web.server.savedrequest.ServerRequestCache
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/******************************************************* REQUEST CACHE ************************************************/
/**********************************************************************************************************************/

@Component
internal class ReactiveRequestCache : ServerRequestCache {

    private val webSessionServerRequestCache = WebSessionServerRequestCache()

    // allows selective use of save request so that only those with 'redirect_uri' parameter are cached
    private fun shouldSaveRequest(exchange: ServerWebExchange): Boolean {
        val request = exchange.request
        return request.queryParams.containsKey("redirect_uri")
    }

    override fun saveRequest(exchange: ServerWebExchange): Mono<Void> {
        if (shouldSaveRequest(exchange)) {
            println("Saving request for ${exchange.request.uri}")
            return webSessionServerRequestCache.saveRequest(exchange)
        } else {
            println("Not saving request for ${exchange.request.uri}")
            return Mono.empty()
        }
    }

    override fun getRedirectUri(exchange: ServerWebExchange): Mono<URI> {
        println("Getting redirect URI for ${exchange.request.uri}")
        return webSessionServerRequestCache.getRedirectUri(exchange)
    }

    override fun removeMatchingRequest(exchange: ServerWebExchange): Mono<ServerHttpRequest> {
        println("Removing matching request for ${exchange.request.uri}")
        return webSessionServerRequestCache.removeMatchingRequest(exchange)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/