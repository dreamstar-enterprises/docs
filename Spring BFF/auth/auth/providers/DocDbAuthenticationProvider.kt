package com.example.authorizationserver.auth.providers

import com.example.authorizationserver.auth.objects.authentication.DocDbUserAuthentication
import com.example.authorizationserver.auth.objects.user.DocDbUser
import com.example.authorizationserver.auth.userservice.docdb.DocDbUserDetailsManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.GeneralSecurityException

/**********************************************************************************************************************/
/********************************************** AUTHENTICATION PROVIDER ***********************************************/
/**********************************************************************************************************************/

@Component
internal class DocDbAuthenticationProvider(
    private val docDbUserDetailsManager: DocDbUserDetailsManager,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.name
        val password = authentication.credentials.toString()

        val user = try {
            docDbUserDetailsManager.loadUserByUsername(username)
        } catch (ex: UsernameNotFoundException) {
            throw UsernameNotFoundException("User not found: $username", ex)
        } catch (ex: Exception) {
            throw GeneralSecurityException("Unknown error", ex)
        }

        // check password validity
        if (!passwordEncoder.matches(password, user.password)) {
            throw BadCredentialsException("Invalid username or password")
        }

        // return authentication object (authenticated)
        return DocDbUserAuthentication.authenticated(user as DocDbUser)

    }


    // this authentication provider works on AuthenticationObjects of type: UserAuthentication
    override fun supports(authentication: Class<*>): Boolean {
        return DocDbUserAuthentication::class.java.isAssignableFrom(authentication)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/