package com.example.bff.auth.repositories

import org.springframework.context.annotation.Configuration
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
internal class CustomServerCsrfTokenRepository() : ServerCsrfTokenRepository {

    companion object {
        private const val CSRF_COOKIE_NAME = "XSRF-TOKEN"
        private const val COOKIE_PATH = "/"
        private const val COOKIE_SAME_SITE = "Strict"
    }

    private fun createCookie(token: CsrfToken?): ResponseCookie {
        return ResponseCookie.from(CSRF_COOKIE_NAME, token?.token ?: "")
            .httpOnly(false)
            .maxAge(-1) // session-based cookie
            .path(COOKIE_PATH)
            .sameSite(COOKIE_SAME_SITE) // set SameSite attribute to Strict
            .secure(true) // ensure the cookie is secure
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