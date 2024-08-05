package com.example.authorizationserver.auth.objects.authentication

import com.example.authorizationserver.auth.objects.user.DocDbUser
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails

/**********************************************************************************************************************/
/*********************************************** AUTHENTICATION OBJECT ************************************************/
/**********************************************************************************************************************/

class DocDbUserAuthentication @JsonCreator constructor(
    @JsonProperty("docDBUser") private val docDBUser: DocDbUser,
    @JsonProperty("password") private val password: String?,
    @JsonProperty("authorities") authorities: Collection<GrantedAuthority?>,
    @JsonProperty("isAuthenticated") isAuthenticated: Boolean
) : AbstractAuthenticationToken(authorities) {

    init {
        // set the authentication status based on the input
        super.setAuthenticated(isAuthenticated)
    }

    override fun getCredentials(): String? {
        return this.password
    }

    override fun getPrincipal(): UserDetails {
        return this.docDBUser
    }

    override fun setAuthenticated(authenticated: Boolean) {
        throw RuntimeException("THE AUTHENTICATION STATUS CANNOT BE CHANGED")
    }

    // factory methods for creating different instances of the Authentication object
    companion object {
        fun authenticated(docDBUser: DocDbUser): DocDbUserAuthentication {
            return DocDbUserAuthentication(
                docDBUser,
                null,
                docDBUser.authorities,
                true
            )
        }

        fun unauthenticated(docDBUser: DocDbUser, password: String): DocDbUserAuthentication {
            return DocDbUserAuthentication(
                docDBUser,
                password,
                docDBUser.authorities,
                false
            )
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/