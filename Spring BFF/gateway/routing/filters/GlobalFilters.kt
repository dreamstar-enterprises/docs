package com.example.gateway.routing.filters

import org.springframework.beans.factory.ObjectProvider
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

// The default implementation of ReactiveOAuth2AuthorizedClientService used by TokenRelayGatewayFilterFactory
// uses an in-memory data store. You will need to provide your own implementation of ReactiveOAuth2AuthorizedClientService
// if you need a more robust solution.

//@Configuration
//internal class TokenRelayFilterConfig {
//
//    @Bean
//    fun tokenRelayGatewayFilterFactory(
//        authorizedClientManagerProvider: ObjectProvider<ReactiveOAuth2AuthorizedClientManager>
//    ): TokenRelayGatewayFilterFactory {
//        return TokenRelayGatewayFilterFactory(authorizedClientManagerProvider)
//    }
//
//    @Bean
//    fun tokenRelayGlobalFilter(
//        tokenRelayGatewayFilterFactory: TokenRelayGatewayFilterFactory
//    ): GlobalFilter {
//        return GlobalFilter { exchange, chain ->
//            tokenRelayGatewayFilterFactory.apply {
//            // optionally configure TokenRelay if needed
//            }.filter(exchange, chain)
//        }
//    }
//}


@Configuration
internal class CustomHeaderFilterConfig {

    @Bean
    fun customHeaderFilter(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            chain.filter(exchange).then(
                Mono.fromRunnable {
                    exchange.response.headers.add(
                        "X-Powered-By",
                        "DreamStar Enterprises"
                    )
                }
            )
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/