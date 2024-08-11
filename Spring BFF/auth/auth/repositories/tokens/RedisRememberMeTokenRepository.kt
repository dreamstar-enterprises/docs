//package com.example.authorizationserver.auth.repositories.tokens
//
//import com.example.authorizationserver.props.RememberMeProperties
//import org.springframework.context.annotation.Configuration
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.data.redis.core.ScanOptions
//import org.springframework.data.redis.serializer.StringRedisSerializer
//import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken
//import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//@Configuration
//internal class RedisRememberMeTokenRepository(
//    private val redisTemplate: RedisTemplate<String, String>
//) : PersistentTokenRepository {
//
//    /**
//     * Creates a new token and stores it in Redis with an expiration time of 14 days.
//     */
//    companion object {
//        private val rememberMeProperties = RememberMeProperties()
//        private val TOKEN_VALID_DAYS = rememberMeProperties.TOKEN_VALID_DAYS
//        private val USERNAME = rememberMeProperties.USERNAME
//        private val TOKEN = rememberMeProperties.TOKEN
//        private val LAST_USED_DATE = rememberMeProperties.LAST_USED_DATE
//        private val NAME_SPACE = rememberMeProperties.NAME_SPACE
//        private val stringRedisSerializer = StringRedisSerializer()
//    }
//
//    /**
//     * Creates a new token and stores it in Redis with an expiration time of 14 days.
//     */
//    override fun createNewToken(token: PersistentRememberMeToken) {
//        val key = generateKey(token.series)
//        val data = mapOf(
//            USERNAME to token.username,
//            TOKEN to token.tokenValue,
//            LAST_USED_DATE to token.date.time.toString()
//        )
//        redisTemplate.opsForHash<String, String>().putAll(key, data)
//        redisTemplate.expire(key, TOKEN_VALID_DAYS.toLong(), TimeUnit.DAYS)
//    }
//
//    /**
//     * Updates an existing token in Redis with new values and resets its expiration time.
//     */
//    override fun updateToken(series: String, tokenValue: String, lastUsed: Date) {
//        val key = generateKey(series)
//        val data = mapOf(
//            TOKEN to tokenValue,
//            LAST_USED_DATE to lastUsed.time.toString()
//        )
//        redisTemplate.opsForHash<String, String>().putAll(key, data)
//        redisTemplate.expire(key, TOKEN_VALID_DAYS.toLong(), TimeUnit.DAYS)
//    }
//
//
//    /**
//     * Retrieves a token from Redis using the series ID.
//     * Returns a PersistentRememberMeToken if the token exists; otherwise, returns null.
//     */
//    override fun getTokenForSeries(seriesId: String): PersistentRememberMeToken? {
//        val key = generateKey(seriesId)
//        val hashValues = redisTemplate.opsForHash<String, String>()
//            .multiGet(key, listOf(USERNAME, TOKEN, LAST_USED_DATE))
//
//        val username = hashValues.get(0)
//        val tokenValue = hashValues.get(1)
//        val date = hashValues.get(2)?.toLongOrNull()
//
//        if (username == null || tokenValue == null || date == null) {
//            return null
//        }
//
//        val time = Date(date)
//        return PersistentRememberMeToken(username, seriesId, tokenValue, time)
//    }
//
//    /**
//     * Removes all tokens associated with the given username from Redis.
//     */
//    override fun removeUserTokens(username: String) {
//        val hashKey = stringRedisSerializer.serialize(USERNAME)
//        val redisConnection = redisTemplate.connectionFactory?.connection
//            ?: throw IllegalStateException("Redis connection factory is not available")
//
//        redisConnection.use { connection ->
//            val cursor = connection.scan(
//                ScanOptions.scanOptions()
//                    .match(generateKey("*"))
//                    .count(1024).build()
//            )
//            cursor.use { scanCursor ->
//                while (scanCursor.hasNext()) {
//                    val key = scanCursor.next()
//                    val hashValue = connection.hGet(key, hashKey)
//                    val storedUsername = stringRedisSerializer.deserialize(hashValue)
//                    if (username == storedUsername) {
//                        connection.expire(key, 0L)
//                        return
//                    }
//                }
//            }
//        }
//    }
//
//    private fun generateKey(series: String) = NAME_SPACE + series
//}
//
///**********************************************************************************************************************/
///**************************************************** END OF KOTLIN ***************************************************/
///**********************************************************************************************************************/