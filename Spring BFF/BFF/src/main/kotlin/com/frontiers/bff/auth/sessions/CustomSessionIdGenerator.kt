package com.frontiers.bff.auth.sessions

import org.springframework.session.SessionIdGenerator
import org.springframework.stereotype.Component
import java.util.*

/**********************************************************************************************************************/
/**************************************************** SESSION ID GENERATOR ********************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/common.html#changing-how-session-ids-are-generated

/**
 * A Custom Session-ID Generator
 */
@Component
internal class CustomSessionIdGenerator : SessionIdGenerator {

    val prefix = "BFF"

    override fun generate(): String {
        // adds custom prefix, timestamp, and UUID to create a session identifier
        return generateEnhancedIdentifier(prefix)
    }

    private fun generateEnhancedIdentifier(prefix: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID()
        return "$prefix-$timestamp-$uuid"
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/