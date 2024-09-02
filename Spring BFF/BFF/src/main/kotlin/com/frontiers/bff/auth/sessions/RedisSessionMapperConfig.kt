package com.frontiers.bff.auth.sessions

import com.frontiers.bff.props.SpringSessionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.session.MapSession
import org.springframework.session.config.ReactiveSessionRepositoryCustomizer
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.session.data.redis.RedisSessionMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.function.BiFunction

/**********************************************************************************************************************/
/************************************************ REDIS CONFIGURATION *************************************************/
/**********************************************************************************************************************/

// more here:
// https://docs.spring.io/spring-session/reference/configuration/redis.html#configuring-redis-session-mapper

@Configuration
internal class RedisSessionMapperConfig() {

    /**
     * Customizes the ReactiveRedisIndexedSessionRepository to use the SafeSessionMapper.
     */
    @Bean
    fun redisSessionRepositoryCustomizer(): ReactiveSessionRepositoryCustomizer<ReactiveRedisIndexedSessionRepository> {
        return ReactiveSessionRepositoryCustomizer { redisSessionRepository ->
            redisSessionRepository.setRedisSessionMapper(
                SafeRedisSessionMapper(
                    redisSessionRepository.sessionRedisOperations
                )
            )
        }
    }

    /**
     * Implementation of SafeSessionMapper.
     * Enhances the default session mapping behavior by adding error handling, ensuring that any mapping issues
     * result in the removal of the problematic session from Redis.
     * Improves the robustness and reliability of session management
     */
    @Component
    internal class SafeRedisSessionMapper(
        private var redisOperations: ReactiveRedisOperations<String, Any>
    ) :  BiFunction<String, Map<String, Any>, Mono<MapSession>>{

        private val springSessionProperties = SpringSessionProperties()
        private val delegate = RedisSessionMapper()

        /**
         * Custom session mapper that delegates to the default RedisSessionMapper.
         * If an exception occurs, the session is deleted from Redis.
         */
        override fun apply(sessionId: String, map: Map<String, Any>): Mono<MapSession> {
            return Mono.defer {
                try {
                    // attempt to apply the session mapping
                    Mono.just(delegate.apply(sessionId, map))
                } catch (ex: IllegalStateException) {
                    // handle exception: delete session from Redis and return empty Mono
                    redisOperations.delete("${springSessionProperties.redis?.sessionNamespace}:$sessionId")
                        .then(Mono.empty<MapSession>())
                }
            }
        }

    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/