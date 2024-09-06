package com.frontiers.bff.auth.providers

import com.frontiers.bff.props.RequestParameterProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository

/**********************************************************************************************************************/
/********************************************* AUTHORIZED CLIENT PROVIDER *********************************************/
/**********************************************************************************************************************/

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/client/ReactiveSpringAddonsOAuth2AuthorizedClientBeans.java

/**
 * Provides a mechanism to obtain and refresh OAuth 2.0 access tokens.
 * ReactiveOAuth2AuthorizedClientProvider is responsible for managing OAuth2 tokens, which includes obtaining
 * access tokens, refreshing them, and handling token expiration.
 */
@Configuration
internal class OAuth2AuthorizedClientProviderConfig {

    @Bean
    fun reativeAuthorizedClientProvider(
        requestParameterProperties: RequestParameterProperties,
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientProvider {

        val providersMap = PerRegistrationReactiveOAuth2AuthorizedClientProvider(
            reactiveClientRegistrationRepository,
            requestParameterProperties,
            emptyMap()
        )

        return providersMap
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/