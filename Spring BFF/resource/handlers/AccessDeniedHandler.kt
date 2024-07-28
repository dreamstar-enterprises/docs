package com.example.timesheetapi.auth.security.handlers

import com.example.timesheetapi.api.utilities.json.JacksonConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

//* FOR AUTHORIZAION FAILURES *//

@Component
internal class AccessDeniedHandler : ServerAccessDeniedHandler {

    private val objectMapper: ObjectMapper = JacksonConfiguration.objectMapper

    override fun handle(
        exchange: ServerWebExchange?,
        denied: org.springframework.security.access.AccessDeniedException?
    ): Mono<Void> {

        val response = exchange?.response ?: return Mono.empty()
        response.statusCode = HttpStatus.FORBIDDEN

        response.headers.contentType = MediaType.APPLICATION_JSON
        response.headers.add("Authorization-Status", "Forbidden")

        val errorMessage = denied?.message ?: "Access Denied"
        val errorResponse = objectMapper.writeValueAsString(mapOf("Authorization Failed" to errorMessage))
        val responseBody = response.bufferFactory().wrap(errorResponse.toByteArray())

        return response.writeWith(Mono.just(responseBody))
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/