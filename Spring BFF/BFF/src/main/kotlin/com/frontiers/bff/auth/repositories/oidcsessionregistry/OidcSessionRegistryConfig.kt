package com.frontiers.bff.auth.repositories.oidcsessionregistry

import org.springframework.security.oauth2.client.oidc.authentication.logout.OidcLogoutToken
import org.springframework.security.oauth2.client.oidc.server.session.ReactiveOidcSessionRegistry
import org.springframework.security.oauth2.client.oidc.session.OidcSessionInformation
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

// see more:
// https://github.com/spring-projects/spring-security/issues/14558
// https://docs.spring.io/spring-security/reference/reactive/oauth2/login/logout.html#configure-provider-initiated-oidc-logout
// what is ReactiveOidcSessionRegistry for?

/**
 * Not sure whether Spring have implemented this properly, or provide support for Redis
 * Turning this off to avoid memory leaks
 */
@Component
internal class NoopSpringDataOidcSessionsStrategy(): ReactiveOidcSessionRegistry {

    override fun saveSessionInformation(info: OidcSessionInformation): Mono<Void> {
        // No operation, simply return an empty Mono
        return Mono.empty()
    }

    override fun removeSessionInformation(clientSessionId: String): Mono<OidcSessionInformation> {
        // No operation, simply return an empty Mono
        return Mono.empty()
    }

    override fun removeSessionInformation(token: OidcLogoutToken): Flux<OidcSessionInformation> {
        // No operation, simply return an empty Flux
        return Flux.empty()
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/