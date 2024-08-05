package com.example.authorizationserver.auth.userservice.oidc.oidcmappers

import com.example.authorizationserver.api.entities.user.UserEntity
import com.example.authorizationserver.auth.objects.user.CustomOidcUser
import com.example.authorizationserver.auth.roles.RoleAuthConfig
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/*************************************************** USER MAPPER ******************************************************/
/**********************************************************************************************************************/

// more here:
// https://medium.com/@d.snezhinskiy/building-sso-based-on-spring-authorization-server-part-3-of-3-b0b31feb2b6e


@Component("google")
internal class GoogleOidcUserMapper : OidcUserMapper {

    // if userEntity does not already exist
    override fun map(oidcUserRequest: OidcUser): CustomOidcUser {
        val oidcUser = CustomOidcUser(oidcUserRequest.authorities, oidcUserRequest.idToken, oidcUserRequest.userInfo)
        oidcUser.setUserId(null)
        oidcUser.username = oidcUser.email
        oidcUser.isAccountNonExpired = true
        oidcUser.isAccountNonLocked = true
        oidcUser.isCredentialsNonExpired = true
        oidcUser.isEnabled = true
        return oidcUser
    }

    // if userEntity already exists
    override fun map(idToken: OidcIdToken, userInfo: OidcUserInfo?, userEntity: UserEntity?): CustomOidcUser {

        // get authorities
        val authorities = userEntity?.securityInformation?.let { RoleAuthConfig().getAuthorities(it.roles) } ?: HashSet()

        // create custom claims
        val claims = mutableMapOf<String, Any>()

        // these get added as attributes to the OAuth2AuthenticationToken
        claims.putAll(idToken.claims)
        if (userEntity != null) {
            claims[StandardClaimNames.GIVEN_NAME] = userEntity.personalInformation.firstName
            claims[StandardClaimNames.MIDDLE_NAME] = userEntity.personalInformation.middleName ?: ""
            claims[StandardClaimNames.FAMILY_NAME] = userEntity.personalInformation.lastName
        }

        // create token
        val customIdToken = OidcIdToken(
            idToken.tokenValue, idToken.issuedAt, idToken.expiresAt, claims
        )

        // create oidcUserDetails object
        val oidcUser = CustomOidcUser(authorities, customIdToken, userInfo)
        oidcUser.setUserId( userEntity?._id.toString())
        oidcUser.username = userEntity?.contactInformation?.email ?: ""
        oidcUser.isAccountNonExpired = userEntity?.securityInformation?.isAccountNonExpired == true
        oidcUser.isAccountNonLocked = userEntity?.securityInformation?.isAccountNonLocked == true
        oidcUser.isCredentialsNonExpired = userEntity?.securityInformation?.isCredentialsNonExpired == true
        oidcUser.isEnabled = userEntity?.securityInformation?.isEnabled == true

        // return oidcUserDetails object
        return oidcUser
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/