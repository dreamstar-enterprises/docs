package com.example.bff.props

import com.c4_soft.springaddons.security.oidc.starter.properties.CorsProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**********************************************************************************************************************/
/**************************************************** PROPERTIES ******************************************************/
/**********************************************************************************************************************/

/**********************************************************************************************************************/
/* SERVER PROPERTIES                                                                                                  */
/**********************************************************************************************************************/
@ConfigurationProperties(prefix = "dse-servers")
@Configuration
internal class ServerProperties {

    /*************************/
    /* SERVERS               */
    /*************************/
    // server settings
    var scheme: String? = null
    var hostname: String? = null

    // reverse proxy settings
    var reverseProxyPort: Int? = null
    val reverseProxyUri: String
        get() = "$scheme://$hostname:$reverseProxyPort"

    // bff server settings
    var bffPort: Int? = null
    var bffPrefix: String? = null
    val bffUri: String
        get() = "$scheme://$hostname:$bffPort"
    val clientUri: String
        get() = "$reverseProxyUri$bffPrefix"

    // resource server settings
    var resourceServerPort: Int? = null
    var resourceServerPrefix: String? = null
    val resourceServerUri: String
        get() = "$scheme://$hostname:$resourceServerPort"

    /*************************/
    /* ISSUERS               */
    /*************************/
    // in-house authorization server
    var authorizationServerPrefix: String? = null
    var inHouseAuthRegistrationId: String? = null
    val inHouseIssuerUri: String
        get() = "$reverseProxyUri$authorizationServerPrefix"

}

/**********************************************************************************************************************/
/* CLIENT PROPERTIES                                                                                                  */
/**********************************************************************************************************************/
@ConfigurationProperties(prefix = "security")
@Configuration
internal class ClientSecurityProperties {

    // nested class for OAuth2 client registrations
    var oauth2: OAuth2Properties = OAuth2Properties()

    // inner classes for structured properties
    internal class OAuth2Properties {
        var client: ClientProperties = ClientProperties()

        internal class ClientProperties {
            var registration: RegistrationProperties = RegistrationProperties()

            // client registrations
            internal class RegistrationProperties {
                var bff: BffProperties = BffProperties()
                var firebase: FirebaseProperties = FirebaseProperties()

                internal class BffProperties {
                    var clientId: String? = null
                    var clientSecret: String? = null
                }

                internal class FirebaseProperties {
                    var clientId: String? = null
                    var clientSecret: String? = null
                }

            }
        }
    }

    // custom getter methods to provide variable names as needed
    val bffClientId: String?
        get() = oauth2.client.registration.bff.clientId

    val bffClientSecret: String?
        get() = oauth2.client.registration.bff.clientSecret

    val firebaseClientId: String?
        get() = oauth2.client.registration.firebase.clientId

    val firebaseClientSecret: String?
        get() = oauth2.client.registration.firebase.clientSecret

}

