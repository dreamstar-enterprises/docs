package com.example.authorizationserver.auth.objects.user

import com.example.authorizationserver.api.entities.user.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser

/**********************************************************************************************************************/
/********************************************** USER / PRINCIPAL OBJECT ************************************************/
/**********************************************************************************************************************/

internal class CustomOidcUser : DefaultOidcUser, UserDetails {
    @JvmField
    var userId: String? = null
    @JvmField
    var username: String = ""
    private var authorities: Collection<GrantedAuthority> = HashSet()
    @JvmField
    var isAccountNonExpired: Boolean = false
    @JvmField
    var isAccountNonLocked: Boolean = false
    @JvmField
    var isCredentialsNonExpired: Boolean = false
    @JvmField
    var isEnabled: Boolean = false

    /** custom constructors **/
    constructor(authorities: Collection<GrantedAuthority>, idToken: OidcIdToken)
            : super(authorities.ifEmpty { AuthorityUtils.NO_AUTHORITIES }, idToken, null, IdTokenClaimNames.SUB)

    constructor(authorities: Collection<GrantedAuthority>, idToken: OidcIdToken, nameAttributeKey: String)
            : super(authorities.ifEmpty { AuthorityUtils.NO_AUTHORITIES }, idToken, null, nameAttributeKey)

    constructor(authorities: Collection<GrantedAuthority>, idToken: OidcIdToken, userInfo: OidcUserInfo?)
            : this(authorities, idToken, userInfo, IdTokenClaimNames.SUB)

    constructor(authorities: Collection<GrantedAuthority>, idToken: OidcIdToken, userInfo: OidcUserInfo?, nameAttributeKey: String)
            : super(authorities.ifEmpty { AuthorityUtils.NO_AUTHORITIES }, idToken, userInfo, nameAttributeKey) {
        /** keep the authorities mutable **/
        this.authorities = authorities.ifEmpty { AuthorityUtils.NO_AUTHORITIES }
    }

    constructor(idToken: OidcIdToken, userInfo: OidcUserInfo?)
            : super(AuthorityUtils.NO_AUTHORITIES, idToken, userInfo)

    //* normal functions *//
    fun getUserId(): String? {
        return userId
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    //* overridden functions with backing fields *//
    override fun getUsername(): String {
        return username
    }

    override fun getPassword(): String? {
        return null
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun isAccountNonExpired(): Boolean {
        return isAccountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return isAccountNonLocked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return isCredentialsNonExpired
    }

    override fun isEnabled(): Boolean {
        return isEnabled
    }

    fun toInstantUserEntity(): UserEntity {
        val userEntity = UserEntity()
        userEntity.personalInformation.firstName = givenName
        userEntity.personalInformation.middleName = middleName
        userEntity.personalInformation.lastName = familyName
        userEntity.contactInformation.email = email
        userEntity.securityInformation.isAccountNonExpired = true
        userEntity.securityInformation.isAccountNonLocked = true
        userEntity.securityInformation.isCredentialsNonExpired = true
        userEntity.securityInformation.isEnabled = true
        return userEntity
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/