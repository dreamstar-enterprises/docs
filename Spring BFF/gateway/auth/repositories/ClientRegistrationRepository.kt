package com.example.gateway.auth.repositories

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class ClientRegistrationRepository {

    @Value("\${oauth2.client.registration.api-gateway.client-id}")
    private lateinit var apiGatewayClientId: String

    @Value("\${oauth2.client.registration.api-gateway.client-secret}")
    private lateinit var apiGatewayClientSecret: String

    @Value("\${gateway-url}")
    private lateinit var gatewayUrl: String

    @Value("\${authorization-url}")
    private lateinit var authorizationUrl: String


    @Bean
    fun reactiveClientRegistrationRepository(): InMemoryReactiveClientRegistrationRepository {
        val inHouseAuthServerRegistration = ClientRegistration
            .withRegistrationId("in-house-auth-server")
            .clientId(apiGatewayClientId)
            .clientSecret(apiGatewayClientSecret)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("$gatewayUrl/login/oauth2/code/in-house-auth-server")
            .authorizationUri("$authorizationUrl/oauth2/authorize")
            .tokenUri("$authorizationUrl/oauth2/token")
            .jwkSetUri("$authorizationUrl/oauth2/jwks")
            .scope("openid")
            .issuerUri(authorizationUrl)
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