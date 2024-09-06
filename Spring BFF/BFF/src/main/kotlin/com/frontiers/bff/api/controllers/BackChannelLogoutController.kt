package com.frontiers.bff.api.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

/**********************************************************************************************************************/
/**************************************************** CONTROLLER ******************************************************/
/**********************************************************************************************************************/

// see more here:
// https://auth0.com/docs/authenticate/login/logout/back-channel-logout

@RestController
@RequestMapping("/logout/connect/back-channel/in-house-auth-server")
internal class BackChannelLogoutController {

    private val logger = LoggerFactory.getLogger(BackChannelLogoutController::class.java)

    @PostMapping
    fun handleLogout(@RequestBody logoutRequest: Map<String, Any>): ResponseEntity<Void> {
        // Handle the logout notification here
        // For example, invalidate user sessions or perform other cleanup

        logger.info("Received back-channel logout request: $logoutRequest")

        // Return HTTP 200 OK to acknowledge receipt of the logout request
        return ResponseEntity.ok().build()
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/