/**********************************************************************************************************************/
/* OIDC PROPERTIES                                                                                                  */
/**********************************************************************************************************************/
@Component
internal class OidcProviderProperties(
    private val serverProperties: ServerProperties,
) {

    /**
     * OpenID Providers configuration: JWK set URI, issuer URI, audience, and authorities mapping configuration
     * for each issuer. A minimum of one issuer is required. Properties defined here are a replacement for
     * spring.security.oauth2.resourceserver.jwt.* (which will be ignored). The reason for that is it is applicable
     * only to single tenant scenarios. Use properties.
     * Authorities mapping defined there is used by both client and resource server filter-chains.
     */
    internal data class OpenidProviderProperties(

        /**
         * Must be exactly the same as in access tokens (even trailing slash, if any, is important).
         * In case of doubt, open one of your access tokens with a tool like https://jwt.io.
         */
        val iss: URI? = null,

        /**
         * Can be omitted if OpenID configuration can be retrieved from ${iss}/.well-known/ openid-configuration
         */
        val jwkSetUri: URI? = null,

        /**
         * Can be omitted. Will insert an audience validator if not null or empty
         */
        val aud: String? = null,

        /**
         * Authorities mapping configuration, per claim
         */
        val authorities: List<SimpleAuthoritiesMappingProperties> = listOf(),

        /**
         * Authorities mapping configuration, per claim
         */
        val usernameClaim: String = StandardClaimNames.SUB
    )

    internal data class SimpleAuthoritiesMappingProperties(
        /**
         * JSON path of the claim(s) to map with this properties
         */
        val path: String = "$.realm_access.roles",

        /**
         * What to prefix authorities with (for instance "ROLE_" or "SCOPE_")
         */
        val prefix: String = "",

        /**
         * Whether to transform authorities to uppercase, lowercase, or to leave it unchanged
         */
        val case: Case = Case.UNCHANGED
    ) {
        enum class Case {
            UNCHANGED,
            UPPER,
            LOWER
        }
    }

    // id-provider 1
    final val InHouseAuthProvider = OpenidProviderProperties(
        iss = URI.create(serverProperties.inHouseIssuerUri),
        jwkSetUri = URI.create("${serverProperties.inHouseIssuerUri}/oauth2/jwks"),
        aud = "BFF-Server",
        authorities = listOf(
            SimpleAuthoritiesMappingProperties(
                path = "$.authorities",
                prefix = "",
                case = SimpleAuthoritiesMappingProperties.Case.UPPER
            )),
        usernameClaim = "sub"
    )

    // final list of OPENID Provider (Issuer) Properties
    final val openidProviderPropertiesList: List<OpenidProviderProperties> = listOf(
        InHouseAuthProvider
    )
}

/**********************************************************************************************************************/
/* SPRING DATA PROPERTIES                                                                                             */
/**********************************************************************************************************************/
@ConfigurationProperties(prefix = "spring.data")
@Configuration
internal class SpringDataProperties {

    // Redis properties
    var redis: RedisProperties = RedisProperties()

    class RedisProperties {
        var host: String = ""
        var password: String = ""
        var port: Int = 6800
    }
}

/**********************************************************************************************************************/
/* SPRING SESSION PROPERTIES                                                                                          */
/**********************************************************************************************************************/
@ConfigurationProperties(prefix = "spring.session")
@Configuration
internal class SpringSessionProperties {

    var redis: RedisProperties? = null
    var timeout: Int = 21600 // in seconds (6 hours)

    class RedisProperties {
        var namespace: String? = null
        var repositoryType: String? = null
        var flushMode: String? = null
        var sessionCleanUpFrequency: Long = 30 // in seconds
        val sessionNameSpace: String
            get() = namespace.toString()
        var oauth2RequestNameSpace: String = "spring:session:bff:oauth2:authorization-request"
        var securityContextNameSpace: String = "spring:session:bff:oauth2:security-context"
        var authorizedClientNameSpace: String = "spring:session:bff:oauth2:authorized-client"
        var expiredSessionsNameSpace: String = "spring:session:bff:oauth2:sessions:expirations"
    }
}

/**********************************************************************************************************************/
/* CORS PROPERTIES                                                                                                    */
/**********************************************************************************************************************/
@Component
internal class CorsProperties(
    serverProperties: ServerProperties,
    csrfProperties: CsrfProperties
) {
    /**
     * Path matcher to which this configuration entry applies
     */
    final val path: String = "/**"

    final val allowedOriginPatterns: List<String> = listOf(
        serverProperties.reverseProxyUri
    )

    final val allowedMethods: List<String> = listOf(
        "GET",
        "POST",
        "PUT",
        "PATCH",
        "DELETE",
        "OPTIONS"
    )

    final val allowedHeaders: List<String> = listOf(
        "Content-Type",
        "Authorization",
        csrfProperties.CSRF_HEADER_NAME
    )

    final val exposedHeaders: List<String> = listOf(
        "Content-Type",
        "Authorization",
        csrfProperties.CSRF_HEADER_NAME
    )

    final val maxAge: Long = 21600L // in seconds (6 hours)

    /**
     * Required if credentials (cookies, authorization headers) are involved
     */
    final val allowCredentials: Boolean = true

    /**
     * If left to false, OPTIONS requests are added to permit-all for the [path] matchers of this [CorsProperties]
     */
    final val disableAnonymousOptions: Boolean = true
}

