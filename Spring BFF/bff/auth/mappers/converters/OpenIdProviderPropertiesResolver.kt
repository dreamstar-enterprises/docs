package com.example.bff.auth.mappers.converters

import com.example.bff.props.OidcProviderProperties.OpenidProviderProperties
import java.util.*

/**********************************************************************************************************************/
/***************************************************** INTERFACE ******************************************************/
/**********************************************************************************************************************/

/*
 * Resolves OpenID Provider configuration properties from OAuth2 / OpenID claims
 * (decoded from a JWT, introspected from an opaque token or retrieved from userinfo endpoint)
 */
internal interface OpenIdProviderPropertiesResolver {
    fun resolve(claimSet: Map<String, Any>): Optional<OpenidProviderProperties>
}
/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/