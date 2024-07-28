package com.example.authorizationserver.auth.security.repositories

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class OAuth2ServiceConfig() {

    @Bean
    // where new authorizations / tokens are stored and existing authorizations are queried
    fun authorizationService(): OAuth2AuthorizationService {
        return InMemoryOAuth2AuthorizationService()
    }

    @Bean
    // where new authorization consents are stored and existing authorization consents are queried
    fun authorizationServiceConsent(): OAuth2AuthorizationConsentService {
        return InMemoryOAuth2AuthorizationConsentService()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/