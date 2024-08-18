package com.example.bff.routing

import org.springframework.beans.factory.ObjectProvider
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.web.reactive.function.OAuth2BodyExtractors.oauth2AccessTokenResponse
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.Principal
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.*


@Component
internal class CustomTokenRelayGatewayFilterFactory(
    clientManagerProvider: ObjectProvider<ReactiveOAuth2AuthorizedClientManager>,
    private val authorizedClientRepository: ServerOAuth2AuthorizedClientRepository,
    private val webClientBuilder: WebClient.Builder
) : TokenRelayGatewayFilterFactory(clientManagerProvider) {
    private val accessTokenExpiresSkew: Duration = Duration.ofMinutes(1)

    /**
     * Adds access token to header
     * Also checks if it has expired. If it has, creates a new one (using the refresh token)
     */
    override fun apply(config: NameConfig): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            exchange.getPrincipal<Principal>()
                .filter { principal: Principal? ->
                    if (principal != null) {
                        println("Principal received: $principal")
                    } else {
                        println("No Principal found")
                    }
                    principal is OAuth2AuthenticationToken
                }
                .cast(OAuth2AuthenticationToken::class.java)
                .flatMap { authentication: OAuth2AuthenticationToken ->
                    getAuthorizedClient(
                        exchange,
                        authentication
                    )
                }
                .flatMap { authenticationPair ->
                    if (shouldRefresh(authenticationPair.oAuth2AuthorizedClient)) {
                        refreshAuthorizedClient(
                            exchange,
                            authenticationPair.oAuth2AuthorizedClient,
                            authenticationPair.oAuth2AuthenticationToken
                        )
                    } else {
                        Mono.just(authenticationPair.oAuth2AuthorizedClient)
                    }
                }
                .map { obj: OAuth2AuthorizedClient -> obj.accessToken }
                .map { token: OAuth2AccessToken ->
                    withBearerAuth(
                        exchange,
                        token
                    )
                }
                .defaultIfEmpty(exchange)
                .flatMap { modifiedExchange ->
                    chain.filter(modifiedExchange)
                }
        }
    }

    /**
     * Asynchronously loads the OAuth2AuthorizedClient (which holds the tokens and client registration details)
     * associated with the current user (represented by the OAuth2AuthenticationToken). It then packages this client
     * together with the authentication token into an AuthenticationPair.
     */
    private fun getAuthorizedClient(
        exchange: ServerWebExchange,
        oauth2Authentication: OAuth2AuthenticationToken
    ): Mono<AuthenticationPair> {
        return authorizedClientRepository.loadAuthorizedClient<OAuth2AuthorizedClient>(
            oauth2Authentication.authorizedClientRegistrationId,
            oauth2Authentication,
            exchange
        )
            .map { oAuth2AuthorizedClient: OAuth2AuthorizedClient ->
                AuthenticationPair(
                    oAuth2AuthorizedClient,
                    oauth2Authentication
                )
            }
    }

    /**
     * Checks whether the access token in the OAuth2AuthorizedClient should be refreshed based on its
     * expiration time (and skew window), and the presence of a refresh token.
     */
    private fun shouldRefresh(authorizedClient: OAuth2AuthorizedClient?): Boolean {
        val refreshToken = authorizedClient?.refreshToken
        if (refreshToken == null) {
            System.err.println("No refresh token available")
            return false
        }
        val now: Instant = CLOCK.instant()
        val expiresAt = authorizedClient.accessToken.expiresAt
        if (now.isAfter(expiresAt!!.minus(this.accessTokenExpiresSkew))) {
            System.err.println("Access token expired and should be refreshed")
            return true
        }
        return false
    }

    /**
     * Create, with the current refresh token, a new OAuth2AuthorizedClient (with a new access token)
     */
    private fun refreshAuthorizedClient(
        exchange: ServerWebExchange,
        authorizedClient: OAuth2AuthorizedClient,
        oauth2Authentication: OAuth2AuthenticationToken
    ): Mono<OAuth2AuthorizedClient> {
        val headers = HttpHeaders()
        val clientRegistration = authorizedClient.clientRegistration
        clientRegistration?.clientId?.let { headers.setBasicAuth(it, clientRegistration.clientSecret) }
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        return webClientBuilder.build()
            .method(HttpMethod.POST)
            .uri(clientRegistration.providerDetails.tokenUri)
            .headers { header: HttpHeaders ->
                header.addAll(
                    headers
                )
            }
            .bodyValue(refreshTokenBody(authorizedClient.refreshToken!!.tokenValue))
            .exchangeToMono { refreshResponse: ClientResponse ->
                if (refreshResponse.statusCode() == HttpStatus.BAD_REQUEST) {
                    System.err.println("The refresh token or sessions expired.")
                    throw ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        TOKEN_REFRESHMENT_ERROR_MESSAGE
                    )
                } else {
                    return@exchangeToMono refreshResponse.body<Mono<OAuth2AccessTokenResponse>>(
                        oauth2AccessTokenResponse()
                    )
                }
            }
            .map { accessTokenResponse: OAuth2AccessTokenResponse ->
                val refreshToken: OAuth2RefreshToken = Optional.ofNullable(accessTokenResponse.refreshToken)
                    .orElse(authorizedClient.refreshToken)
                OAuth2AuthorizedClient(
                    authorizedClient.clientRegistration,
                    authorizedClient.principalName,
                    accessTokenResponse.accessToken,
                    authorizedClient.refreshToken // (old refresh token)
                //  refreshToken, // (new refresh token)
                )
            }
            .flatMap { result: OAuth2AuthorizedClient ->
                authorizedClientRepository.saveAuthorizedClient(
                    result,
                    oauth2Authentication,
                    exchange
                ).thenReturn<OAuth2AuthorizedClient>(result)
            }
    }

    /**
     * Authentication Pair object
     */
    private class AuthenticationPair(
        val oAuth2AuthorizedClient: OAuth2AuthorizedClient,
        val oAuth2AuthenticationToken: OAuth2AuthenticationToken
    )

    /**
     * Static variables and functions
     */
    companion object {
        private const val TOKEN_REFRESHMENT_ERROR_MESSAGE = "Stale session or token"
        private const val GRANT_TYPE_KEY = "grant_type"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private val CLOCK: Clock = Clock.systemUTC()

        /**
         * Add Access Token to Header
         */
        private fun withBearerAuth(exchange: ServerWebExchange, accessToken: OAuth2AccessToken): ServerWebExchange {
            return exchange.mutate()
                .request { r: ServerHttpRequest.Builder ->
                    r.headers { headers: HttpHeaders ->
                        headers.setBearerAuth(
                            accessToken.tokenValue
                        )
                    }
                }
                .build()
        }

        /**
         * Create body for Refresh Token request
         */
        private fun refreshTokenBody(refreshToken: String): MultiValueMap<String, String> {
            val body: MultiValueMap<String, String> = LinkedMultiValueMap()
            body.add(GRANT_TYPE_KEY, AuthorizationGrantType.REFRESH_TOKEN.value)
            body.add(REFRESH_TOKEN_KEY, refreshToken)
            return body
        }
    }
}