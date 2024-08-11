package com.example.authorizationserver.auth.repositories.registeredclients

import com.example.authorizationserver.props.ClientSecurityProperties
import com.example.authorizationserver.props.ServerProperties
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
internal class RegisteredClientConfig(
    private val serverProperties: ServerProperties,
    private val clientSecurityProperties: ClientSecurityProperties
) {

    @Bean
    /* registeredClientRepository (defines: authorization grants, OIDC scopes, etc.) */
    fun registeredClientRepository(passwordEncoder: PasswordEncoder): InMemoryRegisteredClientRepository {

        // bff client
        val bffServer = RegisteredClient
            .withId(UUID.randomUUID().toString())
            .clientId(clientSecurityProperties.bffClientId)
            .clientSecret(clientSecurityProperties.bffClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("${serverProperties.bffUri}/login/oauth2/code/${serverProperties.inHouseAuthRegistrationId}")
            .postLogoutRedirectUri("https://www.manning.com/authorized")
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                    .accessTokenTimeToLive(Duration.ofMinutes(5))
                    .refreshTokenTimeToLive(Duration.ofMinutes(30))
                    .authorizationCodeTimeToLive(Duration.ofMinutes(1))
                    .reuseRefreshTokens(false)
                    .build())
            // requests an ID token, which is necessary for any OpenID Connect request.
            .scope(OidcScopes.OPENID)
            // requests access to the user's profile information (e.g., name, date of birth)
            .scope(OidcScopes.PROFILE)
            .clientName("BFF-Server")
            .clientSettings(ClientSettings
                .builder().requireAuthorizationConsent(true)
                .requireProofKey(true)
                .build())
            .build()

        // back-end client (resource server!)
        val resourceServer = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientSecurityProperties.resourceClientId)
            .clientSecret(passwordEncoder.encode(clientSecurityProperties.resourceClientSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build()

        return InMemoryRegisteredClientRepository(bffServer, resourceServer)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/