package com.example.authorizationserver.auth.security.repositories

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Duration
import java.util.*

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class RegisteredClientConfig() {

    @Value("\${oauth2.client.registration.api-gateway.client-id}")
    private lateinit var apiGatewayClientId: String

    @Value("\${oauth2.client.registration.api-gateway.client-secret}")
    private lateinit var apiGatewayClientSecret: String

    @Bean
    /* registeredClientRepository (defines: authorization grants, OIDC scopes, etc.) */
    fun registeredClientRepository(passwordEncoder: PasswordEncoder): InMemoryRegisteredClientRepository {

        // api-gateway client (gateway client!)
        val apiGatewayClient = RegisteredClient
            .withId(UUID.randomUUID().toString())
            .clientId(apiGatewayClientId)
            .clientSecret(apiGatewayClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://127.0.0.1:9090/login/oauth2/code/in-house-auth-server")
            .postLogoutRedirectUri("https://www.manning.com/authorized")
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                    .accessTokenTimeToLive(Duration.ofMinutes(5))
                    .refreshTokenTimeToLive(Duration.ofMinutes(30))
                    .authorizationCodeTimeToLive(Duration.ofMinutes(1))
                    .reuseRefreshTokens(false)
                    .build())
            .scope(OidcScopes.OPENID) // requests an ID token, which is necessary for any OpenID Connect request.
            .scope(OidcScopes.PROFILE) // requests access to the user's profile information (e.g., name, date of birth)
            .clientSettings(ClientSettings
                .builder().requireAuthorizationConsent(true)
                .requireProofKey(true)
                .build())
            .build()

        // back-end client (resource server!)
        val resourceServer = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("resource_server")
            .clientSecret(passwordEncoder.encode("resource_server_secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build()

        return InMemoryRegisteredClientRepository(apiGatewayClient, resourceServer)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/