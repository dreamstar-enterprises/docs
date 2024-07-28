package com.example.timesheetapi.auth.security.handlers

import com.example.timesheetapi.api.utilities.json.JacksonConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

//* FOR AUTHENTICATION FAILURES *//

@Component
internal class AuthenticationEntryPoint() : ServerAuthenticationEntryPoint {

    private val objectMapper: ObjectMapper = JacksonConfiguration.objectMapper

    override fun commence(exchange: ServerWebExchange?, ex: AuthenticationException?): Mono<Void> {

        val response = exchange?.response ?: return Mono.empty()
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON
        response.headers.add("Authentication-Status", "Unauthorized")

        val errorMessage = ex?.message ?: "Unauthorized"
        val errorResponse = objectMapper.writeValueAsString(mapOf("Authentication Failed" to errorMessage))
        val responseBody = response.bufferFactory().wrap(errorResponse.toByteArray())

        return response.writeWith(Mono.just(responseBody))
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/