/**********************************************************************************************************************/
/* CSRF PROPERTIES                                                                                                    */
/**********************************************************************************************************************/
@Component
internal class CsrfProperties {

    final val CSRF_COOKIE_NAME: String  = "XSRF-CLIENT-TOKEN"
    final val CSRF_HEADER_NAME: String = "X-XSRF-CLIENT-TOKEN"

    final val CSRF_COOKIE_HTTP_ONLY: Boolean = false // so SPA (e.g. Angular) can read it
    final val CSRF_COOKIE_SECURE: Boolean = false // scope is not just on secure connections
    final val CSRF_COOKIE_SAME_SITE: String = "Strict"
    final val CSRF_COOKIE_MAX_AGE: Long = -1 // session cookie, expires when browser closes
    final val CSRF_COOKIE_PATH: String = "/"

}

/**********************************************************************************************************************/
/* SESSION PROPERTIES                                                                                             */
/**********************************************************************************************************************/
@Component
internal class SessionProperties {

    // needs to be called JSESSIONID
    // https://docs.spring.io/spring-security/reference/reactive/oauth2/login/logout.html#oauth2login-advanced-oidc-logout
    final val SESSION_COOKIE_NAME: String = "CLIENT-SESSIONID"

    final val SESSION_COOKIE_HTTP_ONLY: Boolean = true
    final val SESSION_COOKIE_SECURE: Boolean = false // scope is not just on secure connections
    final val SESSION_COOKIE_SAME_SITE: String = "Strict"
    final val SESSION_COOKIE_MAX_AGE: Long = 21600 // in seconds
    final val SESSION_COOKIE_PATH: String =  "/"
}

/**********************************************************************************************************************/
/* AUTHENTICATION PROPERTIES                                                                                           */
/**********************************************************************************************************************/
@Component
internal class AuthenticationProperties() {

    // securityMatchers for Client security filter chain
    final val securityMatchers: List<String> = listOf(
        "/api/**",
        "/login/**",
        "/oauth2/**",
        "/logout/**",
        "/login-options",
    )
}


/**********************************************************************************************************************/
/* AUTHORIZATION PROPERTIES                                                                                           */
/**********************************************************************************************************************/
@Component
internal class AuthorizationProperties(
    serverProperties: ServerProperties
) {

    // allowed permitAlls
    final val permitAll: List<String> = listOf(
        "/api/**",
        "/login/**",
        "/oauth2/**",
        "/logout/connect/back-channel/${serverProperties.inHouseAuthRegistrationId}",
        "/login-options",
    )
}

/**********************************************************************************************************************/
/* REQUEST PARAMETER PROPERTIES  (AUTHORIZATION & TOKEN ENDPOINTS - NOT LOGOUT)                                       */
/**********************************************************************************************************************/
@Component
internal class RequestParameterProperties{

    /**
     * Additional parameters to send with authorization request, mapped by client registration IDs
     */
    private val authorizationParams: MutableMap<String, Map<String, List<String>>> = mutableMapOf()

    // get authorizationParameters
    final fun getExtraAuthorizationParameters(registrationId: String): MultiValueMap<String, String> {
        return getExtraParameters(registrationId, authorizationParams)
    }

    /**
     * Additional parameters to send with token request, mapped by client registration IDs
     */
    private var tokenParams: MutableMap<String, Map<String, List<String>>> = mutableMapOf()

    // get tokenParameters
    final fun getExtraTokenParameters(registrationId: String): MultiValueMap<String, String> {
        return getExtraParameters(registrationId, tokenParams)
    }

    /**
     * Converts to LinkedMultiValueMap (can store multiple values against each key)
     */
    private fun getExtraParameters(
        registrationId: String,
        requestParamsMap: Map<String, Map<String, List<String>>>
    ): MultiValueMap<String, String> {
        val extraParameters = requestParamsMap[registrationId]?.let { otherMap ->
            LinkedMultiValueMap(otherMap)
        } ?: LinkedMultiValueMap()

        return extraParameters
    }

}

