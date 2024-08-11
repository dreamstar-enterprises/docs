package com.example.authorizationserver.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

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
        var bffPrefix: String? = null
        val bffUri: String
                get() = "$reverseProxyUri$bffPrefix"


        /*************************/
        /* ISSUERS               */
        /*************************/
        // authorization server
        var authorizationServerPort: Int? = null
        var authorizationServerPrefix: String? = null
        var inHouseAuthRegistrationId: String? = null
        val inHouseIssuerUri: String
                get() = "$reverseProxyUri$authorizationServerPrefix"
}

/**********************************************************************************************************************/
/* SECURITY PROPERTIES                                                                                                */
/**********************************************************************************************************************/
@ConfigurationProperties(prefix = "security")
@Configuration
internal class ClientSecurityProperties {

        // nested class for OAuth2 client registrations
        var oauth2: OAuth2Properties = OAuth2Properties()

        // nested class for RememberMe properties
        var rememberMe: RememberMeProperties = RememberMeProperties()

        // inner classes for structured properties
        internal class OAuth2Properties {
                var client: ClientProperties = ClientProperties()

                internal class ClientProperties {
                        var registration: RegistrationProperties = RegistrationProperties()
                        var registered: RegisteredProperties = RegisteredProperties()

                        // client registrations
                        internal class RegistrationProperties {
                                var google: GoogleProperties = GoogleProperties()

                                internal class GoogleProperties {
                                        var clientId: String? = null
                                        var clientSecret: String? = null
                                }

                        }

                        // registered clients
                        internal class RegisteredProperties {
                                var bff: BffProperties = BffProperties()
                                var resource: ResourceProperties = ResourceProperties()

                                internal class BffProperties {
                                        var clientId: String? = null
                                        var clientSecret: String? = null
                                }

                                internal class ResourceProperties {
                                        var clientId: String? = null
                                        var clientSecret: String? = null
                                }
                        }
                }
        }
        internal class RememberMeProperties {
                var key: String? = null
        }

        // custom getter methods to provide variable names as needed
        val bffClientId: String?
                get() = oauth2.client.registered.bff.clientId

        val bffClientSecret: String?
                get() = oauth2.client.registered.bff.clientSecret

        val resourceClientId: String?
                get() = oauth2.client.registered.resource.clientId

        val resourceClientSecret: String?
                get() = oauth2.client.registered.resource.clientSecret

        val googleClientId: String?
                get() = oauth2.client.registration.google.clientId

        val googleClientSecret: String?
                get() = oauth2.client.registration.google.clientSecret

        val rememberMeKey: String?
                get() = rememberMe.key

}

/**********************************************************************************************************************/
/* SPRING DATA PROPERTIES                                                                                             */
/**********************************************************************************************************************/
@ConfigurationProperties(prefix = "spring.data")
@Configuration
internal class SpringDataProperties {

        // MongoDB properties
        var mongodb: MongodbProperties = MongodbProperties()

        // Redis properties
        var redis: RedisProperties = RedisProperties()

        class MongodbProperties {
                var uri: String = ""
                var database: String = ""
        }

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
        var timeout: Int = 1800 // in seconds

        class RedisProperties {
                var namespace: String? = null
                var repositoryType: String? = null
                var flushMode: String? = null
        }
}

/**********************************************************************************************************************/
/* CSRF PROPERTIES                                                                                                    */
/**********************************************************************************************************************/
@Component
internal class CsrfProperties {

        final val CSRF_COOKIE_NAME: String = "XSRF-AUTH-TOKEN"
        final val CSRF_HEADER_NAME: String = "X-XSRF-AUTH-TOKEN"
        final val CSRF_PARAMETER_NAME: String = "_csrf"

        final val CSRF_COOKIE_HTTP_ONLY: Boolean = true
        final val CSRF_COOKIE_SECURE: Boolean = false // scope is not just on secure connections
        final val CSRF_COOKIE_SAME_SITE: String = "Strict"
        final val CSRF_COOKIE_MAX_AGE: Long = -1 // in seconds
        final val CSRF_COOKIE_PATH: String = "/"

}

/**********************************************************************************************************************/
/* SESSION PROPERTIES                                                                                                 */
/**********************************************************************************************************************/
@Component
internal class SessionProperties {

        final val SESSION_MAX_AGE: Int = 5 // in seconds

        final val SESSION_COOKIE_NAME: String = "AUTH-SESSIONID"

        final val SESSION_COOKIE_HTTP_ONLY: Boolean = true
        final val SESSION_COOKIE_SECURE: Boolean = false // scope is not just on secure connections
        final val SESSION_COOKIE_SAME_SITE: String = "Strict"
        final val SESSION_COOKIE_MAX_AGE: Int = 5 // in seconds
        final val SESSION_COOKIE_PATH: String =  "/"

        final val REDIRECT_URL: String = "/session-expired"
}

/**********************************************************************************************************************/
/* LOGIN PROPERTIES                                                                                                   */
/**********************************************************************************************************************/

@Component
internal class LoginProperties {

        final val LOGIN_FORM_URL: String = "/login"
}

/**********************************************************************************************************************/
/* LOGOUT PROPERTIES                                                                                                   */
/**********************************************************************************************************************/

@Component
internal class LogoutProperties {

        final val LOGOUT_URL: String = "/logout"
        final val INVALIDATE_HTTP_SESSION: Boolean = true
        final val CLEAR_AUTHENTICATION: Boolean = true
}

/**********************************************************************************************************************/
/* REMEMBER ME PROPERTIES                                                                                             */
/**********************************************************************************************************************/
@Component
internal class RememberMeProperties {

        final val TOKEN_VALID_DAYS: Int = 14 // in days
        final val USERNAME: String = "username"
        final val TOKEN: String = "token"
        final val LAST_USED_DATE: String = "last_used_date"
        final val NAME_SPACE: String = "spring:session:in-house-auth-server:rememberMe:token:"
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/