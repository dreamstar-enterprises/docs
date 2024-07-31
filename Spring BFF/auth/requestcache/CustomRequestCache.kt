package com.example.authorizationserver.auth.security.requestcache

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.PortResolver
import org.springframework.security.web.PortResolverImpl
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.SavedRequest
import org.springframework.security.web.util.UrlUtils
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

/**********************************************************************************************************************/
/******************************************************* REQUEST CACHE ************************************************/
/**********************************************************************************************************************/

@Component
internal class CustomRequestCache : HttpSessionRequestCache()  {

    init {
        // allows selective use of save request so that only those with 'redirect_uri' parameter are cached
        this.setRequestMatcher { request ->
            StringUtils.hasText(request.getParameter("redirect_uri"))
        }
    }

    override fun saveRequest(request: HttpServletRequest, response: HttpServletResponse) {
            println("Saving request to ${request.requestURI}")
            super.saveRequest(request, response)
    }

    override fun getMatchingRequest(request: HttpServletRequest, response: HttpServletResponse): HttpServletRequest? {
        println("Getting matching request for ${request.requestURI}")
        return super.getMatchingRequest(request, response)
    }

    override fun removeRequest(request: HttpServletRequest, response: HttpServletResponse) {
        println("Removing request for ${request.requestURI}")
        super.removeRequest(request, response)
    }

    override fun getRequest(request: HttpServletRequest, response: HttpServletResponse): SavedRequest? {
        println("Getting request for ${request.requestURI}")
        return super.getRequest(request, response)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/