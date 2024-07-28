package com.example.authorizationserver.auth.security.handlers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.log.LogMessage
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.web.authentication.NullRememberMeServices
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class DocDbSuccessHandler(
    private val customSecurityContextRepository: SecurityContextRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : SavedRequestAwareAuthenticationSuccessHandler() {

    private val rememberMeServices = NullRememberMeServices()

     override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authResult: Authentication,
    ) {

         /* session management */
         // if request has no associated session, do not create a new one
         val session: HttpSession? = request.getSession(false)

         session?.let {
             // change session id for security reasons (mitigates against session fixation attacks)
             request.changeSessionId()

             // set session timeout to 5s!
             request.getSession()?.setMaxInactiveInterval(5)

         }

         /* security context */
        // set the authentication into the SecurityContext
        val securityContextHolderStrategy : SecurityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()
        val context: SecurityContext = securityContextHolderStrategy.createEmptyContext()
        context.setAuthentication(authResult)
        securityContextHolderStrategy.setContext(context)

        // save the security context
        customSecurityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response)

        // log results
        if (logger.isDebugEnabled) {
            logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authResult))
        }

        // use rememberMe services
        rememberMeServices.loginSuccess(request, response, authResult)

        // use event publisher
        eventPublisher.publishEvent(InteractiveAuthenticationSuccessEvent(authResult, this::class.java))

         println("REDIRECTING !!!!")

        // carry out final actions
        super.onAuthenticationSuccess(request, response, authResult)

    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/