package com.frontiers.bff.auth.repositories.clientregistrations

import com.frontiers.bff.props.ClientSecurityProperties
import com.frontiers.bff.props.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

/**
 * Contains configuration details necessary for authenticating with an OAuth2 provider, such as client ID,
 * client secret, authorization grant types, scopes, and endpoints.
 * THIS STAYS IN MEMORY - IT DOES NOT NEED TO BE PERSISTED TO AN EXTERNAL STORE LIKE REDIS!
 */
@Configuration
internal class ClientRegistrationRepository(
    private val serverProperties: ServerProperties,
    private val clientSecurityProperties: ClientSecurityProperties,
) {

    @Bean
    fun reactiveClientRegistrationRepository(): ReactiveClientRegistrationRepository {
        return InMemoryReactiveClientRegistrationRepository(
            auth0Registration(), inHouseAuthRegistration()
        )
    }

    // Auth-0 Authentication Provider
    private fun auth0Registration(): ClientRegistration {
        return ClientRegistration
            .withRegistrationId(serverProperties.auth0AuthRegistrationId)
            .clientId(clientSecurityProperties.auth0ClientId)
            .clientSecret(clientSecurityProperties.auth0ClientSecret)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("${serverProperties.clientUri}/login/oauth2/code/${serverProperties.auth0AuthRegistrationId}")
            .authorizationUri("${serverProperties.auth0IssuerUri}/authorize")
            .tokenUri("${serverProperties.auth0IssuerUri}/oauth/token")
            .jwkSetUri("${serverProperties.auth0IssuerUri}/.well-known/jwks.json")
            .userInfoUri("${serverProperties.auth0IssuerUri}/userinfo")
            .providerConfigurationMetadata(mapOf(
                "issuer" to "${serverProperties.auth0IssuerUri}/",
                "authorization_endpoint" to "${serverProperties.auth0IssuerUri}/authorize",
                "token_endpoint" to "${serverProperties.auth0IssuerUri}/oauth/token",
                "userinfo_endpoint" to "${serverProperties.auth0IssuerUri}/userinfo",
                "end_session_endpoint" to "${serverProperties.auth0IssuerUri}/v2/logout",
                "jwks_uri" to "${serverProperties.auth0IssuerUri}/.well-known/jwks.json",
                "revocation_endpoint" to "${serverProperties.auth0IssuerUri}/oauth/revoke"
            ))
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .scope("openid", "offline_access")
            .clientName("BFF-Server")
            .issuerUri("${serverProperties.auth0IssuerUri}/")
            .build()
    }

    // In-House Authentication Provider
    private fun inHouseAuthRegistration(): ClientRegistration {
        return ClientRegistration
            .withRegistrationId(serverProperties.inHouseAuthRegistrationId)
            .clientId(clientSecurityProperties.inHouseAuthClientId)
            .clientSecret(clientSecurityProperties.inHouseAuthClientSecret)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("${serverProperties.clientUri}/login/oauth2/code/${serverProperties.inHouseAuthRegistrationId}")
            .authorizationUri("${serverProperties.inHouseIssuerUri}/oauth2/authorize")
            .tokenUri("${serverProperties.inHouseIssuerUri}/oauth2/token")
            .jwkSetUri("${serverProperties.inHouseIssuerUri}/oauth2/jwks")
            .userInfoUri("${serverProperties.inHouseIssuerUri}/userinfo")
            .providerConfigurationMetadata(mapOf(
                "issuer" to serverProperties.inHouseIssuerUri,
                "authorization_endpoint" to "${serverProperties.inHouseIssuerUri}/oauth2/authorize",
                "token_endpoint" to "${serverProperties.inHouseIssuerUri}/oauth2/token",
                "userinfo_endpoint" to "${serverProperties.inHouseIssuerUri}/userinfo",
                "end_session_endpoint" to "${serverProperties.inHouseIssuerUri}/connect/logout",
                "jwks_uri" to "${serverProperties.inHouseIssuerUri}/oauth2/jwks",
                "revocation_endpoint" to "${serverProperties.inHouseIssuerUri}/oauth2/revoke"
            ))
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .scope("openid", "offline_access")
            .clientName("BFF-Server")
            .issuerUri(serverProperties.inHouseIssuerUri)
            .build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/