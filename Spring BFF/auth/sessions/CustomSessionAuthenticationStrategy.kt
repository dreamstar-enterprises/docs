package com.example.authorizationserver.auth.sessions

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.web.authentication.session.*
import org.springframework.util.Assert

/**********************************************************************************************************************/
/****************************************************** SESSION STRATEGY **********************************************/
/**********************************************************************************************************************/

@Configuration
internal class CustomSessionAuthenticationStrategy(
    private val sessionRegistry: SessionRegistry
) : SessionAuthenticationStrategy {

    private val logger: Log = LogFactory.getLog(javaClass)

    private val delegateStrategies: List<SessionAuthenticationStrategy> by lazy {
        val strategies = listOf(
            concurrentSessionControlAuthenticationStrategy(),
            sessionFixationProtectionStrategy(),
            registerSessionAuthenticationStrategy(),
            changeSessionIdAuthenticationStrategy()
        )
        Assert.notEmpty(strategies, "delegateStrategies cannot be null or empty")
        for (strategy in strategies) {
            Assert.notNull(strategy) { "delegateStrategies cannot contain null entries. Got $strategies" }
        }
        strategies
    }

    @Bean
    fun concurrentSessionControlAuthenticationStrategy(): ConcurrentSessionControlAuthenticationStrategy {
        val strategy = ConcurrentSessionControlAuthenticationStrategy(sessionRegistry)
        strategy.setMaximumSessions(1) // set maximum sessions per user
        strategy.setExceptionIfMaximumExceeded(true) // prevents login if maximum sessions are exceeded
        return strategy
    }

    @Bean
    fun sessionFixationProtectionStrategy(): SessionFixationProtectionStrategy {
        val strategy = SessionFixationProtectionStrategy()
        strategy.setMigrateSessionAttributes(true) // migrates session attributes
        strategy.setAlwaysCreateSession(true) // always create a new session
        return strategy
    }

    @Bean
    fun registerSessionAuthenticationStrategy(): RegisterSessionAuthenticationStrategy {
        val strategy = RegisterSessionAuthenticationStrategy(sessionRegistry)
        return strategy
    }

    @Bean
    fun changeSessionIdAuthenticationStrategy(): ChangeSessionIdAuthenticationStrategy {
        val strategy = ChangeSessionIdAuthenticationStrategy()
        strategy.setAlwaysCreateSession(true)
        return strategy
    }

    @Bean
    fun compositeSessionAuthenticationStrategy(): CompositeSessionAuthenticationStrategy {
        return CompositeSessionAuthenticationStrategy(delegateStrategies)
    }

    override fun onAuthentication(authentication: Authentication?, request: HttpServletRequest?, response: HttpServletResponse?) {
        var currentPosition = 0
        val size: Int = this.delegateStrategies.size
        for (delegate in this.delegateStrategies) {
            if (this.logger.isTraceEnabled) {
                this.logger.trace(
                    "Preparing session with ${delegate.javaClass.simpleName} (${++currentPosition}/$size)"
                )
            }
            delegate.onAuthentication(authentication, request, response)
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/