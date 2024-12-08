package com.viviana.timesheets.auth.tokens.jwt

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator
import com.nimbusds.jose.jwk.source.JWKSetSource
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.viviana.timesheets.props.ServerProperties
import io.netty.channel.ChannelOption
import io.netty.resolver.AbstractAddressResolver
import io.netty.resolver.AddressResolverGroup
import io.netty.util.concurrent.EventExecutor
import io.netty.util.concurrent.Promise
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeUnit

/**********************************************************************************************************************/
/**************************************************** TOKEN CONFIGURATION *********************************************/
/**********************************************************************************************************************/

@Configuration
internal class JwtConfig(
    private val serverProperties: ServerProperties,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(JwtConfig::class.java)
    }

    init {
        // Set multiple IPv6 preferences
        System.setProperty("java.net.preferIPv6Addresses", "true")
        System.setProperty("java.net.preferIPv6Stack", "true")
        java.security.Security.setProperty("networkaddress.preferIPv6Addresses", "true")

        // Log the current settings
        logger.info("IPv6 Preferences:")
        logger.info("preferIPv6Addresses: ${System.getProperty("java.net.preferIPv6Addresses")}")
        logger.info("preferIPv6Stack: ${System.getProperty("java.net.preferIPv6Stack")}")
        logger.info("Security preferIPv6Addresses: ${java.security.Security.getProperty("networkaddress.preferIPv6Addresses")}")
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val jwkSetUri = URI(serverProperties.auth0JwKeySetUri)
        val host = jwkSetUri.host

        logger.info("Initializing JWT decoder for host: $host")

        // Create custom address resolver for IPv6
        val ipv6Resolver = object : AddressResolverGroup<InetSocketAddress>() {
            override fun newResolver(executor: EventExecutor): io.netty.resolver.AddressResolver<InetSocketAddress> {
                return object : AbstractAddressResolver<InetSocketAddress>(executor) {
                    override fun doResolve(address: InetSocketAddress?, promise: Promise<InetSocketAddress>?) {
                        try {
                            // Get the hostname either from the address or fall back to the original host
                            val hostname = address?.hostName ?: host

                            val port = if (address?.port != null) {
                                logger.info("Using provided port: ${address.port}")
                                address.port
                            } else {
                                logger.info("Using fallback port: 443")
                                443
                            }

                            logger.info("Attempting to resolve IPv6 address for: $hostname:$port")

                            val ipv6Address = InetAddress.getAllByName(hostname)
                                .find { it is Inet6Address }
                                ?: throw IllegalStateException("No IPv6 address available for $hostname")

                            val resolvedAddress = InetSocketAddress(ipv6Address, port)
                            logger.info("Resolved IPv6 address: ${ipv6Address.hostAddress}:$port")
                            promise?.setSuccess(resolvedAddress)
                        } catch (ex: Exception) {
                            logger.error("Failed to resolve IPv6 address", ex)
                            promise?.setFailure(ex)
                        }
                    }

                    override fun doResolveAll(address: InetSocketAddress?, promise: Promise<MutableList<InetSocketAddress>>?) {
                        try {
                            val hostname = address?.hostName ?: host

                            val port = if (address?.port != null) {
                                logger.info("Using provided port: ${address.port}")
                                address.port
                            } else {
                                logger.info("Using fallback port: 443")
                                443
                            }

                            logger.info("Attempting to resolve all addresses for: $hostname:$port")

                            val ipv6Addresses = InetAddress.getAllByName(hostname)
                                .filter { it is Inet6Address }
                                .map { InetSocketAddress(it, port) }
                                .toMutableList()

                            if (ipv6Addresses.isEmpty()) {
                                promise?.setFailure(IllegalStateException("No IPv6 addresses available for $hostname"))
                            } else {
                                logger.info("Resolved IPv6 addresses: ${ipv6Addresses.joinToString { "${it.address.hostAddress}:${it.port}" }}")
                                promise?.setSuccess(ipv6Addresses)
                            }
                        } catch (e: Exception) {
                            logger.error("Failed to resolve IPv6 addresses", e)
                            promise?.setFailure(e)
                        }
                    }

                    override fun doIsResolved(address: InetSocketAddress?): Boolean {
                        return address?.address != null && !address.isUnresolved
                    }
                }
            }
        }

        // Create IPv6-enabled WebClient ResourceRetriever
        val webClientRetriever = object : JWKSetSource<SecurityContext> {
            private val webClient = WebClient.builder()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.from(
                            TcpClient.create()
                                .resolver(ipv6Resolver)
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        ).secure()
                    )
                )
                .build()

            override fun getJWKSet(
                refreshEvaluator: JWKSetCacheRefreshEvaluator?,
                currentTime: Long,
                context: SecurityContext?
            ): JWKSet {
                val response = webClient.get()
                    .uri(jwkSetUri)
                    .retrieve()
                    .toEntity(String::class.java)
                    .block(Duration.ofSeconds(10))
                    ?: throw IllegalStateException("Failed to retrieve JWK set")

                return JWKSet.parse(
                    response.body ?: throw IllegalStateException("Empty JWK set response")
                )
            }

            override fun close() {
                // No resources to clean up
            }
        }

        // Create JWKSource with cache and rate limiting
        val jwkSource = JWKSourceBuilder.create<SecurityContext>(webClientRetriever)
            .cache(
                TimeUnit.HOURS.toMillis(3),    // Cache lifespan
                TimeUnit.MINUTES.toMillis(30)   // Refresh timeout
            )
            .rateLimited(
                TimeUnit.MINUTES.toMillis(5)
            )   // Rate limit interval
            { event ->
                logger.warn("Rate limit reached for JWK source")
            }
            .refreshAheadCache(false)
            .build()

        // Create JWT processor
        val keySelector = JWSAlgorithmFamilyJWSKeySelector.fromJWKSource(jwkSource)
        val jwtProcessor = DefaultJWTProcessor<SecurityContext>().apply {
            setJWSKeySelector(keySelector)
        }

        // Create the decoder using both our IPv6 WebClient and cached JWT processor
        return NimbusReactiveJwtDecoder { jwt ->
            Mono.fromCallable {
                try {
                    jwtProcessor.process(jwt, null)
                } catch (ex: Exception) {
                    logger.error("JWT processing error", ex)
                    throw ex
                }
            }
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/
