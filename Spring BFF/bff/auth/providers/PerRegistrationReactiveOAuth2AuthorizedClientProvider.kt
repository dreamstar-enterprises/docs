package com.example.bff.auth.providers

import com.example.bff.props.RequestParameterProperties
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveRefreshTokenTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**********************************************************************************************************************/
/********************************************* AUTHORIZED CLIENT PROVIDER **********************************************/
/**********************************************************************************************************************/

/*
* An alternative ReactiveOAuth2AuthorizedClientProvider to DelegatingReactiveOAuth2AuthorizedClientProvider keeping
* a different provider for each client registration. This allows one to define for each, a set of extra parameters
* to add to token requests.
*/

internal class PerRegistrationReactiveOAuth2AuthorizedClientProvider(
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
    private val requestParameterProperties: RequestParameterProperties,
    private val customProvidersByRegistrationId: Map<String, List<ReactiveOAuth2AuthorizedClientProvider>>,

) : ReactiveOAuth2AuthorizedClientProvider {

    private val providersByRegistrationId = ConcurrentHashMap<String, DelegatingReactiveOAuth2AuthorizedClientProvider>()


    // populate providersByRegistrationId map based on registration respository passed in from class constructor
    init {
      (clientRegistrationRepository as? InMemoryReactiveClientRegistrationRepository)?.toList()?.forEach { reg ->
            val delegate = DelegatingReactiveOAuth2AuthorizedClientProvider(
                getProvidersFor(reg, requestParameterProperties)
            )
            providersByRegistrationId[reg.registrationId] = delegate
        }
    }


    // override authorize function
    override fun authorize(context: OAuth2AuthorizationContext?): Mono<OAuth2AuthorizedClient> {
        context ?: return Mono.empty()

        // get current client registration from context
        val registration = context.clientRegistration

        // if providersByRegistrationId map DOES NOT have provider for given registration id, then assign one
        if (!providersByRegistrationId.containsKey(registration.registrationId)) {
            val delegate = DelegatingReactiveOAuth2AuthorizedClientProvider(
                getProvidersFor(registration, requestParameterProperties)
            )
            providersByRegistrationId[registration.registrationId] = delegate
        }

        // run the authorization function of the provider
        return providersByRegistrationId[registration.registrationId]!!.authorize(context)
    }


    // get provider for the particular client registration
    private fun getProvidersFor(
        registration: ClientRegistration,
        requestParameterProperties: RequestParameterProperties
    ): List<ReactiveOAuth2AuthorizedClientProvider> {

        // get providers for the given client registration id (as passed in from the class constructor)
        val providers = ArrayList(customProvidersByRegistrationId[registration.registrationId] ?: listOf())

        // if grant type is authorisation code, add authorization code provider
        // also add refresh token provider (if 'offline_access' scope is provided)
        if (AuthorizationGrantType.AUTHORIZATION_CODE == registration.authorizationGrantType) {
            providers.add(AuthorizationCodeReactiveOAuth2AuthorizedClientProvider())
            if (registration.scopes.contains("offline_access")) {
                providers.add(
                    createRefreshTokenProvider(registration, requestParameterProperties)
                )
            }
        // otherwise, if grant type is client credentials, add client credentials provider
        } else if (AuthorizationGrantType.CLIENT_CREDENTIALS == registration.authorizationGrantType) {
            providers.add(
                createClientCredentialsProvider(registration, requestParameterProperties)
            )
        }
        return providers
    }


    // create a client credentials provider
    private fun createClientCredentialsProvider(
        registration: ClientRegistration,
        requestParameterProperties: RequestParameterProperties
    ): ClientCredentialsReactiveOAuth2AuthorizedClientProvider {

        // create provider and get extraParameters
        val provider = ClientCredentialsReactiveOAuth2AuthorizedClientProvider()
        val extraParameters = requestParameterProperties.getExtraTokenParameters(registration.registrationId)

        // return provider early if no extraParameters
        if (extraParameters.isEmpty()) {
            return provider
        }

        // create response client, and add extra parameters to it
        val responseClient = WebClientReactiveClientCredentialsTokenResponseClient()
        responseClient.addParametersConverter { extraParameters }

        // add response client into provider
        provider.setAccessTokenResponseClient(responseClient)

        // return provider
        return provider
    }


    // create a refresh token provider
    private fun createRefreshTokenProvider(
        registration: ClientRegistration,
        requestParameterProperties: RequestParameterProperties
    ): RefreshTokenReactiveOAuth2AuthorizedClientProvider {

        // create provider and get extraParameters
        val provider = RefreshTokenReactiveOAuth2AuthorizedClientProvider()
        val extraParameters = requestParameterProperties.getExtraTokenParameters(registration.registrationId)

        // return provider early if no extraParameters
        if (extraParameters.isEmpty()) {
            return provider
        }

        // create response client, and add extra parameters to it
        val responseClient = WebClientReactiveRefreshTokenTokenResponseClient()
        responseClient.addParametersConverter { extraParameters }

        // add response client into provider
        provider.setAccessTokenResponseClient(responseClient)

        // return provider
        return provider
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/