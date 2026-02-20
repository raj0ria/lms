package com.infobeans.lms.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import kotlin.text.toByteArray

/**
 * Service responsible for handling JWT token lifecycle operations.
 *
 * Responsibilities:
 * - Generate Access Tokens (short-lived, 15 min)
 * - Generate Refresh Tokens (long-lived, 7 days)
 * - Extract user identity from token
 * - Manage HTTP-only refresh token cookies
 *
 * Security Requirements (as per LMS specification):
 * - Stateless session management
 * - JWT-based authentication
 * - Secure refresh lifecycle
 *
 * Tokens are signed using HMAC SHA key derived from application secret.
 */

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access.expiration}") val accessExpMs: Long,
    @Value("\${jwt.refresh.expiration}") val refreshExpMs: Long
) {
    /** Signing key used for JWT signature validation */
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    /**
     * Generates a short-lived access token.
     *
     * @param email authenticated user's email (subject)
     * @param role user role for RBAC enforcement
     * @return signed JWT access token
     */
    fun generateAccessToken(email: String, role: String) =
        Jwts.builder()
            .subject(email)
            .claim("role", role)
            .expiration(Date(System.currentTimeMillis() + accessExpMs))
            .signWith(key)
            .compact()

    /**
     * Generates a long-lived refresh token.
     *
     * @param email authenticated user's email
     * @return signed JWT refresh token
     */
    fun generateRefreshToken(email: String) =
        Jwts.builder()
            .subject(email)
            .expiration(Date(System.currentTimeMillis() + refreshExpMs))
            .signWith(key)
            .compact()

    /**
     * Adds refresh token as secure HTTP-only cookie.
     *
     * - HttpOnly prevents JavaScript access (XSS protection)
     * - Used for refresh endpoint
     *
     * @param response HttpServletResponse
     * @param token refresh token value
     */
    fun addRefreshTokenCookie(
        response: HttpServletResponse,
        token: String
    ){
        val cookie = Cookie("refreshToken", token)
        cookie.isHttpOnly = true
        cookie.path = "api/auth"
        cookie.maxAge = refreshExpMs.toInt()
        response.addCookie(cookie)
    }

    /**
     * Deletes refresh token cookie during logout.
     *
     * @param response HttpServletResponse
     */
    fun deleteRefreshTokenCookie(
        response: HttpServletResponse
    ){
        val cookie = Cookie("refreshToken", "")
        cookie.isHttpOnly = true
        cookie.path = "api/auth"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }

    /**
     * Extracts email (subject) from JWT token.
     *
     * @param token signed JWT
     * @return user email
     */ 
    fun extractEmail(token: String) =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token)
            .payload.subject
}