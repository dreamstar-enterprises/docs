package com.example.gateway.auth.redis

import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import io.lettuce.core.resource.DefaultClientResources
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import java.time.Duration

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

// see here for more:
// https://docs.spring.io/spring-session/reference/web-session.html#websession-redis
// https://docs.spring.io/spring-session/reference/configuration/reactive-redis-indexed.html

@Configuration
class RedisConnectionFactoryConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var redisHostName: String

    @Value("\${spring.data.redis.password}")
    private lateinit var redisPassword: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 6380

    @Bean
    @Primary
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {

        // configure Redis standalone configuration
        val config = RedisStandaloneConfiguration()
        config.hostName = redisHostName
        config.port = redisPort
        config.setPassword(RedisPassword.of(redisPassword))

        // create socket options
        val socketOptions = SocketOptions.builder()
            .keepAlive(true)
            .connectTimeout(Duration.ofSeconds(60))
            .build()

        // create client options
        val clientOptions = ClientOptions.builder()
            .autoReconnect(true)
            .pingBeforeActivateConnection(true)
            .socketOptions(socketOptions)
            .build()

        // customize thread pool size
        val clientResources = DefaultClientResources.builder()
            .ioThreadPoolSize(4)
            .computationThreadPoolSize(4)
            .build()

        // create Lettuce client configuration with authentication details
        val clientConfig = LettucePoolingClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(60))
            .clientResources(clientResources)
            .clientOptions(clientOptions)
            .poolConfig(buildLettucePoolConfig())
            .useSsl()
            .build()

        // create Lettuce connection factory
        return LettuceConnectionFactory(config, clientConfig).apply {
            afterPropertiesSet()
        }
    }

    // configure connection pool settings
    protected fun buildLettucePoolConfig(): GenericObjectPoolConfig<Any> {
        val poolConfig = GenericObjectPoolConfig<Any>()
        poolConfig.maxTotal = 100
        poolConfig.maxIdle = 50
        poolConfig.minIdle = 10
        poolConfig.setMaxWait(Duration.ofSeconds(60))
        poolConfig.timeBetweenEvictionRuns = Duration.ofSeconds(60)
        poolConfig.minEvictableIdleTime = Duration.ofMinutes(5)
        poolConfig.testOnBorrow = true
        poolConfig.testWhileIdle = true
        poolConfig.testOnReturn = true
        return poolConfig
    }

}


/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/