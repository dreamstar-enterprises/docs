package com.example.authorizationserver.auth.repositories.tokens

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.stereotype.Repository

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Repository
internal class CustomServletCsrfTokenRepository : CsrfTokenRepository {

    companion object {
        private const val CSRF_COOKIE_NAME = "XSRF-AUTH-TOKEN"
        private const val CSRF_HEADER_NAME = "X-XSRF-AUTH-TOKEN"
        private const val CSRF_PARAMETER_NAME = "_csrf"
        private const val COOKIE_PATH = "/"
        private const val COOKIE_SAME_SITE = "Strict"
    }
    private val cookieCsrfTokenRepository = CookieCsrfTokenRepository()

    init {
        cookieCsrfTokenRepository.setCookieName(CSRF_COOKIE_NAME)
        cookieCsrfTokenRepository.setHeaderName(CSRF_HEADER_NAME)
        cookieCsrfTokenRepository.setParameterName(CSRF_PARAMETER_NAME)
        cookieCsrfTokenRepository.setCookieCustomizer { cookie ->
            cookie.httpOnly(true)
            cookie.secure(false) // scope is not just on secure connections
            cookie.sameSite(COOKIE_SAME_SITE)
            cookie.maxAge(-1)
            cookie.path(COOKIE_PATH)
        }
    }

    override fun generateToken(request: HttpServletRequest): CsrfToken {
        println("Generating CSRF Token")
        return cookieCsrfTokenRepository.generateToken(request)
    }

    override fun saveToken(
        token: CsrfToken?,
        request: HttpServletRequest,
        response: HttpServletResponse?
    ) {
        println("Saving CSRF Token: ${token?.token}")
        cookieCsrfTokenRepository.saveToken(token, request, response)
    }

    override fun loadToken(request: HttpServletRequest): CsrfToken? {
        println("Loaded CSRF Token: ${cookieCsrfTokenRepository.loadToken(request)?.token}")
        return cookieCsrfTokenRepository.loadToken(request)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/