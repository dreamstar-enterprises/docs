package com.example.authorizationserver.auth.security.filters

import com.example.authorizationserver.auth.security.converters.BasicJSONConverter
import com.example.authorizationserver.auth.security.handlers.DocDbFailureHandler
import com.example.authorizationserver.auth.security.handlers.DocDbSuccessHandler
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class DocDbAuthenticationFilter(
    authenticationManager: AuthenticationManager,
    private val docDbSuccessHandler : DocDbSuccessHandler,
    private val docDbFailureHandler : DocDbFailureHandler,
) : AbstractAuthenticationProcessingFilter(RequestMatcher { request ->
            // specify authentication path and other custom conditions here
            val loginUrl = "/login"
            request.servletPath == loginUrl && request.method == HttpMethod.POST.name()
        }
    ){

    private val authenticationConverter = BasicJSONConverter()

    init{
        super.setAuthenticationManager(authenticationManager)
    }

    // main filter logic
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        filterChain: FilterChain
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // is authentication required?
        if (!requiresAuthentication(request, response)) {

            println("AUTHENTICATION NOT REQUIRED !!!!")
            filterChain.doFilter(request, response)
            return
        }
        try {
            val authenticationResult = attemptAuthentication(httpRequest, httpResponse)
            if (authenticationResult == null) {
                println("NULL AUTHENTICATED !!!!")
                // continue with rest of filter chain!
                filterChain.doFilter(request, response)
                return
            }
            println("JUST AUTHENTICATED !!!!")
            // authentication success
            successfulAuthentication(httpRequest, httpResponse, filterChain, authenticationResult)

        } catch (failed: InternalAuthenticationServiceException) {

            // authentication failed
            this.logger.error("An internal error occurred while trying to authenticate the user.", failed);
            unsuccessfulAuthentication(request, response, failed);

        } catch (ex: AuthenticationException) {

            // authentication failed
            unsuccessfulAuthentication(httpRequest, httpResponse, ex)
        }
    }

    // attempt authentication (convert request, and then authenticate)
    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication? {
        val authenticationRequest = authenticationConverter.convert(request)
        if(authenticationRequest !== null) {
            return authenticationManager.authenticate(authenticationRequest)
        } else {
            return null
        }
    }

    // perform on successful authentication
    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {
        docDbSuccessHandler.onAuthenticationSuccess(request, response, authResult)
    }

    // perform on failed authentication
    override fun unsuccessfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        failed: AuthenticationException
    ) {
        docDbFailureHandler.onAuthenticationFailure(request, response, failed)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/