package com.example.bff.auth.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.io.IOException

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

//* FOR AUTHENTICATION FAILURES *//

@Component
internal class AuthenticationHandler : ServerAuthenticationEntryPoint {
    private val objectMapper = ObjectMapper()

    @Throws(IOException::class)
    override fun commence(exchange: ServerWebExchange?, ex: AuthenticationException?): Mono<Void> {

        val response = exchange?.response ?: return Mono.empty()
        response.statusCode = HttpStatus.FORBIDDEN
        response.headers.contentType = MediaType.APPLICATION_JSON
        response.headers.add("Authentication-Status", "Forbidden")

        val errorMessage = ex?.message ?: "Authentication Failed"
        val errorResponse = objectMapper.writeValueAsString(mapOf("Authentication Failed" to errorMessage))
        val responseBody = response.bufferFactory().wrap(errorResponse.toByteArray())

        return response.writeWith(Mono.just(responseBody))
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/