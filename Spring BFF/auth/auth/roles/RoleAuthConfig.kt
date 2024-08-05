package com.example.authorizationserver.auth.roles

import com.example.authorizationserver.api.enums.AuthTypes
import com.example.authorizationserver.api.enums.RoleTypes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RoleAuthConfig {

    // data class - authorities
    data class Auth(val name: AuthTypes)

    // data class - roles
    data class Role(
        val name: RoleTypes,
        val auths: Collection<Auth>
    )

    // authorities
    final val postAuth = Auth(AuthTypes.AUTH_POST)
    final val getAuth = Auth(AuthTypes.AUTH_GET)
    final val putAuth = Auth(AuthTypes.AUTH_PUT)
    final val patchAuth = Auth(AuthTypes.AUTH_PATCH)
    final val deleteAuth = Auth(AuthTypes.AUTH_DELETE)

    // define a role-authority mapping
    val roleAuthorityMapping = mapOf(
        RoleTypes.ROLE_USER to listOf(postAuth, getAuth, putAuth, patchAuth, deleteAuth),
        RoleTypes.ROLE_ADMIN to listOf(postAuth, getAuth, putAuth, patchAuth, deleteAuth)
    )

    @Bean
    // generate authorities from roles
    fun getAuthorities(roles: List<RoleTypes?>): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()
        for (role in roles) {
            // add the role itself as a GrantedAuthority
            if (role?.name != null && role.name.isNotEmpty()) {
                authorities.add(SimpleGrantedAuthority(role.name))
            } else {
                // handle cases where role name is null or empty
                println("Warning: Role name is null or empty for role $role")
            }

            // add all the auths associated with the role as GrantedAuthorities
            roleAuthorityMapping.get(role)?.let { auths ->
                for (auth in auths) {
                    val authorityName = auth.name.toDisplayName()
                    if (authorityName.isNotEmpty()) {
                        authorities.add(SimpleGrantedAuthority(authorityName))
                    } else {
                        // handle cases where authority name is null or empty
                        println("Warning: Authority name is null or empty for authority $auth")
                    }
                }
            }
        }
        return authorities
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/