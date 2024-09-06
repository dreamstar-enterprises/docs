package com.frontiers.bff.auth.resolvers.customizers

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import java.util.function.Consumer

/**********************************************************************************************************************/
/**************************************************** CUSTOMIZER ******************************************************/
/**********************************************************************************************************************/

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/CompositeOAuth2AuthorizationRequestCustomizer.java


/**
 * Helps organize and apply multiple customization strategies to the OAuth2 authorization request
 * in a modular and flexible way.
 */
internal class CompositeOAuth2AuthorizationRequestCustomizer(
    vararg customizers: Consumer<OAuth2AuthorizationRequest.Builder>
) : Consumer<OAuth2AuthorizationRequest.Builder> {

    private val delegates: MutableList<Consumer<OAuth2AuthorizationRequest.Builder>> =
        customizers.toMutableList()

    // secondary constructor to allow extending an existing instance
    constructor(
        other: CompositeOAuth2AuthorizationRequestCustomizer,
        vararg customizers: Consumer<OAuth2AuthorizationRequest.Builder>
    ) : this(*(other.delegates.toTypedArray() + customizers))

    // applies all customizers to the given OAuth2AuthorizationRequest.Builder
    override fun accept(builder: OAuth2AuthorizationRequest.Builder) {
        for (consumer in delegates) {
            consumer.accept(builder)
        }
    }

    // adds an additional customizer
    fun addCustomizer(customizer: Consumer<OAuth2AuthorizationRequest.Builder>):
            CompositeOAuth2AuthorizationRequestCustomizer {
        delegates.add(customizer)
        return this
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/