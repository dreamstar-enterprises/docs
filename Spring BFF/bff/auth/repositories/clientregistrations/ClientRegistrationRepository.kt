package com.example.bff.auth.repositories.clientregistrations

import com.example.bff.props.ClientSecurityProperties
import com.example.bff.props.ServerProperties
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
            inHouseAuthRegistration()
        )
    }

    // In-House Authentication Provider
    private fun inHouseAuthRegistration(): ClientRegistration {
        return ClientRegistration
            .withRegistrationId(serverProperties.inHouseAuthRegistrationId)
            .clientId(clientSecurityProperties.bffClientId)
            .clientSecret(clientSecurityProperties.bffClientSecret)
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
            .scope("openid")
            .clientName("BFF-Server")
            .issuerUri(serverProperties.inHouseIssuerUri)
            .build()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/