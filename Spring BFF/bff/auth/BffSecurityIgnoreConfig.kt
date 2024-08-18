package com.example.bff.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/*********************************************** DEFAULT SECURITY CONFIGURATION ***************************************/
/**********************************************************************************************************************/

@Configuration
internal class BffSecurityIgnoreConfig {

    // Define base paths and file extensions for static resources
    private val staticResourceMap = mapOf(
        "/api/resource/" to listOf("jpg", "png", "gif", "css", "js", "map"),
    )

    // Define base paths and file extensions for security context loading
    private val skipSecurityContextLoading = mapOf(
        "/api/resource/" to listOf("jpg", "png", "gif", "css", "js", "map"),
        "/login-options" to listOf()
    )

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {

        // Generate patterns for static resources
        val staticPatterns = staticResourceMap.flatMap { (basePath, extensions) ->
            if (extensions.isEmpty()) {
                listOf(basePath)
            } else {
                getStaticResourcePatterns(basePath, extensions)
            }
        }.toTypedArray()

        // Print the ignored paths
        println("Ignoring the following static resource paths:")
        staticPatterns.forEach { pattern ->
            println(pattern)
        }

        // Does not (for some reason!) prevent security context from still being loaded!
        return WebSecurityCustomizer { web: WebSecurity ->
            web.debug(false)
                .ignoring()
                .requestMatchers(
                    "/favicon.ico",
                    *staticPatterns
                )
        }
    }

    private fun getStaticResourcePatterns(basePath: String, extensions: List<String>): List<String> {
        return extensions.map { "$basePath**/*.$it" }
    }

    fun shouldSkipStaticResources(requestPath: String): Boolean {
        return staticResourceMap.any { (basePath, extensions) ->
            requestPath.startsWith(basePath)
                    && (extensions.isEmpty() || extensions.any { requestPath.endsWith(it, ignoreCase = true) })
        }
    }

    fun shouldSkipSecurityContextLoading(requestPath: String): Boolean {
        return skipSecurityContextLoading.any { (basePath, extensions) ->
            requestPath.startsWith(basePath)
                    && (extensions.isEmpty() || extensions.any { requestPath.endsWith(it, ignoreCase = true) })
        }
    }


}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/