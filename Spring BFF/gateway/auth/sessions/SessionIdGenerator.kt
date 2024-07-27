package com.example.gateway.auth.sessions

import org.springframework.session.SessionIdGenerator

/**********************************************************************************************************************/
/**************************************************** SESSION ID GENERATOR ********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#changing-how-session-ids-are-generated

internal class SessionIdGenerator : SessionIdGenerator {

    override fun generate(): String {
        // use a UUID with a custom prefix or other unique generation logic
        return "API-GATEWAY-${java.util.UUID.randomUUID()}"
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/