package com.example.bff.api.controllers

import com.c4_soft.springaddons.security.oidc.starter.reactive.client.SpringAddonsServerLogoutSuccessHandler
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/**************************************************** CONTROLLER ******************************************************/
/**********************************************************************************************************************/

//@RestController
//@RequestMapping("/login-options")
//class LoginOptionsController(
//    clientProps: OAuth2ClientProperties,
//    addonsProperties: SpringAddonsServerLogoutSuccessHandler
//) {
//
//    private var loginOptions: List<LoginOptionDto> = listOf()
//
//    init {
//        val clientAuthority = addonsProperties.client.clientUri.authority
//        loginOptions = clientProps.registration
//            .entries
//            .filter { it.value.authorizationGrantType == "authorization_code" }
//            .map { (key, value) ->
//                val label = value.provider
//                val loginUri = "${addonsProperties.client.clientUri}/oauth2/authorization/$key"
//                val providerId = clientProps.registration[key]?.provider
//                val providerIssuerAuthority = clientProps.provider[providerId]?.issuerUri?.let { URI.create(it).authority }
//                LoginOptionDto(label, loginUri, clientAuthority == providerIssuerAuthority)
//            }
//    }
//
//    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
//    fun getLoginOptions(): Mono<List<LoginOptionDto>> {
//        return Mono.just(loginOptions)
//    }
//
//    data class LoginOptionDto(
//        @field:NotEmpty val label: String,
//        @field:NotEmpty val loginUri: String,
//        val isSameAuthority: Boolean
//    )
//}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/