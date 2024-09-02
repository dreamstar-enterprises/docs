package com.frontiers.bff.auth.sessions

import org.slf4j.LoggerFactory
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


/**
 * Listens for session-related events, designed to handle events when a session is created, deleted, or expired.
 * Useful for tracking session lifecycles for auditing, security, or resource management purposes.
 */
@Component
// session event listener
internal class SessionListenerConfig {

    private val logger = LoggerFactory.getLogger(SessionListenerConfig::class.java)

    // copies attributes from old session when a new session is created
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
            logger.info("Session deleted: ${event.sessionId}")
            // logic for session deleted event
        }
    }

    @EventListener
    fun processSessionExpiredEvent(event: SessionExpiredEvent): Mono<Void> {
        return Mono.fromRunnable {
            logger.info("Session expired: ${event.sessionId}")
            // logic for session expired event
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/