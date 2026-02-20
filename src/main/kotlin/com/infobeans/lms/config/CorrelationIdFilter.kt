package com.infobeans.lms.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

/**
 * Filter responsible for correlation ID propagation.
 *
 * Purpose:
 * - Adds correlation ID to each request
 * - Enables traceability in logs
 * - Supports observability requirements
 */
@Component
class CorrelationIdFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val correlationId = request.getHeader("X-Correlation-Id")
            ?: UUID.randomUUID().toString()

        MDC.put("correlationId", correlationId)
        response.setHeader("X-Correlation-Id", correlationId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
