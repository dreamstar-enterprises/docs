package com.frontiers.bff.auth.mappers

import com.frontiers.bff.auth.mappers.converters.ClaimSetAuthoritiesConverter
import com.frontiers.bff.auth.mappers.converters.ConfigurableClaimSetAuthoritiesConverter
import com.frontiers.bff.auth.mappers.converters.OpenIdProviderPropertiesResolver
import org.slf4j.LoggerFactory
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
 * Helps with mapping custom user authorities from ID Token claims or attributes, to Spring SimpleGrantedAuthority
 * that are then stored in a security context object
 * Note: only gets custom claims from the ID Token (or attributes) - those in Access Token stay in that token!
 */
@Configuration
internal class GrantedAuthoritiesMapperConfig {

    private val logger = LoggerFactory.getLogger(GrantedAuthoritiesMapperConfig::class.java)

    @Bean
    fun grantedAuthoritiesMapper(
        authoritiesConverter: ClaimSetAuthoritiesConverter,
    ): GrantedAuthoritiesMapper {

        return GrantedAuthoritiesMapper { authorities ->
            val mappedAuthorities = mutableSetOf<GrantedAuthority>()

            authorities.forEach { authority ->
                logger.info("PROCESSING AUTHORITY: $authority")

                when (authority) {
                    is OidcUserAuthority -> {
                        val idTokenClaims = authority.idToken.claims
                        logger.info("OidcUserAuthority ID Token Claims: $idTokenClaims")

                        val convertedAuthorities = authoritiesConverter.convert(idTokenClaims) ?: emptyList()
                        logger.info("Converted authorities: $convertedAuthorities")
                        mappedAuthorities.addAll(convertedAuthorities)
                    }
                    is OAuth2UserAuthority -> {
                        val attributes = authority.attributes
                        logger.info("OAuth2UserAuthority attributes: $attributes")

                        val convertedAuthorities = authoritiesConverter.convert(attributes) ?: emptyList()
                        logger.info("Converted authorities: $convertedAuthorities")
                        mappedAuthorities.addAll(convertedAuthorities)
                    }
                }
            }

            logger.info("Final mapped authorities: $mappedAuthorities")
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
        logger.info("Initializing authorities converter with properties OpenId provider properties resolver: $authoritiesMappingPropertiesProvider")
        return ConfigurableClaimSetAuthoritiesConverter(authoritiesMappingPropertiesProvider)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/