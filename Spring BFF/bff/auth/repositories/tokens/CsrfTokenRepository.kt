package com.example.bff.auth.repositories.tokens

import com.example.bff.props.CsrfProperties
import org.springframework.http.ResponseCookie
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository
import org.springframework.stereotype.Repository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Repository
internal class CustomServerCsrfTokenRepository(
    private val csrfProperties: CsrfProperties
) : ServerCsrfTokenRepository {

    private val cookieServerCsrfTokenRepository = CookieServerCsrfTokenRepository()

    init {
        cookieServerCsrfTokenRepository.setCookieName(csrfProperties.CSRF_COOKIE_NAME)
        cookieServerCsrfTokenRepository.setCookieCustomizer { cookie ->
            cookie.httpOnly(csrfProperties.CSRF_COOKIE_HTTP_ONLY)
            cookie.secure(csrfProperties.CSRF_COOKIE_SECURE)
            cookie.sameSite(csrfProperties.CSRF_COOKIE_SAME_SITE)
            cookie.maxAge(csrfProperties.CSRF_COOKIE_MAX_AGE)
            cookie.path(csrfProperties.CSRF_COOKIE_PATH)
        }
    }

    private fun createCookie(token: CsrfToken?): ResponseCookie {
        return ResponseCookie
            .from(csrfProperties.CSRF_COOKIE_NAME, token?.token ?: "")
            .httpOnly(csrfProperties.CSRF_COOKIE_HTTP_ONLY)
            .secure(csrfProperties.CSRF_COOKIE_SECURE)
            .sameSite(csrfProperties.CSRF_COOKIE_SAME_SITE)
            .maxAge(csrfProperties.CSRF_COOKIE_MAX_AGE)
            .path(csrfProperties.CSRF_COOKIE_PATH)
            .build()
    }

    override fun generateToken(exchange: ServerWebExchange): Mono<CsrfToken> {
        return CookieServerCsrfTokenRepository.withHttpOnlyFalse().generateToken(exchange)
    }

    override fun saveToken(exchange: ServerWebExchange, token: CsrfToken?): Mono<Void> {
        return Mono.fromRunnable {
            val cookie = createCookie(token)
            exchange.response.addCookie(cookie)
        }
    }

    override fun loadToken(exchange: ServerWebExchange): Mono<CsrfToken> {
        return CookieServerCsrfTokenRepository.withHttpOnlyFalse().loadToken(exchange)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/