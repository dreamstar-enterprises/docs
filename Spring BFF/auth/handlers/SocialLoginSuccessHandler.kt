package com.example.authorizationserver.auth.security.handlers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

// more here:
// https://medium.com/@d.snezhinskiy/building-sso-based-on-spring-authorization-server-part-3-of-3-b0b31feb2b6e
// https://docs.spring.io/spring-authorization-server/reference/guides/how-to-social-login.html#advanced-use-cases-capture-users


//* FOR SOCIAL LOGINS *//

@Configuration
internal class SocialLoginSuccessHandlerConfiguration {

    @Bean
    internal fun socialLoginAuthenticationSuccessHandler(
        handler: UserServiceOidcUserHandler?
    ): SocialLoginSuccessHandler {

        val authenticationSuccessHandler = SocialLoginSuccessHandler()
        // check if handler is not null before setting it
        handler?.let {
            authenticationSuccessHandler.setOidcUserHandler { user -> it.accept(user) }
        }
        return authenticationSuccessHandler
    }
}

@Component
internal class SocialLoginSuccessHandler : AuthenticationSuccessHandler {

    private val delegate: AuthenticationSuccessHandler = SavedRequestAwareAuthenticationSuccessHandler()

    // default behaviour is to do nothing (not invoke it)
    private var oauth2UserHandler: (OAuth2User) -> Unit = {}

    // default behaviour is to invoke in this pattern
    private var oidcUserHandler: (OidcUser) -> Unit = { user -> oauth2UserHandler.invoke(user) }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication
    ) {

        println("AUTHENTICATION SUCCESS!!!")

        if (authentication is OAuth2AuthenticationToken) {
            val principal = authentication.principal
            when (principal) {
                is OidcUser -> oidcUserHandler.invoke(principal)
                is OAuth2User -> oauth2UserHandler.invoke(principal)
            }
        }

        delegate.onAuthenticationSuccess(request, response, authentication)
    }

    fun setOAuth2UserHandler(oauth2UserHandler: (OAuth2User) -> Unit) {
        this.oauth2UserHandler = oauth2UserHandler
    }

    fun setOidcUserHandler(oidcUserHandler: (OidcUser) -> Unit) {
        this.oidcUserHandler = oidcUserHandler
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/