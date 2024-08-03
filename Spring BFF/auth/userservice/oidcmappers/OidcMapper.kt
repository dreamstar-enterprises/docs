package com.example.authorizationserver.auth.userservice.oidcmappers

import com.example.authorizationserver.api.entities.user.UserEntity
import com.example.authorizationserver.auth.objects.user.CustomOidcUser
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser

/**********************************************************************************************************************/
/************************************************** MAPPER INTERFACE **************************************************/
/**********************************************************************************************************************/

internal interface OidcUserMapper {

    fun map(oidcUserRequest: OidcUser): CustomOidcUser
    fun map(idToken: OidcIdToken, userInfo: OidcUserInfo?, userEntity: UserEntity?): CustomOidcUser

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/