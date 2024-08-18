package com.example.bff.auth.handlers.oauth2

import com.example.bff.auth.redirects.OAuth2ServerRedirectStrategy
import com.example.bff.props.OAuth2RedirectionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.web.server.ServerRedirectStrategy

/**********************************************************************************************************************/
/**************************************************** REDIRECTION STRATEGIES ******************************************/
/**********************************************************************************************************************/

/**
 * Provides a way to set up and manage the PreAuthorizationCodeOAuth2ServerRedirectStrategy bean within the
 * Spring application context, enabling it to be used for any pre-authorization re-direction
 */
@Configuration
internal class PreAuthorizationCodeOAuth2ServerRedirectStrategyConfig(){

    @Bean
    // construct the redirect strategy as a function
    internal fun preAuthorizationCodeOAuth2RedirectStrategy(
        oauth2RedirectionProperties: OAuth2RedirectionProperties,
    ): PreAuthorizationCodeServerRedirectStrategy {
        return PreAuthorizationCodeOAuth2ServerRedirectStrategy(
            oauth2RedirectionProperties.preAuthorizationCode
        )
    }

    // pre-defined class
    internal class PreAuthorizationCodeOAuth2ServerRedirectStrategy(
        defaultStatus: HttpStatus
    ) : OAuth2ServerRedirectStrategy(
        defaultStatus
    ), PreAuthorizationCodeServerRedirectStrategy
}

/**********************************************************************************************************************/
/***************************************************** INTERFACES *****************************************************/
/**********************************************************************************************************************/

interface PreAuthorizationCodeServerRedirectStrategy : ServerRedirectStrategy

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/