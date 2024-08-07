package com.example.authorizationserver.auth.repositories.authproviders

import com.example.authorizationserver.props.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class ClientRegistrationRepository(
    private val securityProperties: SecurityProperties
) {

    @Bean
    fun servletClientRegistrationRepository(): ClientRegistrationRepository {
        return InMemoryClientRegistrationRepository(googleClientRegistration())
    }

    // Google OAuth2.0 Authentication Provider
    private fun googleClientRegistration(): ClientRegistration {
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
            .clientId(securityProperties.googleClientId)
            .clientSecret(securityProperties.googleClientSecret)
            .build()
    }

    @Bean
    fun servletAuthorizedClientRepository(): OAuth2AuthorizedClientRepository {
        return HttpSessionOAuth2AuthorizedClientRepository()
    }

    @Bean
    fun servletAuthorizedClientService(
        servletClientRegistrationRepository: ClientRegistrationRepository
    ): OAuth2AuthorizedClientService {
        return InMemoryOAuth2AuthorizedClientService(
            servletClientRegistrationRepository
        )
    }

    @Bean
    fun servletAuthorizedClientManager(
        servletClientRegistrationRepository: ClientRegistrationRepository,
        servletAuthorizedClientRepository: OAuth2AuthorizedClientRepository
    ): OAuth2AuthorizedClientManager {

        // create a builder for the authorized client provider with different grant types
        val servletAuthorizedClientProvider: OAuth2AuthorizedClientProvider
                = OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()  // For the Authorization Code Grant flow
            .refreshToken()       // For the Refresh Token Grant flow
            .clientCredentials()  // For the Client Credentials Grant flow
            .build()

        // create the DefaultOAuth2AuthorizedClientManager instance
        val servletAuthorizedClientManager = DefaultOAuth2AuthorizedClientManager(
            servletClientRegistrationRepository,  // Repository for client registrations
            servletAuthorizedClientRepository     // Repository for authorized clients
        )

        // set the authorized client provider to the manager
        servletAuthorizedClientManager.setAuthorizedClientProvider(servletAuthorizedClientProvider)

        return servletAuthorizedClientManager
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/