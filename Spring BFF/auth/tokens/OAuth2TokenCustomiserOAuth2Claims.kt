package com.example.authorizationserver.auth.security.tokens

import com.example.authorizationserver.auth.security.tokens.TokenCustomiserConfig.ClaimsBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsSet
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class OpaqueTokenCustomizerConfig(
    private val tokenCustomiserConfig: TokenCustomiserConfig
) {

    @Bean
    internal fun opaqueTokenCustomizer(): OAuth2TokenCustomizer<OAuth2TokenClaimsContext> {
        return OAuth2TokenCustomizer { context ->
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.tokenType)) {
                val userDetails = tokenCustomiserConfig.getUserDetails(context)
                tokenCustomiserConfig.addClaims(context.claims.toClaimsBuilder(), userDetails)
            }
        }
    }

    // OAuth2TokenClaimsSet.Builder extension
    internal fun OAuth2TokenClaimsSet.Builder.toClaimsBuilder(): ClaimsBuilder {
        return object : ClaimsBuilder {
            override fun claim(name: String, value: Any?) {
                this@toClaimsBuilder.claim(name, value)
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/