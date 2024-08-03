package com.example.authorizationserver.auth.filters

import com.example.authorizationserver.auth.converters.BasicJSONConverter
import com.example.authorizationserver.auth.handlers.CustomSavedRequestAwareAuthenticationSuccessHandler
import com.example.authorizationserver.auth.handlers.CustomSimpleUrlAuthenticationFailureHandler
import com.example.authorizationserver.auth.sessions.CustomSessionAuthenticationStrategy
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
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class DocDbAuthenticationFilter(
    private val servletAuthenticationManager: AuthenticationManager,
    private val customSessionAuthenticationStrategy: CustomSessionAuthenticationStrategy,
    customSavedRequestAwareAuthenticationSuccessHandler: CustomSavedRequestAwareAuthenticationSuccessHandler,
    customSimpleUrlAuthenticationFailureHandler: CustomSimpleUrlAuthenticationFailureHandler,
    customSecurityContextRepository: SecurityContextRepository,
) : AbstractAuthenticationProcessingFilter(RequestMatcher { request ->
            // specify authentication path and other custom conditions here
            val loginUrl = "/login"
            request.servletPath == loginUrl && request.method == HttpMethod.POST.name()
        }
    ){

    private val authenticationConverter = BasicJSONConverter()
    private val continueChainBeforeSuccessfulAuthentication = false

    init{
        // initialise class variables
        this.setAuthenticationManager(servletAuthenticationManager)
        this.setSecurityContextRepository(customSecurityContextRepository)

        // success handler
        val successHandler = customSavedRequestAwareAuthenticationSuccessHandler
        this.setAuthenticationSuccessHandler(successHandler)

        // failure handler
        val failureHandler = customSimpleUrlAuthenticationFailureHandler
        this.setAuthenticationFailureHandler(failureHandler)
    }


    // main filter logic (don't user 'super' for this!)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        if (!requiresAuthentication(httpRequest, httpResponse)) {
            chain.doFilter(httpRequest, response)
            return
        }
        try {
            val authenticationResult = attemptAuthentication(httpRequest, httpResponse)
                ?: // return immediately as subclass has indicated that it hasn't completed
                return
            customSessionAuthenticationStrategy.onAuthentication(authenticationResult, httpRequest, httpResponse)
            // Authentication success
            if (this.continueChainBeforeSuccessfulAuthentication) {
                chain.doFilter(httpRequest, httpResponse)
            }
            successfulAuthentication(httpRequest, httpResponse, chain, authenticationResult)
        } catch (failed: InternalAuthenticationServiceException) {
            logger.error("An internal error occurred while trying to authenticate the user.", failed)
            unsuccessfulAuthentication(httpRequest, httpResponse, failed)
        } catch (ex: AuthenticationException) {
            // Authentication failed
            unsuccessfulAuthentication(httpRequest, httpResponse, ex)
        }
    }


    // attempt authentication (convert request, and then authenticate)
    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication? {
        val authenticationRequest = authenticationConverter.convert(request)
        return authenticationRequest?.let {
            servletAuthenticationManager.authenticate(it)
        }
    }


    // perform on successful authentication
    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {

        // call the parent class method to perform default actions
        super.successfulAuthentication(request, response, chain, authResult)

    }


    // perform on failed authentication
    override fun unsuccessfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        failed: AuthenticationException
    ) {

        // call the parent class method to perform default actions
        super.unsuccessfulAuthentication(request, response, failed)

    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/