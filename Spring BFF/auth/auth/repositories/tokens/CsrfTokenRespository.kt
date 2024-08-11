package com.example.authorizationserver.auth.repositories.tokens

import com.example.authorizationserver.props.CsrfProperties
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
internal class CustomServletCsrfTokenRepository(
    private val csrfProperties: CsrfProperties
) : CsrfTokenRepository {

    private val cookieCsrfTokenRepository = CookieCsrfTokenRepository()

    init {
        cookieCsrfTokenRepository.setCookieName(csrfProperties.CSRF_COOKIE_NAME)
        cookieCsrfTokenRepository.setHeaderName(csrfProperties.CSRF_HEADER_NAME)
        cookieCsrfTokenRepository.setParameterName(csrfProperties.CSRF_PARAMETER_NAME)
        cookieCsrfTokenRepository.setCookieCustomizer { cookie ->
            cookie.httpOnly(csrfProperties.CSRF_COOKIE_HTTP_ONLY)
            cookie.secure(csrfProperties.CSRF_COOKIE_SECURE)
            cookie.sameSite(csrfProperties.CSRF_COOKIE_SAME_SITE)
            cookie.maxAge(csrfProperties.CSRF_COOKIE_MAX_AGE)
            cookie.path(csrfProperties.CSRF_COOKIE_PATH)
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