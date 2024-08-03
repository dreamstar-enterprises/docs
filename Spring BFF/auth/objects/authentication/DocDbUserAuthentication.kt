package com.example.authorizationserver.auth.objects.authentication

import com.example.authorizationserver.auth.objects.user.DocDbUser
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails

/**********************************************************************************************************************/
/*********************************************** AUTHENTICATION OBJECT ************************************************/
/**********************************************************************************************************************/

internal class DocDbUserAuthentication : AbstractAuthenticationToken {
    private val docDBUser: DocDbUser?
    private val password: String?

    // constructor for User - authenticated
    private constructor(
        docDBUser: DocDbUser
    ) : super(docDBUser.authorities) {
        this.docDBUser = docDBUser.copy(password = null) // password is null
        this.password = null // password is null
        super.eraseCredentials() // erase credentials
        super.setAuthenticated(true)
    }

    // constructor for User - un-authenticated
    private constructor(
        docDBUser: DocDbUser,
        password: String
    ) : super(docDBUser.authorities) {
        this.docDBUser = docDBUser
        this.password = password
        super.setAuthenticated(false)
    }

    override fun getCredentials(): String? {
        return this.password
    }

    override fun getPrincipal(): UserDetails? {
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
            )
        }

        fun unauthenticated(docDBUser: DocDbUser, password: String): DocDbUserAuthentication {
            return DocDbUserAuthentication(
                docDBUser,
                password
            )
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/