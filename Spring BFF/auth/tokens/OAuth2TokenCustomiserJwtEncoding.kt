package com.example.authorizationserver.auth.tokens

import com.example.authorizationserver.auth.tokens.TokenCustomiserConfig.ClaimsBuilder
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class OAuth2TokenCustomiserJwtEncoding(
    private val tokenCustomiserConfig: TokenCustomiserConfig
) {

    @Bean
    internal fun jwtTokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context ->
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.tokenType)) {
                val userDetails = tokenCustomiserConfig.getUserDetails(context)
                tokenCustomiserConfig.addClaims(context.claims.toClaimsBuilder(), userDetails)

                // Convert JwtClaimsSet.Builder to JwtClaimsSet to access claims
                val claimsSet = context.claims.build()

                // Print each claim to the console
                claimsSet.claims.forEach { (key, value) ->
                    println("Claim Key: $key, Claim Value: $value")
                }
            }
        }
    }


    // jwtClaimsSet.Builder extension
    internal fun JwtClaimsSet.Builder.toClaimsBuilder(): ClaimsBuilder {
        return object : ClaimsBuilder {
            override fun claim(name: String, value: Any?) {
                this@toClaimsBuilder.claim(name, value)
            }
        }
    }


    @Bean
    // for signing JWT access tokens (from Nimbus)
    internal fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = generateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    // for generating the RSA key, for JWT tokens (asymmetric encryption: private + public key)
    private fun generateRsaKey(): KeyPair {
        val keyPair: KeyPair
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            keyPair = keyPairGenerator.generateKeyPair()
        } catch (ex: NoSuchAlgorithmException) {
            throw IllegalStateException("RSA KeyPairGenerator not available", ex)
        }
        return keyPair
    }

    @Bean
    // for decoding signed access tokens
    internal fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/
