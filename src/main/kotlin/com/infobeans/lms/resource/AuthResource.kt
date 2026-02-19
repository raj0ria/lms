package com.infobeans.lms.resource

import com.infobeans.lms.dto.AuthRequest
import com.infobeans.lms.dto.AuthResponse
import com.infobeans.lms.dto.RegisterResponse
import com.infobeans.lms.dto.RegisterUserRequest
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.UserRepository
import com.infobeans.lms.service.JwtService
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

@RestController
@RequestMapping("/api/auth")
class AuthResource(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest,
              response: HttpServletResponse) : AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw RuntimeException("Invalid email")

        if(!encoder.matches(request.password, user.password)){
            throw RuntimeException("Invalid password")
        }

        val accessToken = jwtService.generateAccessToken(user.email, user.role.name)
        val refreshToken = jwtService.generateRefreshToken(user.email)

        jwtService.addRefreshTokenCookie(response, refreshToken)

        return AuthResponse(
            accessToken = accessToken,
            role = user.role.name
        )
    }

    @PostMapping("/refresh")
    fun refresh(request: HttpServletRequest,
                response: HttpServletResponse): AuthResponse{

        val refreshToken = request.cookies
            ?.firstOrNull { it.name == "refreshToken"}
            ?.value
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        val email = jwtService.extractEmail(refreshToken);

        val user = userRepository.findByEmail(email)
            ?: throw RuntimeException("Invalid credentials")

        val newAccessToken = jwtService.generateAccessToken(user.email, user.role.name)
        val newRefreshToken = jwtService.generateRefreshToken(user.email)
        jwtService.addRefreshTokenCookie(response, newRefreshToken)
        return AuthResponse(
            accessToken = newAccessToken,
            role = user.role.name
        )
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse){
        jwtService.deleteRefreshTokenCookie(response)
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterUserRequest): ResponseEntity<RegisterResponse>{
        if (userRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Username already exists")
        }

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