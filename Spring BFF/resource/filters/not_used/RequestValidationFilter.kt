package com.example.timesheetapi.auth.security.filters.not_used

/**********************************************************************************************************************/
/************************************************* VALIDATION FILTER **************************************************/
/**********************************************************************************************************************/

//@Component
//internal class RequestValidationFilter() : WebFilter {
//
//    private val objectMapper: ObjectMapper = JacksonConfiguration.objectMapper
//
//    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
//        val request = exchange.request
//        val response = exchange.response
//        val requestId = request.headers.getFirst("Request-Id")
//
//        // if not valid, exit filter chain, and return response
//        if (requestId == null || requestId.isBlank()) {
//
//            response.statusCode = HttpStatus.BAD_REQUEST
//            response.headers.contentType = MediaType.APPLICATION_JSON
//
//            val errorMessage = "Request-Id header is missing or empty"
//            val errorResponse = objectMapper.writeValueAsString(mapOf("Validation Error" to errorMessage))
//
//            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorResponse.toByteArray())))
//        } else {
//
//            // if valid, proceed with the filter chain
//            return chain.filter(exchange)
//        }
//
//    }
//}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/