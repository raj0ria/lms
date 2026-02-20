package com.infobeans.lms.config

import com.infobeans.lms.exceptions.JWTExpiredException
import com.infobeans.lms.service.impl.CustomUserDetailsService
import com.infobeans.lms.service.impl.JwtService
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Custom JWT authentication filter.
 *
 * Responsibilities:
 * - Intercepts incoming requests
 * - Extracts Bearer token
 * - Validates JWT
 * - Loads user details
 * - Sets authentication in SecurityContext
 *
 * Executes once per request.
 */
@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userDetailsService: CustomUserDetailsService
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
       val header = request.getHeader("Authorization")
        var email = ""

        // Check if Authorization header contains Bearer token
        if(header != null && header.startsWith("Bearer ")){
            val token = header.substring(7)
            try {
                email = jwtService.extractEmail(token)
            } catch (e: ExpiredJwtException) {
                throw JWTExpiredException("JWT token expired")
            }
            val userDetails = userDetailsService.loadUserByUsername(email)

            val auth = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            )
            SecurityContextHolder.getContext().authentication = auth
        }
        filterChain.doFilter(request, response)
    }
}