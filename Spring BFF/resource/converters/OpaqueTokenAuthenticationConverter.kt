package com.example.timesheetapi.auth.security.converters

import com.example.timesheetapi.api.utilities.functions.toMap
import com.example.timesheetapi.auth.security.objects.user.CustomOAuth2AuthenticatedPrincipal
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.util.stream.Collectors

/**********************************************************************************************************************/
/********************************************* AUTHENTICATION CONVERTER ***********************************************/
/**********************************************************************************************************************/

@Component
internal class CustomReactiveOpaqueTokenAuthenticationConverter2
    : ReactiveOpaqueTokenAuthenticationConverter {

    override fun convert(
        introspectedToken: String?,
        authenticatedPrincipal: OAuth2AuthenticatedPrincipal
    ): Mono<Authentication> {

        // get attributes
        val attributes = authenticatedPrincipal.attributes

        // get authorities
        var authorities: Collection<GrantedAuthority?>? = AuthorityUtils.NO_AUTHORITIES
        if (authenticatedPrincipal.attributes.containsKey("authorities")) {
            authorities =
                (authenticatedPrincipal.attributes["authorities"] as List<String?>?)!!.stream()
                    .map { auth: String? ->
                        SimpleGrantedAuthority(
                            auth
                        )
                    }
                    .collect(Collectors.toUnmodifiableSet())
        }

        // get username
        var username: String? = null
        if (attributes.containsKey("username")
            && StringUtils.hasText(attributes["username"] as String?)
        ) {
            username = attributes["username"] as String?
        }

        // create access token
        val accessToken = OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            introspectedToken,
            authenticatedPrincipal.getAttribute(IdTokenClaimNames.IAT),
            authenticatedPrincipal.getAttribute(IdTokenClaimNames.EXP)
        )

        // create authentication principal object
        val customOAuth2User = CustomOAuth2AuthenticatedPrincipal(
            username!!,
            authorities!!,
            attributes
        )

        // create authentication object
        val bearerTokenAuthentication = BearerTokenAuthentication(
            customOAuth2User,
            accessToken,
            customOAuth2User.authorities
        )

        // return authentication object
        return Mono.just(bearerTokenAuthentication)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/