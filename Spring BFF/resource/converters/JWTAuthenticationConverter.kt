package com.example.timesheetapi.auth.security.converters

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/********************************************* AUTHENTICATION CONVERTER ***********************************************/
/**********************************************************************************************************************/

@Component
internal class JwtAuthenticationConverter : Converter<Jwt, Mono<JwtAuthenticationToken>> {

    override fun convert(source: Jwt): Mono<JwtAuthenticationToken> {
        val authorities = listOf(GrantedAuthority { "READ" })

        val customClaim = source.getClaimAsString("custom-claim") ?: ""

        return Mono.just(JwtAuthenticationToken(source, authorities, customClaim))
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/