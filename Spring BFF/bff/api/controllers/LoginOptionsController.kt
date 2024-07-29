package com.example.bff.api.controllers

import com.c4_soft.springaddons.security.oidc.starter.properties.SpringAddonsOidcProperties
import jakarta.validation.constraints.NotEmpty
import org.springframework.beans.factory.annotation.Value
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

    @Value("\${reverse-proxy-uri}")
    private lateinit var reverseProxyUri: String

    @Value("\${bff-uri}")
    private lateinit var bffUri: String

    private var loginOptions: List<LoginOptionDto>? = null

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getLoginOptions(): Mono<List<LoginOptionDto>> {
        if (loginOptions == null || loginOptions!!.isEmpty()) {

            val clientAuthority = URI.create(reverseProxyUri).authority
            val clientRegistrations = clientRegistrationRepository.toList()

            loginOptions = clientRegistrations
                .filter { it.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE }
                .map { registration ->
                    val label = registration.clientName
                    // internal endpoint that redirects to the auth server for authorization
                    val loginUri = "$bffUri/oauth2/authorization/${registration.registrationId}"
                    val providerIssuerAuthority = registration.providerDetails.issuerUri?.toString()
                        ?.let { URI.create(it).authority }
                    println("loginURI, $loginUri")
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