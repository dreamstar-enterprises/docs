package com.example.authorizationserver.auth.tokens

import com.example.authorizationserver.auth.objects.user.CustomOidcUser
import com.example.authorizationserver.auth.objects.user.DocDbUser
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.token.*

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class TokenCustomiserConfig {

    interface ClaimsBuilder {
        fun claim(name: String, value: Any?)
    }

    @Bean
    // used for generating either opaque or jwt tokens
    internal fun tokenGenerator(
        jwkSource: JWKSource<SecurityContext>,
        jwtTokenCustomizer: OAuth2TokenCustomizer<JwtEncodingContext>,
        opaqueTokenCustomizer: OAuth2TokenCustomizer<OAuth2TokenClaimsContext>
    ): OAuth2TokenGenerator<out OAuth2Token> {
        val jwtEncoder = NimbusJwtEncoder(jwkSource)
        val jwtGenerator = JwtGenerator(jwtEncoder)
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer)

        val accessTokenGenerator = OAuth2AccessTokenGenerator()
        accessTokenGenerator.setAccessTokenCustomizer(opaqueTokenCustomizer)

        val refreshTokenGenerator = OAuth2RefreshTokenGenerator()
        return DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator)
    }

    // utility method (generic type) for getting user details / principal object
    internal fun <T> getUserDetails(context: T): UserDetails where T : OAuth2TokenContext {
        return when (val principal = context.getPrincipal<Authentication>().principal) {
            is CustomOidcUser -> principal
            is DocDbUser -> principal
            is UserDetails -> principal
            else -> throw IllegalStateException("Unsupported principal type: ${principal::class}")
        }
    }

    // utility method for adding custom claims
    internal fun addClaims(claimsBuilder: ClaimsBuilder, userDetails: UserDetails) {
        if (userDetails.username.isNotEmpty()) {
            claimsBuilder.apply {
                claim("userId", extractUserId(userDetails))
                claim("username", userDetails.username)
                claim("authorities", userDetails.authorities.map { it.authority }.toSet())
                claim("isAccountNonExpired", userDetails.isAccountNonExpired)
                claim("isAccountNonLocked", userDetails.isAccountNonLocked)
                claim("isCredentialsNonExpired", userDetails.isCredentialsNonExpired)
                claim("isEnabled", userDetails.isEnabled)

            }
        } else {
            throw IllegalStateException("Bad UserDetails, username is empty")
        }
    }

    // utility method for getting user id (since getter does not exist on type UserDetails)
    private fun extractUserId(userDetails: UserDetails): String? {
        return when (userDetails) {
            is CustomOidcUser -> userDetails.getUserId()
            is DocDbUser -> userDetails.getUserId()
            else -> throw IllegalStateException("Unsupported UserDetails type: ${userDetails::class}")
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/