package com.example.bff.auth.handlers

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class LoginFailureHandler : ServerAuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        exchange: WebFilterExchange?,
        exception: AuthenticationException?
    ): Mono<Void> {

        // ensure exchange is not null
        val webExchange = exchange?.exchange ?: return Mono.empty()

        return webExchange.session.flatMap { session ->
            // retrieve the post-login success URI from session or use a default URI
            val postLoginSuccessUri = Mono.justOrEmpty(
                session.attributes["post-login-failure-uri"] as String?
            ).defaultIfEmpty("/default-failure-url")

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