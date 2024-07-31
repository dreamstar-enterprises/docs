package com.example.authorizationserver.auth.security.userservice

import com.example.authorizationserver.api.entities.user.UserEntity
import com.example.authorizationserver.auth.security.userservice.oidcmappers.OidcUserMapper
import com.example.authorizationserver.auth.security.virtualthreads.VirtualThreadManager
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

/**********************************************************************************************************************/
/********************************************** SERVICE IMPLEMENTATION ************************************************/
/**********************************************************************************************************************/

// more here:
// https://medium.com/@d.snezhinskiy/building-sso-based-on-spring-authorization-server-part-3-of-3-b0b31feb2b6e


/**********************************************************************************************************************/
/* O.I.D.C. USER DETAILS - SERVICE  */
/**********************************************************************************************************************/

@Service
internal class CustomOidcUserDetailsService(
    private val mappers: Map<String, OidcUserMapper>,
    private val virtualThreadManager: VirtualThreadManager,
) : OidcUserService() {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {

        // get oidc request
        val oidcUserRequest: OidcUser = super.loadUser(userRequest) as OidcUser

        // get registration id from oidc request
        val registrationId: String = userRequest.clientRegistration.registrationId

        // check mapper exists, otherwise throw error
        require(mappers.containsKey(registrationId)) { "No mapper defined for such registrationId" }

        // get appropriate mapper
        val mapper: OidcUserMapper = mappers.getValue(registrationId)

        // get userEntity (optional) by email
        val email: String = userRequest.idToken.email
        val userEntity: UserEntity? = virtualThreadManager.fetchUserEntity(email)

        // return appropriate OidcUserDetails object
        return userEntity?.let {
            mapper.map(oidcUserRequest.idToken, oidcUserRequest.userInfo, userEntity)
        } ?: mapper.map(oidcUserRequest)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/