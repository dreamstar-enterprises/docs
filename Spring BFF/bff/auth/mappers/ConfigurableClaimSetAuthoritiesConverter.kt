package com.example.bff.auth.mappers

import com.c4_soft.springaddons.security.oidc.starter.properties.NotAConfiguredOpenidProviderException
import com.example.bff.props.OidcProviderProperties.SimpleAuthoritiesMappingProperties
import com.example.bff.props.OidcProviderProperties.SimpleAuthoritiesMappingProperties.Case
import com.example.bff.props.OidcProviderProperties.SimpleAuthoritiesMappingProperties.Case.*
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
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

        // find claims and convert them into authorities
        override fun convert(source: Map<String, Any>): Collection<GrantedAuthority> {
            val opProperties = opPropertiesResolver.resolve(source)
                .orElseThrow { NotAConfiguredOpenidProviderException(source) }

            return opProperties.authorities
                .flatMap { authoritiesMappingProps -> getAuthorities(source, authoritiesMappingProps) }
                .map { role -> SimpleGrantedAuthority(role) }
        }

        // get authorities (formatted) as a list of strings
        private fun getAuthorities(
            claims: Map<String, Any>,
            props: SimpleAuthoritiesMappingProperties
        ): List<String> {
            return getClaims(claims, props.path)
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

       // get claims from claims json (from the Map<String, Any>)
        private fun getClaims(claims: Map<String, Any>, path: String): List<String> {
            return try {
                val res = JsonPath.read<Any>(claims, path)
                when (res) {
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
                emptyList()
            }
        }

    }

/**********************************************************************************************************************/
/*************************************************** END OF KOTLIN ****************************************************/
/**********************************************************************************************************************/