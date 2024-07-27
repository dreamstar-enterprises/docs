package com.example.authorizationserver.auth.security.handlers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.web.authentication.NullRememberMeServices
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class DocDbFailureHandler(): SimpleUrlAuthenticationFailureHandler() {

    private val rememberMeServices = NullRememberMeServices()

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {

        /* security context */
        // erase security context
        val securityContextHolderStrategy : SecurityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()
        securityContextHolderStrategy.clearContext()

        // log results
        logger.trace("Failed to process authentication request", exception)
        logger.trace("Cleared SecurityContextHolder")
        logger.trace("Handling authentication failure")

        // use rememberMe services
        rememberMeServices.loginFail(request, response)

        // carry out final actions

        super.onAuthenticationFailure(request, response, exception)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/