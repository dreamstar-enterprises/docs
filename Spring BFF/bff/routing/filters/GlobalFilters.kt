package com.example.bff.routing.filters

import com.example.bff.auth.repositories.authclients.RedisServerOAuth2AuthorizedClientRepository
import com.example.bff.routing.CustomTokenRelayGatewayFilterFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.factory.SaveSessionGatewayFilterFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/******************************************************* FILTER *******************************************************/
/**********************************************************************************************************************/

// The default implementation of ReactiveOAuth2AuthorizedClientService used by TokenRelayGatewayFilterFactory
// uses an in-memory data store. You will need to provide your own implementation of ReactiveOAuth2AuthorizedClientService
// if you need a more robust solution.

@Configuration
internal class TokenRelayFilterConfig {

    @Bean
    fun tokenRelayGatewayFilterFactory(
        authorizedClientManagerProvider: ObjectProvider<ReactiveOAuth2AuthorizedClientManager>,
        authorizedClientRepository: RedisServerOAuth2AuthorizedClientRepository,
        webClientBuilder: WebClient.Builder,
    ): CustomTokenRelayGatewayFilterFactory {
        return CustomTokenRelayGatewayFilterFactory(
            authorizedClientManagerProvider,
            authorizedClientRepository,
            webClientBuilder
        )
    }

    @Bean
    fun tokenRelayGlobalFilter(
        customTokenRelayGatewayFilterFactory: CustomTokenRelayGatewayFilterFactory
    ): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            customTokenRelayGatewayFilterFactory.apply{
                // optionally configure futher if needed
            }.filter(exchange, chain)
        }
    }
}

@Configuration
internal class SaveSessionFilterConfig(
    private val saveSessionGatewayFilterFactory: SaveSessionGatewayFilterFactory
) {

    @Bean
    fun saveSessionGlobalFilter(): GlobalFilter {
        return GlobalFilter { exchange, chain ->
            // Apply the SaveSession filter globally
            saveSessionGatewayFilterFactory.apply {
                // optionally configure futher if needed
            }.filter(exchange, chain)
        }
    }
}

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