package com.frontiers.bff.auth.sessions

import com.frontiers.bff.props.SpringSessionProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisKeyExpiredEvent
import org.springframework.data.redis.listener.KeyspaceEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Configuration
internal class RedisKeyExpirationListenerConfig {

    @Bean
    @Primary
    fun keyExpirationListenerContainer(
        connectionFactory: RedisConnectionFactory
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        return container
    }

    @Bean
    fun redisKeyExpirationListener(
        listenerContainer: RedisMessageListenerContainer
    ): KeyExpirationEventMessageListener {
        return KeyExpirationEventMessageListener(listenerContainer)
    }
}

@Component
internal class KeyExpirationEventMessageListener(
    listenerContainer: RedisMessageListenerContainer
) : KeyspaceEventMessageListener(listenerContainer), ApplicationEventPublisherAware {

    private var applicationEventPublisher: ApplicationEventPublisher? = null
    private val redisNamespace = SpringSessionProperties().redis?.sessionNamespace

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val expiredKey = String(message.body, StandardCharsets.UTF_8)
        // check if the key matches session keys
        if (expiredKey.startsWith("$redisNamespace:")) {
            // perform custom cleanup and logout from Auth0
            handleSessionExpiration(expiredKey)
        }
    }

    override fun doHandleMessage(message: Message) {
        // get the byte array from the message
        val byteArray = message.getBody()

        // create a RedisKeyExpiredEvent using the byte array
        applicationEventPublisher?.publishEvent(
            RedisKeyExpiredEvent<ByteArray>(byteArray)
        )
    }

    private fun handleSessionExpiration(sessionId: String) {
        // 1. Remove session-related data from Redis
        // 2. Trigger RP-Initiated Logout from Auth0
        println("EXPIRED KEY: $sessionId")
    }
}
