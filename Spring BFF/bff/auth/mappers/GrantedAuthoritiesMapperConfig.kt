package com.example.bff.auth.mappers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority

/**********************************************************************************************************************/
/************************************************* AUTHORITY MAPPER ***************************************************/
/**********************************************************************************************************************/

/*
 * Params: authoritiesConverter – the authorities converter to use (by default ConfigurableClaimSetAuthoritiesConverter)
 * Returns: GrantedAuthoritiesMapper using the authorities converter in the context
 * Helps with transforming and mapping user authorities based on their claims or attributes, providing
 * a flexible way to customize how authorities are derived and used in the security context
 */
@Configuration
internal class GrantedAuthoritiesMapperConfig {

    @Bean
    fun grantedAuthoritiesMapper(
        authoritiesConverter: ClaimSetAuthoritiesConverter
    ): GrantedAuthoritiesMapper {

        return GrantedAuthoritiesMapper { authorities ->
            val mappedAuthorities = mutableSetOf<GrantedAuthority>()

            authorities.forEach { authority ->
                when (authority) {
                    is OidcUserAuthority -> {
                        mappedAuthorities.addAll(
                            authoritiesConverter.convert(authority.idToken.claims)
                                ?: emptyList())
                    }
                    is OAuth2UserAuthority -> {
                        mappedAuthorities.addAll(
                            authoritiesConverter.convert(authority.attributes)
                                ?: emptyList())
                    }
                }
            }

            mappedAuthorities
        }
    }

    /*
     * Retrieves granted authorities from the Jwt (from its private claims or with the help of an external service)
     * and converts them into a collection of GrantedAuthorities
     * Note: The term "provider" underscores that this resolver is supplying the properties required for
     * further processing, such as mapping claims to authorities in an authentication or authorization context.
     */
    @Bean
    fun authoritiesConverter(
        authoritiesMappingPropertiesProvider: OpenIdProviderPropertiesResolver
    ): ClaimSetAuthoritiesConverter {
        return ConfigurableClaimSetAuthoritiesConverter(authoritiesMappingPropertiesProvider)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/