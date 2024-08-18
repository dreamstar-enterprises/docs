package com.example.bff.auth.mappers.converters

import com.c4_soft.springaddons.security.oidc.starter.properties.NotAConfiguredOpenidProviderException
import com.example.bff.props.OidcProviderProperties.SimpleAuthoritiesMappingProperties
import com.example.bff.props.OidcProviderProperties.SimpleAuthoritiesMappingProperties.Case
import com.example.bff.props.OidcProviderProperties.SimpleAuthoritiesMappingProperties.Case.*
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.util.StringUtils

/**********************************************************************************************************************/
/*********************************************** AUTHORITIES CONVERTER ************************************************/
/**********************************************************************************************************************/

/*
 * A converter that takes JWT claims and converts defined ones into Spring Authorities (for the Authentication Object)
 */
@Configuration
internal class ConfigurableClaimSetAuthoritiesConverter (
    private val opPropertiesResolver: OpenIdProviderPropertiesResolver
) : ClaimSetAuthoritiesConverter {

    private val log = LoggerFactory.getLogger(ConfigurableClaimSetAuthoritiesConverter::class.java)

    // find claims and convert them into authorities
    override fun convert(source: Map<String, Any>): Collection<GrantedAuthority> {
        val opProperties = opPropertiesResolver.resolve(source)
            .orElseThrow { NotAConfiguredOpenidProviderException(source) }

        log.info("Resolved OpenID provider properties: $opProperties")

        val authorities = opProperties.authorities
            .flatMap { authoritiesMappingProps -> getAuthorities(source, authoritiesMappingProps) }
            .map { role -> SimpleGrantedAuthority(role) }

        log.info("Converted authorities: $authorities")
        return authorities
    }

    // get authorities, from relevant claim(s), and return as a list of (formatted) strings
    private fun getAuthorities(
        claims: Map<String, Any>,
        props: SimpleAuthoritiesMappingProperties
    ): List<String> {

        val extractedClaims = getClaims(claims, props.path)

        log.debug("Extracted claims for path {}: {}", props.path, extractedClaims)

        return extractedClaims
            .flatMap { claim -> claim.split(",").flatMap { it.split(" ") } }
            .filter { StringUtils.hasText(it) }
            .map { it.trim() }
            .map { processCase(it, props.case) }
            .map { "${props.prefix}$it" }
    }

    // convert to the correct case
    private fun processCase(role: String, case: Case): String {
        return when (case) {
            UPPER -> role.uppercase()
            LOWER -> role.lowercase()
            else -> role
        }
    }

   // get claims from claims json (i.e. from the Map<String, Any>)
    private fun getClaims(claims: Map<String, Any>, path: String): List<String> {

       log.info("Attempting to extract claims from path: {}", path)

        return try {
            when (val res = JsonPath.read<Any>(claims, path)) {
                is String -> listOf(res)
                is List<*> -> when {
                    res.isEmpty() -> emptyList()
                    res[0] is String -> res.filterIsInstance<String>()
                    res[0] is List<*> -> res.flatMap { (it as? List<*>)?.filterIsInstance<String>() ?: emptyList() }
                    else -> emptyList()
                }
                else -> emptyList()
            }
        } catch (e: PathNotFoundException) {
            log.info("Path not found: {}. Exception: {}", path, e.message)
            emptyList()
        }
    }

}

/**********************************************************************************************************************/
/*************************************************** END OF KOTLIN ****************************************************/
/**********************************************************************************************************************/