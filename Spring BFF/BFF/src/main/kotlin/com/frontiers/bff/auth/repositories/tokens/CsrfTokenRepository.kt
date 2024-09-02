package com.frontiers.bff.auth.repositories.tokens

import com.frontiers.bff.props.CsrfProperties
import org.slf4j.LoggerFactory
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository
import org.springframework.stereotype.Repository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * The CSRF token is validated by comparing the token value from the request (header or parameter) with the token
 * value in the cookie. The server does not need to persist or store tokens beyond the duration of the request.
 * The actual comparison and validation are handled by CSRF protection filters or components within Spring Security,
 * ensuring the token matches without needing additional server-side storage.
 */

@Repository
internal class CustomServerCsrfTokenRepository(
    private val csrfProperties: CsrfProperties
) : ServerCsrfTokenRepository {

    private val logger = LoggerFactory.getLogger(CustomServerCsrfTokenRepository::class.java)

    private val delegate = CookieServerCsrfTokenRepository()

    init {
        delegate.setCookieName(csrfProperties.CSRF_COOKIE_NAME)
        delegate.setHeaderName(csrfProperties.CSRF_HEADER_NAME)
        delegate.setParameterName(csrfProperties.CSRF_PARAMETER_NAME)
        delegate.setCookieCustomizer { cookie ->
            cookie.httpOnly(csrfProperties.CSRF_COOKIE_HTTP_ONLY)
            cookie.secure(csrfProperties.CSRF_COOKIE_SECURE)
            cookie.sameSite(csrfProperties.CSRF_COOKIE_SAME_SITE)
            cookie.maxAge(csrfProperties.CSRF_COOKIE_MAX_AGE)
            cookie.path(csrfProperties.CSRF_COOKIE_PATH)
            cookie.domain(csrfProperties.CSRF_COOKIE_DOMAIN)
        }
    }

    override fun generateToken(exchange: ServerWebExchange): Mono<CsrfToken> {
        logger.info("Generating CSRF token")
        return delegate.generateToken(exchange)
    }

    override fun saveToken(exchange: ServerWebExchange, token: CsrfToken?): Mono<Void> {
        logger.info("Saving CSRF token")
        return delegate.saveToken(exchange, token)
    }

    override fun loadToken(exchange: ServerWebExchange): Mono<CsrfToken> {
        logger.info("Loading CSRF token")
        return delegate.loadToken(exchange)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/