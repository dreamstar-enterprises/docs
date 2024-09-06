package com.frontiers.bff.auth.requestcache

import com.frontiers.bff.auth.repositories.tokens.CustomServerCsrfTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache
import org.springframework.security.web.server.savedrequest.ServerRequestCache
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/******************************************************* REQUEST CACHE ************************************************/
/**********************************************************************************************************************/

/**
 * Saves (caches) the request uri, before any redirect uris, to the user's session, in case needed
 */
@Component
internal class ReactiveRequestCache() : ServerRequestCache {

    private val logger = LoggerFactory.getLogger(CustomServerCsrfTokenRepository::class.java)

    // stateless to reduce load on redis - no need to persist request state to session as an attribute
    private val delegate = NoOpServerRequestCache.getInstance()

    override fun saveRequest(exchange: ServerWebExchange): Mono<Void> {
        logger.info("Saving request for ${exchange.request.uri}")
        return delegate.saveRequest(exchange)
    }

    override fun getRedirectUri(exchange: ServerWebExchange): Mono<URI> {
        logger.info("Getting redirect URI for ${exchange.request.uri}")
        return delegate.getRedirectUri(exchange)
    }

    override fun removeMatchingRequest(exchange: ServerWebExchange): Mono<ServerHttpRequest> {
        logger.info("Removing matching request for ${exchange.request.uri}")
        return delegate.removeMatchingRequest(exchange)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/