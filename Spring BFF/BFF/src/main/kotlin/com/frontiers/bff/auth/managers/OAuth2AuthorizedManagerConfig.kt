package com.frontiers.bff.auth.managers

import com.frontiers.bff.auth.repositories.authclients.RedisServerOAuth2AuthorizedClientRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager

/**********************************************************************************************************************/
/********************************************* AUTHORIZED CLIENT MANAGER **********************************************/
/**********************************************************************************************************************/

/**
 * Manages the state of authorized clients, including obtaining, refreshing, and storing access tokens
 * Configures and provides a ReactiveOAuth2AuthorizedClientManager bean. This bean manages OAuth2 clients in a
 * reactive Spring application, handling client authorization and token management.
 *
 * ReactiveClientRegistrationRepository: Provides OAuth2 client registration details.
 * RedisServerOAuth2AuthorizedClientRepository: Manages authorized OAuth2 clients.
 * ReactiveOAuth2AuthorizedClientProvider: Handles token acquisition and refresh.
 */
@Configuration
internal class OAuth2AuthorizedManagerConfig {

    private val logger: Logger = LoggerFactory.getLogger(OAuth2AuthorizedManagerConfig::class.java)

    @Bean
    fun reactiveAuthorizedClientManager(
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
        redisServerOAuth2AuthorizedClientRepository: RedisServerOAuth2AuthorizedClientRepository,
        reactiveAuthorizedClientProvider: ReactiveOAuth2AuthorizedClientProvider,
    ): ReactiveOAuth2AuthorizedClientManager {

        logger.info("Creating reactiveAuthorizedClientManager instance")

        // create the AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager instance
        val reactiveAuthorizedClientManager = DefaultReactiveOAuth2AuthorizedClientManager(
            reactiveClientRegistrationRepository,
            redisServerOAuth2AuthorizedClientRepository
        )

        logger.info("Setting reactiveAuthorizedClientManager with reativeAuthorizedClientProvider ")

        // set the authorized client provider to the manager
        reactiveAuthorizedClientManager.setAuthorizedClientProvider(reactiveAuthorizedClientProvider)

        return reactiveAuthorizedClientManager
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/