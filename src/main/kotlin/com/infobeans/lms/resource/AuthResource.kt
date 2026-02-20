package com.infobeans.lms.resource

import com.infobeans.lms.dto.AuthRequest
import com.infobeans.lms.dto.AuthResponse
import com.infobeans.lms.dto.RegisterResponse
import com.infobeans.lms.dto.RegisterUserRequest
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.UserRepository
import com.infobeans.lms.service.impl.JwtService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * Authentication Controller for LMS application.
 *
 * Responsibilities:
 * - User registration
 * - User login (JWT generation)
 * - Token refresh lifecycle
 * - Logout (refresh token invalidation)
 *
 * Security Design (as per LMS requirements):
 * - Stateless authentication using JWT
 * - Access token (short-lived)
 * - Refresh token (stored in HttpOnly cookie)
 * - RBAC enforced via role claim in access token
 *
 * Base Path: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
class AuthResource(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    /**
     * Authenticates user credentials and generates JWT tokens.
     *
     * Flow:
     * 1. Validate email exists
     * 2. Validate password using BCrypt
     * 3. Generate access token (short-lived)
     * 4. Generate refresh token (long-lived)
     * 5. Store refresh token in HttpOnly cookie
     *
     * @param request Login request containing email and password
     * @param response HTTP response to attach refresh cookie
     * @return AuthResponse containing access token and role
     */
    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest,
              response: HttpServletResponse) : AuthResponse {

        // Validate user existence
        val user = userRepository.findByEmail(request.email)
            ?: throw RuntimeException("Invalid email")

        // Validate password using encoded hash comparison
        if(!encoder.matches(request.password, user.password)){
            throw RuntimeException("Invalid password")
        }

        // Generate JWT tokens
        val accessToken = jwtService.generateAccessToken(user.email, user.role.name)
        val refreshToken = jwtService.generateRefreshToken(user.email)

        // Store refresh token in secure HttpOnly cookie
        jwtService.addRefreshTokenCookie(response, refreshToken)

        return AuthResponse(
            accessToken = accessToken,
            role = user.role.name
        )
    }

    /**
     * Generates new access token using valid refresh token.
     *
     * Flow:
     * 1. Extract refresh token from cookie
     * 2. Validate and extract email from token
     * 3. Generate new access token
     * 4. Rotate refresh token (new refresh token issued)
     *
     * @param request HTTP request containing refresh cookie
     * @param response HTTP response to attach new refresh cookie
     * @return new AuthResponse with fresh access token
     */
    @PostMapping("/refresh")
    fun refresh(request: HttpServletRequest,
                response: HttpServletResponse): AuthResponse{

        // Extract refresh token from cookies
        val refreshToken = request.cookies
            ?.firstOrNull { it.name == "refreshToken"}
            ?.value
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        // Extract email from token
        val email = jwtService.extractEmail(refreshToken);

        // Validate user existence
        val user = userRepository.findByEmail(email)
            ?: throw RuntimeException("Invalid credentials")

        // Issue new tokens (token rotation strategy)
        val newAccessToken = jwtService.generateAccessToken(user.email, user.role.name)
        val newRefreshToken = jwtService.generateRefreshToken(user.email)

        // Replace refresh cookie
        jwtService.addRefreshTokenCookie(response, newRefreshToken)
        return AuthResponse(
            accessToken = newAccessToken,
            role = user.role.name
        )
    }

    /**
     * Logs out the user by invalidating refresh token cookie.
     *
     * Since system is stateless:
     * - No server-side session invalidation
     * - Refresh token cookie is deleted
     *
     * @param response HTTP response
     */
    @PostMapping("/logout")
    fun logout(response: HttpServletResponse){
        jwtService.deleteRefreshTokenCookie(response)
    }

    /**
     * Registers a new user in the system.
     *
     * Business Rules:
     * - Email must be unique (unique index enforced at DB level)
     * - Default role = STUDENT if not provided
     * - Role must be valid enum value
     * - Password stored using BCrypt hashing
     *
     * @param request registration details
     * @return ResponseEntity with success message
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterUserRequest): ResponseEntity<RegisterResponse>{

        // Check for duplicate email
        if (userRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Username already exists")
        }

        // Determine user role (default STUDENT)
        val role = when {
            request.role.isNullOrBlank() -> Role.STUDENT

            else -> {
                try {
                    Role.valueOf(request.role.uppercase())
                } catch (ex: Exception) {
                    throw IllegalArgumentException("Invalid role: ${request.role}")
                }
            }
        }

        // Save user with encrypted password
        userRepository.save(
            User(
                name = request.name,
                email = request.email,
                password = encoder.encode(request.password),
                role =  role
            )
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RegisterResponse("User registered successfully"))
    }
}