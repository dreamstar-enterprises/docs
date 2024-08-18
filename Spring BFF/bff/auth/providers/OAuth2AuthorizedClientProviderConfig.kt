package com.example.bff.auth.providers

import com.example.bff.props.RequestParameterProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository

/**********************************************************************************************************************/
/********************************************* AUTHORIZED CLIENT PROVIDER *********************************************/
/**********************************************************************************************************************/

/**
 * Provides a mechanism to obtain and refresh OAuth 2.0 access tokens.
 * ReactiveOAuth2AuthorizedClientProvider is responsible for managing OAuth2 tokens, which includes obtaining
 * access tokens, refreshing them, and handling token expiration.
 */
@Configuration
internal class OAuth2AuthorizedClientProviderConfig {

    private val log = LoggerFactory.getLogger(OAuth2AuthorizedClientProviderConfig::class.java)

    @Bean
    @Primary
    fun reativeAuthorizedClientProvider(
        requestParameterProperties: RequestParameterProperties,
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientProvider {

        val provider = PerRegistrationReactiveOAuth2AuthorizedClientProvider(
            reactiveClientRegistrationRepository,
            requestParameterProperties,
            emptyMap()
        )

        // Print the details of the provider for debugging
        log.info("Created ReactiveOAuth2AuthorizedClientProvider: $provider")

        return provider
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/