package com.example.authorizationserver.auth.security.objects.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**********************************************************************************************************************/
/********************************************** USER / PRINCIPAL OBJECT ************************************************/
/**********************************************************************************************************************/

internal data class DocDbUser(
    private val userId: String?,
    private val username: String,
    private val password: String?,
    private val authorities: Collection<GrantedAuthority?>,
    private val isAccountNonExpired: Boolean?,
    private val isAccountNonLocked: Boolean?,
    private val isCredentialsNonExpired: Boolean?,
    private val isEnabled: Boolean?
) : UserDetails {

    fun getUserId(): String? {
        return userId
    }

    override fun getUsername(): String {
        return username
    }

    override fun getPassword(): String? {
        return password
    }

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return authorities
    }

    override fun isAccountNonExpired(): Boolean {
        return isAccountNonExpired ?: false
    }

    override fun isAccountNonLocked(): Boolean {
        return isAccountNonLocked ?: false
    }

    override fun isCredentialsNonExpired(): Boolean {
        return isCredentialsNonExpired ?: false
    }

    override fun isEnabled(): Boolean {
        return isEnabled ?: false
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/