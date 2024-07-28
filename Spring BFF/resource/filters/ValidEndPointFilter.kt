package com.example.timesheetapi.auth.security.filters

import com.example.timesheetapi.api.utilities.json.JacksonConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPattern
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

@Component
internal class ValidEndPointFilter(
    private val endpointValidator: EndpointValidator
) : WebFilter {

    private val objectMapper: ObjectMapper = JacksonConfiguration.objectMapper

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        val requestPath = request.uri.path

        // if not valid
        if (!endpointValidator.isValidEndPoint(requestPath)) {

            response.statusCode = HttpStatus.NOT_FOUND
            response.headers.contentType = MediaType.APPLICATION_JSON

            val errorMessage = "Request Path is not valid"
            val errorResponse = objectMapper.writeValueAsString(mapOf("Validation Error" to errorMessage))

            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorResponse.toByteArray())))
        } else {

            // if valid, proceed with the filter chain
            return chain.filter(exchange)
        }

    }
}

@Component
internal class EndpointValidator(
    private val applicationContext: ApplicationContext
) {

    // static endpoints to be combined with dynamic ones
    private val staticEndpoints: Set<String> = setOf(
        "/logout",
        // add other statically defined endpoints here
    )

    internal fun isValidEndPoint(requestPath: String): Boolean {

        // check static endpoints first
        if (staticEndpoints.contains(requestPath)) {
            return true
        }

        // check dynamic endpoints
        val handlerMappings = applicationContext.getBeansOfType(RequestMappingHandlerMapping::class.java).values
        handlerMappings.forEach { handlerMapping ->
            val mappings = handlerMapping.handlerMethods.keys
            mappings.forEach { mapping ->
                if (mapping.patternsCondition.patterns.any {
                        pattern ->
                        pathMatches(pattern, requestPath)
                    } == true) {
                    return true
                }
            }
        }
        return false
    }

    private fun pathMatches(pattern: PathPattern, requestPath: String): Boolean {
        val pathContainer = PathContainer.parsePath(requestPath)
        return pattern.matches(pathContainer)
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/