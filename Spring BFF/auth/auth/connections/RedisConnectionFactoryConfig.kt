//package com.example.authorizationserver.auth.connections
//
//import com.example.authorizationserver.props.SpringDataProperties
//import io.lettuce.core.ClientOptions
//import io.lettuce.core.SocketOptions
//import io.lettuce.core.resource.DefaultClientResources
//import org.apache.commons.pool2.impl.GenericObjectPoolConfig
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import org.springframework.data.redis.connection.RedisConnectionFactory
//import org.springframework.data.redis.connection.RedisPassword
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
//import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
//import java.time.Duration
//
///**********************************************************************************************************************/
///************************************************ CONNECTION FACTORY **************************************************/
///**********************************************************************************************************************/
//
//// see here for more:
//// https://docs.spring.io/spring-session/reference/web-session.html#websession-redis
//// https://docs.spring.io/spring-session/reference/configuration/redis.html
//
//@Configuration
//internal class RedisConnectionFactoryConfig(
//    private val springDataProperties: SpringDataProperties
//) {
//
//    @Bean
//    @Primary
//    fun redisConnectionFactory(): RedisConnectionFactory {
//
//        println("Configuring Redis standalone configuration...")
//
//        // configure Redis standalone configuration
//        val config = RedisStandaloneConfiguration()
//        config.hostName = springDataProperties.redis.host
//        config.port = springDataProperties.redis.port
//        config.setPassword(RedisPassword.of(springDataProperties.redis.password))
//
//        println("Redis host: ${config.hostName}")
//        println("Redis port: ${config.port}")
//        println("Redis password set: ${springDataProperties.redis.password.isNotEmpty()}")
//
//        println("Creating socket options...")
//
//        // create socket options
//        val socketOptions = SocketOptions.builder()
//            .keepAlive(true)
//            .connectTimeout(Duration.ofSeconds(60))
//            .build()
//
//        println("Creating client options...")
//
//        // create client options
//        val clientOptions = ClientOptions.builder()
//            .autoReconnect(true)
//            .pingBeforeActivateConnection(true)
//            .socketOptions(socketOptions)
//            .build()
//
//        println("Customizing thread pool size...")
//
//        // customize thread pool size
//        val clientResources = DefaultClientResources.builder()
//            .ioThreadPoolSize(4)
//            .computationThreadPoolSize(4)
//            .build()
//
//        println("Creating Lettuce client configuration with authentication details...")
//
//        // create Lettuce client configuration with authentication details
//        val clientConfig = LettucePoolingClientConfiguration.builder()
//            .commandTimeout(Duration.ofSeconds(60))
//            .clientResources(clientResources)
//            .clientOptions(clientOptions)
//            .poolConfig(buildLettucePoolConfig())
//            .useSsl()
//            .build()
//
//        println("Creating Lettuce connection factory...")
//
//        // create Lettuce connection factory
//        val factory =  LettuceConnectionFactory(config, clientConfig).apply {
//            afterPropertiesSet()
//        }
//
//        println("Lettuce connection factory created successfully.")
//
//        return factory
//    }
//
//    // configure connection pool settings
//    protected fun buildLettucePoolConfig(): GenericObjectPoolConfig<Any> {
//        println("Configuring connection pool settings...")
//        val poolConfig = GenericObjectPoolConfig<Any>()
//        poolConfig.maxTotal = 100
//        poolConfig.maxIdle = 50
//        poolConfig.minIdle = 10
//        poolConfig.setMaxWait(Duration.ofSeconds(60))
//        poolConfig.timeBetweenEvictionRuns = Duration.ofSeconds(60)
//        poolConfig.minEvictableIdleTime = Duration.ofMinutes(5)
//        poolConfig.testOnBorrow = true
//        poolConfig.testWhileIdle = true
//        poolConfig.testOnReturn = true
//        println("Connection pool settings configured.")
//        return poolConfig
//    }
//
//}
//
///**********************************************************************************************************************/
///**************************************************** END OF KOTLIN ***************************************************/
///**********************************************************************************************************************/