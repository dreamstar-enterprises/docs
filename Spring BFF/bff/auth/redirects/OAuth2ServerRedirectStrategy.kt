package com.example.bff.auth.redirects

import com.example.bff.props.OAuth2RedirectionProperties
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.web.server.ServerRedirectStrategy
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

/**********************************************************************************************************************/
/**************************************************** REDIRECTION STRATEGIES ******************************************/
/**********************************************************************************************************************/

/**
 * A redirect strategy that might not actually redirect: the HTTP status is taken from
 * oauth2-redirect-status properties. User-agents will auto redirect only if the status is in 3xx range.
 * This gives single page and mobile applications a chance to intercept the redirection and choose to follow the
 * redirection (or not), with which agent and potentially by clearing some headers. (like OK, ACCEPTED, NO_CONTENT, ...)
 * for single page and mobile applications to handle this redirection as it wishes (change the user-agent,
 * clear some headers, ...).
 */

internal open class OAuth2ServerRedirectStrategy(
    private var defaultStatus: HttpStatus
) : ServerRedirectStrategy {

    private val oauth2ServerRedirectionProperties = OAuth2RedirectionProperties()

    override fun sendRedirect(exchange: ServerWebExchange, location: URI): Mono<Void> {
        println("RUNNING REDIRECT STRATEGY: $defaultStatus")
        return Mono.fromRunnable {
            val response: ServerHttpResponse = exchange.response
            val status = Optional
                .ofNullable(
                    exchange.request.headers[oauth2ServerRedirectionProperties.RESPONSE_STATUS_HEADER]
                )
                .flatMap { it.stream().findAny() }
                .filter { it.isNotBlank() }
                .map { statusStr ->
                    try {
                        HttpStatus.valueOf(statusStr.toInt())
                    } catch (e: NumberFormatException) {
                        HttpStatus.valueOf(statusStr.uppercase())
                    }
                }
                .orElse(defaultStatus)

            response.statusCode = status
            response.headers.location = location
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/