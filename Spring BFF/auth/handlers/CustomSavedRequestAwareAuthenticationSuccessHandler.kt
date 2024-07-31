package com.example.authorizationserver.auth.security.handlers

import com.example.authorizationserver.auth.security.requestcache.CustomRequestCache
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class CustomSavedRequestAwareAuthenticationSuccessHandler(
    customRequestCache: CustomRequestCache
) : SavedRequestAwareAuthenticationSuccessHandler() {

    init {
        // set the custom request cache here
        super.setRequestCache(customRequestCache)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/