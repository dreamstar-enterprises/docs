package com.example.authorizationserver.auth.sessions

import org.springframework.session.SessionIdGenerator
import org.springframework.stereotype.Component

/**********************************************************************************************************************/
/************************************************ SESSION ID GENERATOR ************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#changing-how-session-ids-are-generated

@Component
internal class CustomSessionIdGenerator : SessionIdGenerator {

    override fun generate(): String {
        // use a UUID with a custom prefix or other unique generation logic
        return "IN-HOUSE-AUTH-SERVER-${java.util.UUID.randomUUID()}"
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/