package com.example.bff.auth.mappers

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
 * PropertiesResolversConfig sets up the necessary configuration
 * ByIssuerOpenidProviderPropertiesResolver determines and finds the appropriate OpenID provider properties
 * based on the issuer claim in the token.
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
        log.debug(
            "Building default OpenidProviderPropertiesResolver with: {}",
            oidcProviderProperties.openidProviderPropertiesList
        )
        return ByIssuerOpenidProviderPropertiesResolver(oidcProviderProperties)
    }
}

@Component
internal class ByIssuerOpenidProviderPropertiesResolver(
    private val oidcProviderProperties: OidcProviderProperties
) : OpenIdProviderPropertiesResolver {

    override fun resolve(claimSet: Map<String, Any>): Optional<OpenidProviderProperties> {
        val iss = claimSet["iss"]?.toString()

        return oidcProviderProperties.openidProviderPropertiesList
            .find { it.iss?.toString() == iss }
            .let { Optional.ofNullable(it) }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/