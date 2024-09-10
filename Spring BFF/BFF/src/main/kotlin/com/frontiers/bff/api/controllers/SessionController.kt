package com.frontiers.bff.api.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/**************************************************** CONTROLLER ******************************************************/
/**********************************************************************************************************************/

@RestController
@RequestMapping("/session")
internal class SessionController {

    @GetMapping("/refresh")
    fun refreshSession(exchange: ServerWebExchange): Mono<Void> {
        return exchange.session.flatMap { session ->
            session.changeSessionId()
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/