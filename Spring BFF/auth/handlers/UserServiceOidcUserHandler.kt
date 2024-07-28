package com.example.authorizationserver.auth.security.handlers

import com.example.authorizationserver.api.enums.RoleTypes
import com.example.authorizationserver.auth.security.objects.user.CustomOidcUser
import com.example.authorizationserver.auth.security.roles.RoleAuthConfig
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component
import java.util.function.Consumer

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class UserServiceOidcUserHandler() : Consumer<OidcUser> {

    override fun accept(user: OidcUser) {

        // capture user in an external data base on first authentication
        val oidcUser = user as CustomOidcUser

        // if oicdUser id is null - attempt to add user to database
        if (oidcUser.getUserId() == null) {
            val grantedAuthorities = oidcUser.authorities as MutableCollection<GrantedAuthority>

            // create userEntity from oidcUserDetails object
            val userEntity = oidcUser.toInstantUserEntity()

            // assign default role
            val defaultRole = RoleTypes.ROLE_USER
            userEntity.securityInformation.roles = listOf(defaultRole)

            // save userEntity to database
            // userService.save(localUser)

            // get authorities
            if (userEntity.securityInformation.roles.isNotEmpty()) {
                val authorities = userEntity.securityInformation.let { RoleAuthConfig().getAuthorities(it.roles) }
                grantedAuthorities.addAll(authorities)
            }

            // assign newly created userEntity id back to oidcUserDetails object
            oidcUser.setUserId(userEntity._id.toString())
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/