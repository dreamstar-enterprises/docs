package com.example.authorizationserver.auth.security.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.io.IOException

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

//* FOR AUTHORIZAION FAILURES *//

@Component
internal class DefaultAccessDeniedHandler : AccessDeniedHandler {
    private val objectMapper = ObjectMapper()

    @Throws(IOException::class)
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.setHeader("Authorization-Status", "Forbidden")

        val errorMessage = accessDeniedException.message ?: "Access Denied"
        val errorResponse = mapOf("Authorization Failed" to errorMessage)
        val jsonResponse = objectMapper.writeValueAsString(errorResponse)

        response.writer.write(jsonResponse)
        response.writer.flush()
        response.writer.close()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/