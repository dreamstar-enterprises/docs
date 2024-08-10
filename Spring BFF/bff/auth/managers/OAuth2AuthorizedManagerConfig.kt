package com.example.bff.auth.managers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository

/**********************************************************************************************************************/
/********************************************* AUTHORIZED CLIENT MANAGER **********************************************/
/**********************************************************************************************************************/

/**
 * Manages the state of authorized clients, including obtaining, refreshing, and storing access tokens
 */
@Configuration
internal class OAuth2AuthorizedManagerConfig {

    @Bean
    fun reactiveAuthorizedClientManager(
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
        reactiveAuthorizedClientRepository: ServerOAuth2AuthorizedClientRepository,
        reactiveAuthorizedClientProvider: ReactiveOAuth2AuthorizedClientProvider
    ): ReactiveOAuth2AuthorizedClientManager {

        // create the DefaultReactiveOAuth2AuthorizedClientManager instance
        val reactiveAuthorizedClientManager = DefaultReactiveOAuth2AuthorizedClientManager(
            reactiveClientRegistrationRepository, // Repository for client registrations
            reactiveAuthorizedClientRepository // Repository for authorized clients
        )

        // set the authorized client provider to the manager
        reactiveAuthorizedClientManager.setAuthorizedClientProvider(reactiveAuthorizedClientProvider)

        return reactiveAuthorizedClientManager
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/