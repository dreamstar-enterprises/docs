package com.frontiers.bff.auth.handlers.csrf

import com.frontiers.bff.props.CsrfProperties
import org.slf4j.LoggerFactory
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestHandler
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**********************************************************************************************************************/
/****************************************************** HANDLER *******************************************************/
/**********************************************************************************************************************/

// needed for SPAs according to this:
// https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa

/**
 *  ensures that CSRF tokens are managed appropriately depending on whether they are included in headers
 *  or request parameters and incorporates protection mechanisms against specific security threats like BREACH.
 */
@Component
internal class SPACsrfTokenRequestHandler(
    private val csrfProperties: CsrfProperties
) : ServerCsrfTokenRequestHandler {

    private val logger = LoggerFactory.getLogger(SPACsrfTokenRequestHandler::class.java)

    private val delegate: ServerCsrfTokenRequestHandler
            = XorServerCsrfTokenRequestAttributeHandler()

    override fun handle(
        exchange: ServerWebExchange?,
        csrfToken: Mono<CsrfToken>?
    ) {
        /*
         * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
         * the CsrfToken when it is rendered in the response body.
         */
        logger.info("Handling CSRF token: {}", csrfToken)
        delegate.handle(exchange, csrfToken)
    }

    override fun resolveCsrfTokenValue(exchange: ServerWebExchange, csrfToken: CsrfToken): Mono<String>? {
        /*
         * If the request contains a request header, use CsrfTokenRequestAttributeHandler
         * to resolve the CsrfToken. This applies when a single-page application includes
         * the header value automatically, which was obtained via a cookie containing the
         * raw CsrfToken.
         */
        val headerToken = exchange.request.headers.getFirst(csrfProperties.CSRF_HEADER_NAME)
        return if (StringUtils.hasText(headerToken)) {
            logger.info("Resolving CSRF token: {}", csrfToken)
            super.resolveCsrfTokenValue(exchange, csrfToken)
        } else {
            /*
             * In all other cases (e.g. if the request contains a request parameter), use
             * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
             * when a server-side rendered form includes the _csrf request parameter as a
             * hidden input.
             */
            logger.info("Resolving CSRF token: {}", csrfToken)
            delegate.resolveCsrfTokenValue(exchange, csrfToken)
        }
    }
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/