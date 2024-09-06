package com.frontiers.bff.auth.configurations

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec

/**********************************************************************************************************************/
/*************************************************** CONFIGURATION ****************************************************/
/**********************************************************************************************************************/

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/client/ClientReactiveHttpSecurityPostProcessor.java
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/client/ClientAuthorizeExchangeSpecPostProcessor.java
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/ReactiveHttpSecurityPostProcessor.java
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/reactive/AuthorizeExchangeSpecPostProcessor.java

@Configuration
internal class PostProcessorsConfig {

    /**
     * Hook to override security rules for all paths that are not listed in
     * "permit-all". Default is isAuthenticated().
     *
     * @return a hook to override security rules for all paths that are not listed in
     * "permit-all". Default is isAuthenticated().
     *
     * Fine tunes access control from java configuration. It applies to all routes not listed in "permit-all"
     * property configuration. Default requires users to be authenticated.
     */
    @ConditionalOnMissingBean
    @Bean
    internal fun clientAuthorizePostProcessor(): ClientAuthorizeExchangeSpecPostProcessor {
        return ClientAuthorizeExchangeSpecPostProcessor {
            spec: ServerHttpSecurity.AuthorizeExchangeSpec ->
            spec.anyExchange().authenticated()
        }
    }

    /**
     * Hook to override all or part of HttpSecurity auto-configuration.
     *
     * @return a hook to override all or part of HttpSecurity auto-configuration.
     * Overrides anything from above auto-configuration.
     * It is called just before the security filter-chain is returned. Default is a no-op.
     */
    @ConditionalOnMissingBean
    @Bean
    internal fun clientHttpPostProcessor(): ClientReactiveHttpSecurityPostProcessor {
        return ClientReactiveHttpSecurityPostProcessor {
            serverHttpSecurity: ServerHttpSecurity ->
            serverHttpSecurity
        }
    }
}

/**********************************************************************************************************************/
/***************************************************** INTERFACES *****************************************************/
/**********************************************************************************************************************/

internal fun interface ClientAuthorizeExchangeSpecPostProcessor : AuthorizeExchangeSpecPostProcessor

internal fun interface ClientReactiveHttpSecurityPostProcessor : ReactiveHttpSecurityPostProcessor

interface AuthorizeExchangeSpecPostProcessor {
    fun authorizeHttpRequests(spec: AuthorizeExchangeSpec): AuthorizeExchangeSpec?
}

interface ReactiveHttpSecurityPostProcessor {
    fun process(serverHttpSecurity: ServerHttpSecurity): ServerHttpSecurity
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/