package com.frontiers.bff.routing.savesession

import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.factory.SaveSessionGatewayFilterFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**********************************************************************************************************************/
/**************************************************** SAVE SESSION ****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class SaveSessionConfig(
    private val saveSessionGatewayFilterFactory: SaveSessionGatewayFilterFactory
) {

    @Bean
    fun saveSession(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            saveSessionGatewayFilterFactory.apply {

                // optionally configure futher if needed
            }.filter(exchange, chain)
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/
