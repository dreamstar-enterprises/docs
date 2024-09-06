package com.frontiers.bff.routing.filters

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

/**
 * Configures endpoints considered that should be skipped
 */
@Configuration
internal class IgnoreFilter {

    private val logger = LoggerFactory.getLogger(IgnoreFilter::class.java)

    // define base paths and file extensions for security context loading
    private val skipRequestPath: Map<String, List<String>> = mapOf(
        "/login-options" to listOf(),
    )

    // main function that checks if the request path should skip static resources
    fun shouldSkipRequestPath(requestPath: String): Boolean {
        val isSkipped = getRequestPathMatchers().any { it.matches(requestPath) }
        if (isSkipped) {
            logger.info("Request path '$requestPath' is skipped as a skippable path.")
        }
        return isSkipped
    }

    // function that generates matchers for request path
    private fun getRequestPathMatchers(): List<RequestPathMatcher> {
        return skipRequestPath.flatMap { (basePath, extensions) ->
            if (extensions.isEmpty()) {
                listOf(RequestPathMatcher(basePath))
            } else {
                extensions.map { extension -> RequestPathMatcher(basePath, extension) }
            }
        }
    }

    // data class to encapsulate the logic for matching request paths
    private data class RequestPathMatcher(val basePath: String, val extension: String? = null) {
        fun matches(requestPath: String): Boolean {
            return requestPath.startsWith(basePath) &&
                    (extension == null || requestPath.endsWith(".$extension", ignoreCase = true))
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/