/**********************************************************************************************************************/
/* RE-DIRECTION PROPERTIES                                                                                           */
/**********************************************************************************************************************/

/**
 * HTTP status for redirections in OAuth2 login and logout. You might set this to something in 2xx range
 * (like OK, ACCEPTED, NO_CONTENT, ...) for single page and mobile applications to handle this redirection as it wishes
 * (change the user-agent, clear some headers, ...). 2xx WILL NOT automatically re-direct.
 */

@Component
internal class OAuth2RedirectionProperties() {

    // create instance of server, security properties class
    private val  serverProperties =  ServerProperties()

    /*************************/
    /* LOGIN REDIRECT HOST   */
    /*************************/
    /**
     * URI containing scheme, host, and port used for redirection
     * (defaults to the client URI).
     */
    final val postLoginRedirectHostValue: URI? = null

    fun getPostLoginRedirectHost(
    ): URI {
        return postLoginRedirectHostValue ?: URI.create(serverProperties.clientUri)
    }

    /*************************/
    /* SUCCESSFUL LOGIN      */
    /*************************/
    /**
     * Path used to redirect the user after successful login.
     */
    final val postLoginRedirectPath: String? = null

    /**
     * Get final constructed redirect URI.
     */
    final fun getPostLoginRedirectUri(): URI? {
        if (postLoginRedirectHostValue == null && postLoginRedirectPath == null) {
            return null
        }

        val uriBuilder = UriComponentsBuilder.fromUri(getPostLoginRedirectHost())
        postLoginRedirectPath?.let { uriBuilder.path(it) }

        return uriBuilder.build().toUri()
    }

    /*************************/
    /* LOGIN ERROR           */
    /*************************/
    /**
     * Path used to redirect the user if unsuccessful login.
     */
    final val loginErrorRedirectPath:  String? = null

    /**
     * Get final constructed redirect URI.
     */
    final fun getLoginErrorRedirectUri(): URI? {
        if (postLoginRedirectHostValue == null && loginErrorRedirectPath == null) {
            return null
        }

        val uriBuilder = UriComponentsBuilder.fromUri(getPostLoginRedirectHost())
        loginErrorRedirectPath?.let { uriBuilder.path(it) }

        return uriBuilder.build().toUri()
    }

    /*************************/
    /* RESPONSE HEADER       */
    /*************************/
    /**
     * Header used by OAuth2ServerRedirectStrategy to carry the various response codes
     */
    final val RESPONSE_STATUS_HEADER: String = "X-RESPONSE-STATUS"

    /*************************/
    /* STATUS CODES          */
    /*************************/
    /**
     * Status for the 1st response in authorization code flow, with location to get authorization code from authorization server
     */
    final val preAuthorizationCode: HttpStatus = HttpStatus.FOUND

    /**
     * Status for the response after authorization code, with location to the UI
     */
    final val postAuthorizationCode: HttpStatus = HttpStatus.FOUND

    /**
     * Status for the response after BFF logout, with location to authorization server logout endpoint
     * ACCEPTED IS code 202 - so does not automatically re-direct
     */
    final val rpInitiatedLogout: HttpStatus = HttpStatus.ACCEPTED


    /**
     * Map of logout properties indexed by client registration ID
     * (must match a registration in Spring Boot OAuth2 client configuration).
     * LogoutProperties are configuration for authorization server not strictly following the RP-Initiated Logout
     * standard, but exposing a logout end-point expecting an authorized GET request with following request params:
     * "client-id" (required)
     *  post-logout redirect URI (optional)
     */
    private val oauth2Logout: Map<String?, OAuth2LogoutProperties> = mapOf()

    internal fun getLogoutProperties(clientRegistrationId: String?): OAuth2LogoutProperties? {
        return oauth2Logout.get(clientRegistrationId)
    }

}


/**********************************************************************************************************************/
/* LOGIN PROPERTIES                                                                                                   */
/**********************************************************************************************************************/
@Component
internal class LoginProperties {

    final val LOGIN_URL: String = "/"

