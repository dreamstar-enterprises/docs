package com.example.bff.auth.providers

import com.example.bff.props.RequestParameterProperties
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
 * Handles the authorization code exchange, refresh tokens, and other OAuth 2.0 authorization flows.
 */
@Configuration
internal class OAuth2AuthorizedClientProviderConfig {

    @Bean
    @Primary
    fun reativeAuthorizedClientProvider(
        requestParameterProperties: RequestParameterProperties,
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientProvider {

        return PerRegistrationReactiveOAuth2AuthorizedClientProvider(
            reactiveClientRegistrationRepository,
            requestParameterProperties,
            emptyMap()
        )
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/