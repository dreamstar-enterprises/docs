package com.example.authorizationserver.auth.security.converters

import com.example.authorizationserver.auth.security.objects.authentication.DocDbUserAuthentication
import com.example.authorizationserver.auth.security.objects.user.DocDbUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.AuthenticationConverter

/**********************************************************************************************************************/
/********************************************* AUTHENTICATION CONVERTER ***********************************************/
/**********************************************************************************************************************/

/* Authentication Converter (when credentials are passed in as plain JSON) */
internal class BasicJSONConverter : AuthenticationConverter {

    override fun convert(request: HttpServletRequest): Authentication? {
        val username = request.getParameter("username")
        val password = request.getParameter("password")

        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return null
        }

        // return authentication object (un-authenticated)
        return DocDbUserAuthentication.unauthenticated(
            DocDbUser(
                null,
                username,
                password,
                emptyList<GrantedAuthority>(),
                false,
                false,
                false,
                false
            ), password)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/