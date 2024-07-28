package com.example.authorizationserver.auth.security.handlers.not_used

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URISyntaxException


/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class AuthorizationEndPointSuccessHandler
    : AuthenticationSuccessHandler {

    private val redirectStrategy = DefaultRedirectStrategy()
    private val allowedHosts = setOf("www.manning.com", "another-trusted-site.com")

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication
    ) {
        val result = authentication as OAuth2AuthorizationCodeRequestAuthenticationToken
        val code = result.authorizationCode!!.tokenValue

        val redirectUri = buildSecureRedirectUri(result.redirectUri, code, result.state)

        println(redirectUri)
        println("END POINT HANDLING")

        if (redirectUri != null) {
            // invalidate the session if required
            request?.session?.invalidate()

            // redirect to the secure URI
            redirectStrategy.sendRedirect(request, response, redirectUri)
        } else {
            // Handle invalid redirect URI scenario
            response?.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect URI")
        }
    }

    private fun buildSecureRedirectUri(redirectUri: String?, code: String, state: String?): String? {
        if (redirectUri == null || !isValidUri(redirectUri)) {
            return null
        }

        val builder = UriComponentsBuilder
            .fromUriString(redirectUri)
            .queryParam(OAuth2ParameterNames.CODE, code)

        if (StringUtils.hasText(state)) {
            builder.queryParam(OAuth2ParameterNames.STATE, state)
        }

        return builder.build().toUriString()
    }

    private fun isValidUri(uri: String): Boolean {
        return try {
            val parsedUri = URI(uri)
            val host = parsedUri.host
            val userInfo = parsedUri.userInfo

            // check if the host is in the allowed list and user info is not used
            host != null && allowedHosts.contains(host) && userInfo == null &&
                    (parsedUri.scheme == "http" || parsedUri.scheme == "https")
        } catch (e: URISyntaxException) {
            false
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/