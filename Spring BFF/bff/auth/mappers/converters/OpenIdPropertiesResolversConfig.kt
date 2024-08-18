package com.example.bff.auth.mappers.converters

import com.example.bff.props.OidcProviderProperties
import com.example.bff.props.OidcProviderProperties.OpenidProviderProperties
import org.slf4j.LoggerFactory

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.*

/**********************************************************************************************************************/
/************************************************ PROPERTIES RESOLVERS ************************************************/
/**********************************************************************************************************************/

/*
 * Returns the ByIssuerOpenidProviderPropertiesResolver with a list of oidcProviderProperties passed to it
 * Note: The term "resolve" aligns with the functionality of identifying and returning specific properties
 * based on some input.
 */
@Configuration
internal class OpenIdPropertiesResolversConfig {

    private val log = LoggerFactory.getLogger(OpenIdPropertiesResolversConfig::class.java)

    @Bean
    @Primary
    fun openidProviderPropertiesResolver(
        oidcProviderProperties: OidcProviderProperties
    ): OpenIdProviderPropertiesResolver {
        log.info("Building default OpenidProviderPropertiesResolver with the following OpenID provider properties list:")

        oidcProviderProperties.openidProviderPropertiesList.forEach { providerProperties ->
            log.info("OpenIdProviderProperties: iss={}, jwkSetUri={}, aud={}, authorities={}, usernameClaim={}",
                providerProperties.iss,
                providerProperties.jwkSetUri,
                providerProperties.aud,
                providerProperties.authorities,
                providerProperties.usernameClaim
            )
        }
        return ByIssuerOpenidProviderPropertiesResolver(oidcProviderProperties)
    }
}

/*
 * ByIssuerOpenidProviderPropertiesResolver finds the appropriate OpenID provider properties based on the
 * issuer claim in a token.
 * Note: The term "resolve" aligns with the functionality of identifying and returning specific properties
 * based on some input.
 */
@Component
internal class ByIssuerOpenidProviderPropertiesResolver(
    private val oidcProviderProperties: OidcProviderProperties
) : OpenIdProviderPropertiesResolver {

    private val log = LoggerFactory.getLogger(ByIssuerOpenidProviderPropertiesResolver::class.java)

    override fun resolve(claimSet: Map<String, Any>): Optional<OpenidProviderProperties> {
        val iss = claimSet["iss"]?.toString()

        log.info("Resolving OpenID provider properties for issuer: {}", iss)

        val resolvedProperties = oidcProviderProperties.openidProviderPropertiesList
            .find { it.iss?.toString() == iss }
            .let { Optional.ofNullable(it) }

        if (resolvedProperties.isPresent) {
            log.info("Found matching OpenIdProviderProperties for issuer: {}", iss)
        } else {
            log.info("No matching OpenIdProviderProperties found for issuer: {}", iss)
        }

        return resolvedProperties
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/