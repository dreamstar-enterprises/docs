package com.frontiers.bff.api.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**********************************************************************************************************************/
/**************************************************** CONTROLLER ******************************************************/
/**********************************************************************************************************************/

@RestController
internal class FallbackController {

    @GetMapping("/fallback")
    fun fallback(): ResponseEntity<Map<String, String>> {
        val responseBody = mapOf(
            "status" to "SERVICE_UNAVAILABLE",
            "message" to "Gateway fallback occurred. The service might be temporarily unavailable."
        )

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseBody)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/