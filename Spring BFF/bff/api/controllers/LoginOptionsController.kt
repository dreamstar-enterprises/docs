package com.example.bff.api.controllers

import com.c4_soft.springaddons.security.oidc.starter.properties.SpringAddonsOidcProperties
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
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
    private val clientRegistrationRepository: InMemoryReactiveClientRegistrationRepository,
    private val addonsProperties: SpringAddonsOidcProperties
) {

    private var loginOptions: List<LoginOptionDto>? = null

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getLoginOptions(): Mono<List<LoginOptionDto>> {
        if (loginOptions == null || loginOptions!!.isEmpty()) {

            val clientAuthority = addonsProperties.client.clientUri.authority

            val clientRegistrations = clientRegistrationRepository.toList()
            loginOptions = clientRegistrations
                .filter { it.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE }
                .map { registration ->
                    val label = registration.clientName
                    val loginUri = registration.redirectUri
                    val providerIssuerAuthority = registration.providerDetails.configurationMetadata["issuer"]?.toString()?.let { URI.create(it).authority }
                    println("Label: $label")
                    println("LoginUri: $loginUri")
                    println("providerIssuerAuthority: $providerIssuerAuthority")
                    LoginOptionDto(label, loginUri, clientAuthority == providerIssuerAuthority)
                }
        }

        return Mono.just(loginOptions!!)
    }


    data class LoginOptionDto(
        @field:NotEmpty val label: String,
        @field:NotEmpty val loginUri: String,
        val isSameAuthority: Boolean
    )
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/