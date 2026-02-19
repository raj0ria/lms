package com.infobeans.lms.service.impl

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import kotlin.text.toByteArray

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access.expiration}") val accessExpMs: Long,
    @Value("\${jwt.refresh.expiration}") val refreshExpMs: Long
) {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(email: String, role: String) =
        Jwts.builder()
            .subject(email)
            .claim("role", role)
            .expiration(Date(System.currentTimeMillis() + accessExpMs))
            .signWith(key)
            .compact()

    fun generateRefreshToken(email: String) =
        Jwts.builder()
            .subject(email)
            .expiration(Date(System.currentTimeMillis() + refreshExpMs))
            .signWith(key)
            .compact()

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

    fun deleteRefreshTokenCookie(
        response: HttpServletResponse
    ){
        val cookie = Cookie("refreshToken", "")
        cookie.isHttpOnly = true
        cookie.path = "api/auth"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }

    fun extractEmail(token: String) =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token)
            .payload.subject
}