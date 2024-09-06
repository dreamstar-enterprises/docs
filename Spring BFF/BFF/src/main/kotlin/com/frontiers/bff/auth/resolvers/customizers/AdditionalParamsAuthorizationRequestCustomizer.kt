package com.frontiers.bff.auth.resolvers.customizers

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.util.MultiValueMap
import java.util.function.Consumer

/**********************************************************************************************************************/
/**************************************************** CUSTOMIZER ******************************************************/
/**********************************************************************************************************************/

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/AdditionalParamsAuthorizationRequestCustomizer.java

/**
 * adds extra parameters to the OAuth2 authorization request (if needed).
 */
internal class AdditionalParamsAuthorizationRequestCustomizer(
    private val additionalParams: MultiValueMap<String, String>
) : Consumer<OAuth2AuthorizationRequest.Builder> {

    override fun accept(builder: OAuth2AuthorizationRequest.Builder) {
        builder.additionalParameters { params ->
            additionalParams.forEach { (key, values) ->
                params[key] = values.joinToString(",")
            }
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/