package com.example.authorizationserver.auth.objects.user

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**********************************************************************************************************************/
/********************************************** USER / PRINCIPAL OBJECT ************************************************/
/**********************************************************************************************************************/

data class DocDbUser(
    @JsonProperty("userId") private val userId: String?,
    @JsonProperty("username") private val username: String,
    @JsonProperty("password") private val password: String?,
    @JsonProperty("authorities") private val authorities: Collection<GrantedAuthority?>,
    @JsonProperty("isAccountNonExpired") private val isAccountNonExpired: Boolean?,
    @JsonProperty("isAccountNonLocked") private val isAccountNonLocked: Boolean?,
    @JsonProperty("isCredentialsNonExpired") private val isCredentialsNonExpired: Boolean?,
    @JsonProperty("isEnabled") private val isEnabled: Boolean?
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