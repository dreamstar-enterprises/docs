package com.frontiers.bff.auth.redirects

import com.frontiers.bff.props.OAuth2RedirectionProperties
import org.slf4j.LoggerFactory
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

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/client/SpringAddonsOauth2ServerRedirectStrategy.java

/**
 * A redirect strategy that might not actually redirect: the HTTP status is taken from
 * OAuth2RedirectionProperties. User-agents will auto redirect only if the status is in 3xx range.
 * If set to 2xx range (like OK, ACCEPTED, NO_CONTENT, ...), this gives single page and mobile applications a chance
 * to intercept the redirection and choose to follow the redirection (or not), with which agent, and to potentially
 * clear some headers - so a single page or mobile application can handle the redirection as it wishes
 * (change the user-agent, clear some headers, ...).
 */

/**
 * Provides a flexible way to handle HTTP redirects by configuring the response status code
 * and redirect location dynamically.
 */
internal open class OAuth2ServerRedirectStrategy(
    private var defaultStatus: HttpStatus,
    private val oauth2ServerRedirectionProperties: OAuth2RedirectionProperties
) : ServerRedirectStrategy {

    private val logger = LoggerFactory.getLogger(OAuth2ServerRedirectStrategy::class.java)

    override fun sendRedirect(exchange: ServerWebExchange, location: URI): Mono<Void> {
        logger.info("RUNNING SERVER REDIRECT STRATEGY: $defaultStatus")
        return Mono.fromRunnable {
            val response: ServerHttpResponse = exchange.response
            // get response status from the following header, otherwise use the passed in default
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