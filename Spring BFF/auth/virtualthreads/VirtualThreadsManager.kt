package com.example.authorizationserver.auth.virtualthreads

import com.example.authorizationserver.api.entities.user.UserEntity
import com.example.authorizationserver.api.repositories.users.UserRepositoryImpl
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import javax.annotation.PreDestroy
import kotlin.coroutines.CoroutineContext

// using Java Virtual Threads (Project Loom), since Spring does not yet support reactive Auth Servers
// https://github.com/spring-projects/spring-authorization-server/issues/152
// https://kt.academy/article/dispatcher-loom

@Configuration
class ExecutorConfig {

    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    @Bean
    fun coroutineDispatcher(): CoroutineContext = executor.asCoroutineDispatcher()

    @PreDestroy
    fun cleanUp() {
        executor.close()
    }
}

@Service
internal class VirtualThreadManager(
    @Autowired
    private val userRepo: UserRepositoryImpl,
    private val dispatcher: CoroutineContext
) {

    internal fun fetchUserEntity(userEmail: String): UserEntity? {

        return try {
            // run the coroutine blocking the current thread until it completes
            runBlocking {
                // switch to the custom dispatcher created from the executor
                withContext(dispatcher) {
                    getUserEntity(userEmail)
                }
            }
        } catch (ex: RejectedExecutionException) {
            throw RuntimeException("Executor was shut down, unable to execute task", ex)
        }
    }

    // get userEntity from DocDatabase
    private suspend fun getUserEntity(userEmail: String): UserEntity? {
        val userEntity = userRepo.getUserByEmail(userEmail)
        return userEntity
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/