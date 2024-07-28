package com.example.timesheetapi.auth.security.repositories

import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/***************************************************** REPOSITORY *****************************************************/
/**********************************************************************************************************************/

@Configuration
internal class SecurityContextConfig() : ServerSecurityContextRepository {

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        return exchange.getSession().doOnNext { session ->
            println("Saving SecurityContext: $context in session: ${session.id}")
            session.attributes["SPRING_SECURITY_CONTEXT"] = context
        }.then()
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        return exchange.getSession().flatMap { session ->
            val context = session.attributes["SPRING_SECURITY_CONTEXT"] as? SecurityContext
            println("Loading SecurityContext: $context from session: ${session.id}")
            Mono.justOrEmpty(context)
        }
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/