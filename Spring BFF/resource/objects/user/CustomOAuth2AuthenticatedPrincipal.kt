package com.example.timesheetapi.auth.security.objects.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

/**********************************************************************************************************************/
/********************************************** USER / PRINCIPAL OBJECT ************************************************/
/**********************************************************************************************************************/

internal class CustomOAuth2AuthenticatedPrincipal(
    private val username: String,
    private val authorities: Collection<GrantedAuthority?>,
    private val attributes: Map<String, Any>
) : OAuth2AuthenticatedPrincipal {

    override fun getName(): String {
        return username
    }

    override fun getAttributes(): Map<String, Any> {
        return attributes
    }

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return authorities
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/