package com.example.authorizationserver.auth.csrf

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.util.matcher.RequestMatcher

/**********************************************************************************************************************/
/************************************************** REQUEST MATCHER ***************************************************/
/**********************************************************************************************************************/

@Configuration
internal class CsrfProtectionMatcher: RequestMatcher {

    override fun matches(request: HttpServletRequest): Boolean {
        return !request.method.equals("GET", ignoreCase = true)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/