package com.example.bff.auth.repositories

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class ClientRegistrationRepository() {

    @Value("\${oauth2.client.registration.api-gateway.client-id}")
    private lateinit var gatewayClientId: String

    @Value("\${oauth2.client.registration.api-gateway.client-secret}")
    private lateinit var gatewayClientSecret: String

    @Value("\${in-house-auth-registration-id}")
    private lateinit var inHouseAuthRegistrationId: String

    @Value("\${in-house-issuer-uri}")
    private lateinit var inHouseIssuerUri: String

    @Value("\${reverse-proxy-uri}")
    private lateinit var reverseProxyUri: String

    @Value("\${bff-prefix}")
    private lateinit var bffPrefix: String

    @Bean
    fun reactiveClientRegistrationRepository(): ReactiveClientRegistrationRepository {
        return InMemoryReactiveClientRegistrationRepository(
            inHouseAuthServerRegistration()
        )
    }

    private fun inHouseAuthServerRegistration(): ClientRegistration {
        return ClientRegistration
            .withRegistrationId(inHouseAuthRegistrationId)
            .clientId(gatewayClientId)
            .clientSecret(gatewayClientSecret)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("$reverseProxyUri$bffPrefix/login/oauth2/code/$inHouseAuthRegistrationId")
            .authorizationUri("$inHouseIssuerUri/oauth2/authorize")
            .tokenUri("$inHouseIssuerUri/oauth2/token")
            .jwkSetUri("$inHouseIssuerUri/oauth2/jwks")
            .userInfoUri("$inHouseIssuerUri/userinfo")
            .providerConfigurationMetadata(mapOf(
                "issuer" to inHouseIssuerUri,
                "authorization_endpoint" to "$inHouseIssuerUri/oauth2/authorize",
                "token_endpoint" to "$inHouseIssuerUri/oauth2/token",
                "userinfo_endpoint" to "$inHouseIssuerUri/userinfo",
                "jwks_uri" to "$inHouseIssuerUri/oauth2/jwks",
                "revocation_endpoint" to "$inHouseIssuerUri/oauth2/revoke"
            ))
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .scope("openid")
            .clientName("In-House Auth Server")
            .issuerUri(inHouseIssuerUri)
            .build()
    }

    @Bean
    fun reactiveAuthorizedClientRepository(): ServerOAuth2AuthorizedClientRepository {
        return WebSessionServerOAuth2AuthorizedClientRepository()
    }

    @Bean
    fun reactiveAuthorizedClientService(
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientService {
        return InMemoryReactiveOAuth2AuthorizedClientService(
            reactiveClientRegistrationRepository
        )
    }

    @Bean
    fun reactiveAuthorizedClientManager(
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
        reactiveAuthorizedClientRepository: ServerOAuth2AuthorizedClientRepository
    ): ReactiveOAuth2AuthorizedClientManager {

        // Create a builder for the authorized client provider with different grant types
        val reactiveAuthorizedClientProvider: ReactiveOAuth2AuthorizedClientProvider
                = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()  // For the Authorization Code Grant flow
            .refreshToken()       // For the Refresh Token Grant flow
            .clientCredentials()  // For the Client Credentials Grant flow
            .build()

        // Create the DefaultReactiveOAuth2AuthorizedClientManager instance
        val reactiveAuthorizedClientManager = DefaultReactiveOAuth2AuthorizedClientManager(
            reactiveClientRegistrationRepository,  // Repository for client registrations
            reactiveAuthorizedClientRepository     // Repository for authorized clients
        )

        // Set the authorized client provider to the manager
        reactiveAuthorizedClientManager.setAuthorizedClientProvider(reactiveAuthorizedClientProvider)

        return reactiveAuthorizedClientManager
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/