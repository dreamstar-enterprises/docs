package com.example.authorizationserver.auth.sessions

import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.springframework.context.event.EventListener
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.session.events.SessionCreatedEvent
import org.springframework.session.events.SessionDeletedEvent
import org.springframework.session.events.SessionDestroyedEvent
import org.springframework.session.events.SessionExpiredEvent
import org.springframework.session.web.http.SessionEventHttpSessionListenerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/************************************************ SESSION CONFIGURATION ***********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#listening-session-events
// https://docs.spring.io/spring-session/reference/http-session.html#httpsession-httpsessionlistener

@Component
// session event listener
internal class SessionListenerConfig() {

    @Bean
    // for publishing session lifecycle events, to enable application to respond to such events
    fun httpSessionEventPublisher(): HttpSessionEventPublisher {
        return HttpSessionEventPublisher()
    }

    // define individual HttpSessionListener beans
    @Bean
    fun sessionCreatedListener(): HttpSessionListener {
        return object : HttpSessionListener {
            override fun sessionCreated(event: HttpSessionEvent) {
                println("Session created: ${event.session.id}")
            }

            override fun sessionDestroyed(event: HttpSessionEvent) {
                println("Session destroyed: ${event.session.id}")
            }
        }
    }

    @Bean
    fun sessionDeletedListener(): HttpSessionListener {
        return object : HttpSessionListener {
            override fun sessionCreated(event: HttpSessionEvent) {
                println("Session deleted event - Session created: ${event.session.id}")
            }

            override fun sessionDestroyed(event: HttpSessionEvent) {
                println("Session deleted event - Session destroyed: ${event.session.id}")
            }
        }
    }

    @Bean
    fun sessionDestroyedListener(): HttpSessionListener {
        return object : HttpSessionListener {
            override fun sessionCreated(event: HttpSessionEvent) {
                println("Session destroyed event - Session created: ${event.session.id}")
            }

            override fun sessionDestroyed(event: HttpSessionEvent) {
                println("Session destroyed event - Session destroyed: ${event.session.id}")
            }
        }
    }

    @Bean
    fun sessionExpiredListener(): HttpSessionListener {
        return object : HttpSessionListener {
            override fun sessionCreated(event: HttpSessionEvent) {
                println("Session expired event - Session created: ${event.session.id}")
            }

            override fun sessionDestroyed(event: HttpSessionEvent) {
                println("Session expired event - Session destroyed: ${event.session.id}")
            }
        }
    }

    // Create a list of all listeners and inject it into the SessionEventHttpSessionListenerAdapter
    @Bean
    fun httpSessionListeners(
        sessionCreatedListener: HttpSessionListener,
        sessionDeletedListener: HttpSessionListener,
        sessionDestroyedListener: HttpSessionListener,
        sessionExpiredListener: HttpSessionListener
    ): List<HttpSessionListener> {
        return listOf(
            sessionCreatedListener,
            sessionDeletedListener,
            sessionDestroyedListener,
            sessionExpiredListener
        )
    }

    // configure SessionEventHttpSessionListenerAdapter with the list of listeners
    @Bean
    fun sessionEventHttpSessionListenerAdapter(
        httpSessionListeners: List<HttpSessionListener>
    ): SessionEventHttpSessionListenerAdapter {
        return SessionEventHttpSessionListenerAdapter(httpSessionListeners)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/