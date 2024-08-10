package com.example.bff.api.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

/**********************************************************************************************************************/
/**************************************************** CONTROLLER ******************************************************/
/**********************************************************************************************************************/

@RestController
@RequestMapping("/logout/connect/back-channel/in-house-auth-server")
class BackChannelLogoutController {

    @PostMapping
    fun handleLogout(@RequestBody logoutRequest: Map<String, Any>): ResponseEntity<Void> {
        // Handle the logout notification here
        // For example, invalidate user sessions or perform other cleanup

        println("Received back-channel logout request: $logoutRequest")

        // Return HTTP 200 OK to acknowledge receipt of the logout request
        return ResponseEntity.ok().build()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/