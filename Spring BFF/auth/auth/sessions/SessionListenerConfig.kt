package com.example.authorizationserver.auth.sessions

import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.session.events.SessionCreatedEvent
import org.springframework.session.events.SessionDeletedEvent
import org.springframework.session.events.SessionDestroyedEvent
import org.springframework.session.events.SessionExpiredEvent
import org.springframework.session.web.http.SessionEventHttpSessionListenerAdapter

/**********************************************************************************************************************/
/************************************************ SESSION CONFIGURATION ***********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#listening-session-events
// https://docs.spring.io/spring-session/reference/http-session.html#httpsession-httpsessionlistener

@Configuration
// session event listener
internal class SessionListenerConfig() {

    @Bean
    // for publishing session lifecycle events, to enable application to respond to such events
    fun httpSessionEventPublisher(): HttpSessionEventPublisher {
        return HttpSessionEventPublisher()
    }

    @Bean
    fun sessionEventHttpSessionListenerAdapter(): SessionEventHttpSessionListenerAdapter {
        val listeners: MutableList<HttpSessionListener> = ArrayList()
        listeners.add(HttpEventListener())
        return SessionEventHttpSessionListenerAdapter(listeners)
    }

    // http event listener
    inner class HttpEventListener : HttpSessionListener {
        override fun sessionCreated(event: HttpSessionEvent) {
            println("HTTP Session created: ${event.session.id}")
        }

        override fun sessionDestroyed(event: HttpSessionEvent) {
            println("HTTP Session destroyed: ${event.session.id}")
        }
    }

    @EventListener
    fun processSessionCreatedEvent(event: SessionCreatedEvent) {
        // Log or print information about the created session
        println("Session created: ${event.sessionId}")
        // Logic for session created event
    }

    @EventListener
    fun processSessionDeletedEvent(event: SessionDeletedEvent) {
        // Log or print information about the deleted session
        println("Session deleted: ${event.sessionId}")
        // Logic for session deleted event
    }

    @EventListener
    fun processSessionExpiredEvent(event: SessionExpiredEvent) {
        // Log or print information about the expired session
        println("Session expired: ${event.sessionId}")
        // Logic for session expired event
    }

    @EventListener
    fun processSessionDestroyedEvent(event: SessionDestroyedEvent) {
        // Log or print information about the destroyed session
        println("Session expired: ${event.sessionId}")
        // Logic for session destoyed event
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/