package com.example.bff.auth.handlers

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class LoginSuccessHandler : ServerAuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        exchange: WebFilterExchange?,
        authentication: Authentication?
    ): Mono<Void> {

        // ensure exchange is not null
        val webExchange = exchange?.exchange ?: return Mono.empty()

        return webExchange.session.flatMap { session ->
            // retrieve the post-login success URI from session or use a default URI
            val postLoginSuccessUri = Mono.justOrEmpty(
                session.attributes["post-login-success-uri"] as String?
            ).defaultIfEmpty("/default-success-url")

            // apply the redirect and return Mono<Void>
            postLoginSuccessUri.flatMap { uri ->
                val response = exchange.exchange.response
                response.statusCode = HttpStatus.FOUND
                response.headers.location = URI.create(uri)
                response.setComplete()
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/