    final val POST_AUTHENTICATION_SUCCESS_URI_HEADER: String = "X-POST-LOGIN-SUCCESS-URI"
    final val POST_AUTHENTICATION_SUCCESS_URI_PARAM: String = "post_login_success_uri"
    final val POST_AUTHENTICATION_SUCCESS_URI_SESSION_ATTRIBUTE = POST_AUTHENTICATION_SUCCESS_URI_PARAM

    final val POST_AUTHENTICATION_FAILURE_URI_HEADER: String = "X-POST-LOGIN-FAILURE-URI"
    final val POST_AUTHENTICATION_FAILURE_URI_PARAM: String = "post_login_failure_uri"
    final val POST_AUTHENTICATION_FAILURE_URI_SESSION_ATTRIBUTE: String = POST_AUTHENTICATION_FAILURE_URI_PARAM
    final val POST_AUTHENTICATION_FAILURE_CAUSE_ATTRIBUTE: String = "error"

}

/**********************************************************************************************************************/
/* LOGOUT PROPERTIES                                                                                                  */
/**********************************************************************************************************************/
@Component
internal class LogoutProperties {

    // create instance of server properties class
    private val  serverProperties =  ServerProperties()

    final val LOGOUT_URL: String = "/logout"

    final val POST_LOGOUT_SUCCESS_URI_HEADER: String = "X-POST-LOGOUT-SUCCESS-URI"
    final val POST_LOGOUT_SUCCESS_URI_PARAM: String = "post_logout_success_uri"

    /*************************/
    /* LOGOUT REDIRECT HOST  */
    /*************************/
    /**
     * URI containing scheme, host, and port used for redirection
     * (defaults to the client URI).
     */
    final val postLogoutRedirectHostValue: URI? = null

    fun getPostLogoutRedirectHost(
    ): URI {
        return postLogoutRedirectHostValue ?: URI.create(serverProperties.clientUri)
    }

    /*************************/
    /* LOGOUT                */
    /*************************/
    /**
     * Path used to redirect the user on logout
     */
    final val postLogoutRedirectPath:  String? = null

    /**
     * Get final constructed redirect URI.
     */
    final fun getPostLogoutRedirectUri(): URI? {
        if (postLogoutRedirectHostValue == null && postLogoutRedirectPath == null) {
            return null
        }

        val uriBuilder = UriComponentsBuilder.fromUri(getPostLogoutRedirectHost())
        postLogoutRedirectPath?.let { uriBuilder.path(it) }

        return uriBuilder.build().toUri()
    }

}

/**********************************************************************************************************************/
/* OAUTH2 LOGOUT PROPERTIES                                                                                                  */
/**********************************************************************************************************************/
internal data class OAuth2LogoutProperties(
    /**
     * URI on the authorization server where to redirect the user for logout
     */
    val uri: URI? = null,

    /**
     * Request param name for client-id
     */
    val clientIdRequestParam: String? = null,

    /**
     * Request param name for post-logout redirect URI
     * (where the user should be redirected after their session is closed on the authorization server)
     */
    val postLogoutUriRequestParam: String? = null,

    /**
     * Request param name for setting an ID-Token hint
     */
    val idTokenHintRequestParam: String? = null,

    /**
     * RP-Initiated Logout is enabled by default. Setting this to false disables it.
     */
    val rpInitiatedLogoutEnabled: Boolean = true
)

/**********************************************************************************************************************/
/* BACK CHANNEL LOGOUT PROPERTIES                                                                                                   */
/**********************************************************************************************************************/
@Component
internal class BackChannelLogoutProperties(
    serverProperties: ServerProperties,
    logoutProperties: LogoutProperties
) {

    /**
     * Enabled by default.
     */
    final val enabled = true

    /**
     * The URI for a loop of the Spring client to itself in which it actually ends the user session.
     * Overriding this can be useful to force the scheme and port in the case where the client is behind a revers proxy
     * with different scheme and port (default URI uses the original Back-Channel Logout request scheme and ports).
     */
    // internal-logout-uri: ${reverse-proxy-uri}${bff-prefix}/logout should work too,
    // but there is no reason to go through the reverse proxy for this internal call
    final val internalLogoutUri: String
        = "${serverProperties.bffUri}${logoutProperties.LOGOUT_URL}"

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/