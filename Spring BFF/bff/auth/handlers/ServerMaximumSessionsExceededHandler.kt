package com.example.bff.auth.handlers

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.security.web.server.authentication.InvalidateLeastUsedServerMaximumSessionsExceededHandler
import org.springframework.security.web.server.authentication.PreventLoginServerMaximumSessionsExceededHandler
import org.springframework.security.web.server.authentication.ServerMaximumSessionsExceededHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import org.springframework.web.server.session.DefaultWebSessionManager
import org.springframework.web.server.session.WebSessionManager

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

//* FOR WHEN MAXIMUM SESSIONS ARE EXCEEDED *//

@Component
class ServerMaximumSessionsExceededHandler {

    @Bean
    fun maximumSessionsExceededHandler(
        @Qualifier(WebHttpHandlerBuilder.WEB_SESSION_MANAGER_BEAN_NAME)
        webSessionManager: WebSessionManager
    ): ServerMaximumSessionsExceededHandler {
        // Implement your condition for choosing the handler
        val preventLogin = true // Or dynamically determine this value
        if (preventLogin) {
            return PreventLoginServerMaximumSessionsExceededHandler()
        }
        val sessionStore = (webSessionManager as DefaultWebSessionManager).sessionStore
        return InvalidateLeastUsedServerMaximumSessionsExceededHandler(sessionStore)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/