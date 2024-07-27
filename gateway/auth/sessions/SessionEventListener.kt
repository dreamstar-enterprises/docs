package com.example.gateway.auth.sessions

import org.springframework.context.event.EventListener
import org.springframework.session.events.SessionCreatedEvent
import org.springframework.session.events.SessionDeletedEvent
import org.springframework.session.events.SessionExpiredEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/************************************************** SESSION CONFIGURATION *********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/reactive-redis-indexed.html#listening-session-events

@Component
// session event listener
internal class SessionEventListener {

    @EventListener
    fun processSessionCreatedEvent(event: SessionCreatedEvent): Mono<Void> {
        return Mono.fromRunnable {
            // Log or print information about the created session
            println("Session created: ${event.sessionId}")
            // Your logic for session created event
        }
    }

    @EventListener
    fun processSessionDeletedEvent(event: SessionDeletedEvent): Mono<Void> {
        return Mono.fromRunnable {
            // Log or print information about the deleted session
            println("Session deleted: ${event.sessionId}")
            // Your logic for session deleted event
        }
    }

    @EventListener
    fun processSessionExpiredEvent(event: SessionExpiredEvent): Mono<Void> {
        return Mono.fromRunnable {
            // Log or print information about the expired session
            println("Session expired: ${event.sessionId}")
            // Your logic for session expired event
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/