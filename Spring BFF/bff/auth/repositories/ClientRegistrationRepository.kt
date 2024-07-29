package com.example.bff.auth.repositories

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class ClientRegistrationRepository() {

    @Value("\${gateway-client-id}")
    private lateinit var gatewayClientId: String

    @Value("\${gateway-client-secret}")
    private lateinit var gatewayClientSecret: String

    @Value("\${in-house-auth-registration-id}")
    private lateinit var inHouseAuthRegistrationId: String

    @Value("\${issuer-uri}")
    private lateinit var issuerUri: String

    @Value("\${reverse-proxy-uri}")
    private lateinit var reverseProxyUri: String

    @Value("\${bff-prefix}")
    private lateinit var bffPrefix: String

    @Bean
    fun reactiveClientRegistrationRepository(): InMemoryReactiveClientRegistrationRepository {
        val inHouseAuthServerRegistration = ClientRegistration
            .withRegistrationId(inHouseAuthRegistrationId)
            .clientId(gatewayClientId)
            .clientSecret(gatewayClientSecret)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("$reverseProxyUri$bffPrefix/login/oauth2/code/$inHouseAuthRegistrationId")
            .authorizationUri("$issuerUri/oauth2/authorize")
            .tokenUri("$issuerUri/oauth2/token")
            .jwkSetUri("$issuerUri/oauth2/jwks")
            .userInfoUri("$issuerUri/userinfo")
            .providerConfigurationMetadata(mapOf(
                "issuer" to issuerUri,
                "authorization_endpoint" to "$issuerUri/oauth2/authorize",
                "token_endpoint" to "$issuerUri/oauth2/token",
                "userinfo_endpoint" to "$issuerUri/userinfo",
                "jwks_uri" to "$issuerUri/oauth2/jwks",
                "revocation_endpoint" to "$issuerUri/oauth2/revoke"
            ))
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .scope("openid")
            .clientName("In-House Auth Server")
            .issuerUri(issuerUri)
            .build()

        return InMemoryReactiveClientRegistrationRepository(
            inHouseAuthServerRegistration
        )
    }

    @Bean
    fun reactiveAuthorizedClientService(
        reactiveClientRegistrationRepository: InMemoryReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientService {
        return InMemoryReactiveOAuth2AuthorizedClientService(
            reactiveClientRegistrationRepository
        )
    }

    @Bean
    fun reactiveAuthorizedClientManager(
        reactiveClientRegistrationRepository: InMemoryReactiveClientRegistrationRepository,
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