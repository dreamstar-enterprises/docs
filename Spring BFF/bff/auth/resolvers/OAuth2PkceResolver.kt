package com.example.bff.auth.resolvers

import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/***************************************************** RESOLVER *******************************************************/
/**********************************************************************************************************************/

// more here:
// https://www.baeldung.com/spring-security-pkce-secret-clients

@Component
internal class OAuth2PkceResolver(
    private val repo: ReactiveClientRegistrationRepository
) {

    @Bean
    fun pkceResolver(): ServerOAuth2AuthorizationRequestResolver {
        val resolver = DefaultServerOAuth2AuthorizationRequestResolver(repo)
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce())
        return resolver
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/