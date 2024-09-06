package com.frontiers.bff.auth.handlers.sessions

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.web.server.authentication.InvalidateLeastUsedServerMaximumSessionsExceededHandler
import org.springframework.security.web.server.authentication.MaximumSessionsContext
import org.springframework.security.web.server.authentication.PreventLoginServerMaximumSessionsExceededHandler
import org.springframework.security.web.server.authentication.ServerMaximumSessionsExceededHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import org.springframework.web.server.session.DefaultWebSessionManager
import org.springframework.web.server.session.WebSessionManager
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

//* FOR WHEN MAXIMUM SESSIONS ARE EXCEEDED *//

/**
 * Customizes the behavior when the maximum number of concurrent sessions is exceeded.
 * It offers two strategies based on the preventLogin flag.
 * If preventLogin is true, it prevents additional logins by using PreventLoginServerMaximumSessionsExceededHandler.
 * If preventLogin is false, it invalidates the least used sessions to allow new logins by using
 * InvalidateLeastUsedServerMaximumSessionsExceededHandler
 */
@Component
internal class CustomMaximumSessionsExceededHandler(
    @Qualifier(WebHttpHandlerBuilder.WEB_SESSION_MANAGER_BEAN_NAME)
    private val webSessionManager: WebSessionManager
) : ServerMaximumSessionsExceededHandler {

    private val preventLogin = true // set accordingly

    override fun handle(context: MaximumSessionsContext): Mono<Void> {
        // choose the implementation based on the flag
        return if (preventLogin) {
            PreventLoginServerMaximumSessionsExceededHandler().handle(context)
        } else {
            val sessionStore = (webSessionManager as DefaultWebSessionManager).sessionStore
            InvalidateLeastUsedServerMaximumSessionsExceededHandler(sessionStore).handle(context)
        }
    }
}
/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/