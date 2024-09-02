package com.frontiers.bff.auth.handlers

import com.frontiers.bff.auth.sessions.SessionControl
import com.frontiers.bff.props.CsrfProperties
import com.frontiers.bff.props.SessionProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

/**
 * On logout invalidates the local session (which also has other security objects: e.g. security context, authorized clients)
 */
@Component
internal class SessionServerLogoutHandler(
    private val sessionControl: SessionControl,
    private val sessionProperties: SessionProperties,
    private val csrfProperties: CsrfProperties,
) : ServerLogoutHandler {

    private val logger = LoggerFactory.getLogger(SessionServerLogoutHandler::class.java)

//    private val indexed = ReactiveRedisSessionIndexer(redisOperations, this.namespace)

    override fun logout(exchange: WebFilterExchange, authentication: Authentication?): Mono<Void> {
        return exchange.exchange.session
            .flatMap { session ->
                logger.info("Logging out: Invalidating User Session: ${session.id}")

                val response = exchange.exchange.response

                // invalidate and delete session
                sessionControl.invalidateSession(session.id)
                    .then(Mono.fromRunnable {

                        logger.info("Deleting Session Cookie: ${sessionProperties.SESSION_COOKIE_NAME}")

                        // delete the session cookie
                        val sessionCookie = ResponseCookie.from(sessionProperties.SESSION_COOKIE_NAME)
                        .maxAge(0)
                        .httpOnly(sessionProperties.SESSION_COOKIE_HTTP_ONLY)
                        .secure(sessionProperties.SESSION_COOKIE_SECURE)
                        .sameSite(sessionProperties.SESSION_COOKIE_SAME_SITE)
                        .path(sessionProperties.SESSION_COOKIE_PATH)
                        .domain(sessionProperties.SESSION_COOKIE_DOMAIN)
                        .build()
                        response.headers.add(
                            HttpHeaders.SET_COOKIE,
                            sessionCookie.toString()
                        )

                        logger.info("Deleting Session Cookie: ${csrfProperties.CSRF_COOKIE_NAME}")

                        // delete the CSRF cookie
                        val csrfCookie = ResponseCookie.from(csrfProperties.CSRF_COOKIE_NAME)
                        .maxAge(0)
                        .httpOnly(csrfProperties.CSRF_COOKIE_HTTP_ONLY)
                        .secure(csrfProperties.CSRF_COOKIE_SECURE)
                        .sameSite(csrfProperties.CSRF_COOKIE_SAME_SITE)
                        .path(csrfProperties.CSRF_COOKIE_PATH)
                        .domain(csrfProperties.CSRF_COOKIE_DOMAIN)
                        .build()
                        response.headers.add(
                            HttpHeaders.SET_COOKIE,
                            csrfCookie.toString()
                        )
                    })
            }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/