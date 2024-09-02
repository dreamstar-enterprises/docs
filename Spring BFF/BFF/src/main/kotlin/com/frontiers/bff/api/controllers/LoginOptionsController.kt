package com.frontiers.bff.api.controllers

import com.frontiers.bff.props.ServerProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/**************************************************** CONTROLLER ******************************************************/
/**********************************************************************************************************************/

@RestController
@RequestMapping("/login-options")
internal class LoginOptionsController(
    private val serverProperties: ServerProperties,
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
) {

    private var loginOptions: List<LoginOptionDto>? = null

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getLoginOptions(): Mono<List<LoginOptionDto>> {
        if (loginOptions == null || loginOptions!!.isEmpty()) {

            val clientAuthority = URI.create(serverProperties.reverseProxyUri).authority
            val clientRegistrations = (clientRegistrationRepository as? InMemoryReactiveClientRegistrationRepository)?.toList()
                ?: emptyList()

            loginOptions = clientRegistrations
                .filter { it.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE }
                .mapNotNull { registration ->
                    // client name
                    val label = registration.clientName
                    // internal oauth2 request redirection filter
                    val oauth2Redirection = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                    // endpoint that redirects to the auth server
                    val loginUri = "${serverProperties.bffServerUri}$oauth2Redirection/${registration.registrationId}"
                    // checks if issuer authority and reverse proxy authority are the same
                    val providerIssuerAuthority = registration.providerDetails.issuerUri?.toString()
                        ?.let { URI.create(it).authority }
                    LoginOptionDto(label, loginUri, clientAuthority == providerIssuerAuthority)
                }
        }

        return Mono.just(loginOptions!!)
    }

    data class LoginOptionDto(
        @field:NotEmpty val label: String,
        @field:NotEmpty val loginUri: String,
        @get:JsonProperty("isSameAuthority") val isSameAuthority: Boolean
    )
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/