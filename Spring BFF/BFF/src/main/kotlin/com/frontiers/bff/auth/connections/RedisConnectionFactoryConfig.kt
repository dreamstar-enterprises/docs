package com.frontiers.bff.auth.connections

import com.frontiers.bff.props.SpringDataProperties
import io.lettuce.core.ClientOptions.DEFAULT_DISCONNECTED_BEHAVIOR
import io.lettuce.core.SocketOptions
import io.lettuce.core.SslOptions
import io.lettuce.core.TimeoutOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterClientOptions.DEFAULT_MAX_REDIRECTS
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.lettuce.core.internal.HostAndPort
import io.lettuce.core.protocol.DecodeBufferPolicies
import io.lettuce.core.protocol.ProtocolVersion
import io.lettuce.core.resource.DefaultClientResources
import io.lettuce.core.resource.DnsResolvers
import io.lettuce.core.resource.MappingSocketAddressResolver
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Duration


/**********************************************************************************************************************/
/************************************************ CONNECTION FACTORY **************************************************/
/**********************************************************************************************************************/

// see here for more:
// https://docs.spring.io/spring-session/reference/web-session.html#websession-redis
// https://docs.spring.io/spring-session/reference/configuration/reactive-redis-indexed.html
// https://github.com/Azure/AzureCacheForRedis/blob/main/Lettuce%20Best%20Practices.md
// https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/src/samples/Azure-Cache-For-Redis/Lettuce/Azure-AAD-Authentication-With-Lettuce.md

/**
 * Establishes a Connection (factory) with Redis
 */
@Configuration
internal class RedisConnectionFactoryConfig(
    private val springDataProperties: SpringDataProperties
) {

    // reactive RedisConnectionFactory for key expiration event handling
    @Bean
    @Primary
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {

        // configure Redis standalone configuration
        val config = RedisStandaloneConfiguration()
        config.hostName = springDataProperties.redis.host
        config.port = springDataProperties.redis.port
        config.setPassword(RedisPassword.of(springDataProperties.redis.password))

        // create client options

        // Create SSL options if SSL is required
        val sslOptions = SslOptions.builder()
            .jdkSslProvider()  // Or use OpenSslProvider if you prefer
            .build()

        // Create timeout options
        val timeoutOptions = TimeoutOptions.builder()
            .fixedTimeout(Duration.ofSeconds(20))
            .timeoutCommands(true)
            .build()

        // cluster specific settings for optimal reliability.
        val clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enablePeriodicRefresh(Duration.ofSeconds(5))
            .dynamicRefreshSources(false)
            .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(5))
            .enableAllAdaptiveRefreshTriggers().build()

        // create socket options
        val socketOptions = SocketOptions.builder()
            .keepAlive(SocketOptions.DEFAULT_SO_KEEPALIVE)
            .tcpNoDelay(SocketOptions.DEFAULT_SO_NO_DELAY)
            // time to wait for connection to be established, before considering it as a failed connection
            .connectTimeout(Duration.ofSeconds(60))
            .build()

        val mappingFunction: (HostAndPort) -> HostAndPort = { hostAndPort ->
            val host = springDataProperties.redis.host
            val addresses: Array<InetAddress> = try {
                DnsResolvers.JVM_DEFAULT.resolve(host)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                emptyArray() // Handle error and return an empty array
            }

            val cacheIP = addresses.firstOrNull()?.hostAddress ?: ""
            var finalAddress = hostAndPort

            if (hostAndPort.hostText == cacheIP) {
                finalAddress = HostAndPort.of(host, hostAndPort.port)
            }

            finalAddress
        }

        val resolver = MappingSocketAddressResolver.create(DnsResolvers.JVM_DEFAULT, mappingFunction)

        // customize thread pool size
        val clientResources = DefaultClientResources.builder()
            .ioThreadPoolSize(8)
            .computationThreadPoolSize(8)
            .socketAddressResolver(resolver)
            .build()

        val clusterClientOptions = ClusterClientOptions.builder()
            .autoReconnect(true)
            .pingBeforeActivateConnection(true)
            .sslOptions(sslOptions)
            .timeoutOptions(timeoutOptions)
            .socketOptions(socketOptions)
            .topologyRefreshOptions(clusterTopologyRefreshOptions)
            .validateClusterNodeMembership(true)
            .suspendReconnectOnProtocolFailure(true)
            .disconnectedBehavior(DEFAULT_DISCONNECTED_BEHAVIOR)
            .decodeBufferPolicy(DecodeBufferPolicies.ratio(0.5F))
            .requestQueueSize(1000)
            .maxRedirects(DEFAULT_MAX_REDIRECTS)
            .publishOnScheduler(true) //DEFAULT_PUBLISH_ON_SCHEDULER.
            .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
            .build()

        // configure connection pool settings
        fun buildLettucePoolConfig(): GenericObjectPoolConfig<Any> {
            val poolConfig = GenericObjectPoolConfig<Any>()
            poolConfig.maxTotal = 100
            poolConfig.maxIdle = 50
            poolConfig.minIdle = 10
            poolConfig.setMaxWait(Duration.ofSeconds(120))
            poolConfig.timeBetweenEvictionRuns = Duration.ofSeconds(120)
            poolConfig.minEvictableIdleTime = Duration.ofMinutes(5)
            poolConfig.testOnBorrow = true
            poolConfig.testWhileIdle = true
            poolConfig.testOnReturn = true
            poolConfig.blockWhenExhausted = true
            poolConfig.lifo = true
            return poolConfig
        }

        // create Lettuce client configuration with authentication details
        val clientConfig = LettucePoolingClientConfiguration.builder()
            // maximum time allowed for a Redis command to execute before the operation is considered timed out.
            .commandTimeout(Duration.ofSeconds(60))
            .clientResources(clientResources)
            .clientOptions(clusterClientOptions)
            .poolConfig(buildLettucePoolConfig())
            .useSsl()
            .build()

        // create Lettuce connection factory
        return LettuceConnectionFactory(config, clientConfig).apply {
            afterPropertiesSet()
            validateConnection = false
            setShareNativeConnection(true)
        }
    }

}


/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/