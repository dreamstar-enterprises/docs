package com.example.authorizationserver.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**********************************************************************************************************************/
/**************************************************** PROPERTIES ******************************************************/
/**********************************************************************************************************************/

/**********************************************************************************************************************/
/* SERVER PROPERTIES                                                                                                  */
/**********************************************************************************************************************/
@ConfigurationProperties(prefix = "dse-servers")
@Configuration
internal class ServerProperties {

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
internal class SecurityProperties {

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
        var timeout: Int = 30

        class RedisProperties {
                var namespace: String? = null
                var repositoryType: String? = null
                var flushMode: String? = null
        }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/