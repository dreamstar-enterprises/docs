package com.example.timesheetapi.auth.security.objects.authentication

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

/**********************************************************************************************************************/
/*********************************************** AUTHENTICATION OBJECT ************************************************/
/**********************************************************************************************************************/

internal class JWTAuthentication(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>,
    private val customClaim: String
) : JwtAuthenticationToken(jwt, authorities) {

    fun getCustomClaim(): String {
        return customClaim
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/