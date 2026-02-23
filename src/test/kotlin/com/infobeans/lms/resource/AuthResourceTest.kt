package com.infobeans.lms.resource

import com.infobeans.lms.dto.*
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.UserRepository
import com.infobeans.lms.service.impl.JwtService
import io.mockk.*
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class AuthResourceTest {

    @MockK lateinit var userRepository: UserRepository
    @MockK lateinit var encoder: PasswordEncoder
    @MockK lateinit var jwtService: JwtService
    @MockK lateinit var request: HttpServletRequest
    @MockK lateinit var response: HttpServletResponse

    @InjectMockKs
    lateinit var authResource: AuthResource

    @Test
    fun `should login successfully`() {

        val authRequest = AuthRequest("test@mail.com", "password")

        val user = User(
            id = 1L,
            name = "Test",
            email = "test@mail.com",
            password = "encodedPass",
            role = Role.STUDENT
        )

        every { userRepository.findByEmail("test@mail.com") } returns user
        every { encoder.matches("password", "encodedPass") } returns true
        every { jwtService.generateAccessToken(any(), any()) } returns "accessToken"
        every { jwtService.generateRefreshToken(any()) } returns "refreshToken"
        every { jwtService.addRefreshTokenCookie(response, "refreshToken") } just Runs

        val result = authResource.login(authRequest, response)

        assertEquals("accessToken", result.accessToken)
        assertEquals("STUDENT", result.role)

        verify(exactly = 1) { userRepository.findByEmail("test@mail.com") }
        verify(exactly = 1) { encoder.matches("password", "encodedPass") }
        verify(exactly = 1) { jwtService.generateAccessToken(any(), any()) }
        verify(exactly = 1) { jwtService.generateRefreshToken(any()) }
    }

    @Test
    fun `should throw exception when email not found`() {

        every { userRepository.findByEmail(any()) } returns null

        assertFailsWith<RuntimeException> {
            authResource.login(AuthRequest("wrong@mail.com", "pass"), response)
        }

        verify { userRepository.findByEmail("wrong@mail.com") }
    }

    @Test
    fun `should throw exception when password invalid`() {

        val user = User(
            id = 1L,
            name = "Test",
            email = "test@mail.com",
            password = "encoded",
            role = Role.STUDENT
        )

        every { userRepository.findByEmail(any()) } returns user
        every { encoder.matches(any(), any()) } returns false

        assertFailsWith<RuntimeException> {
            authResource.login(AuthRequest("test@mail.com", "wrong"), response)
        }
    }


    @Test
    fun `should refresh token successfully`() {

        val refreshCookie = Cookie("refreshToken", "oldToken")

        every { request.cookies } returns arrayOf(refreshCookie)
        every { jwtService.extractEmail("oldToken") } returns "test@mail.com"

        val user = User(
            id = 1L,
            name = "Test",
            email = "test@mail.com",
            password = "encoded",
            role = Role.STUDENT
        )

        every { userRepository.findByEmail("test@mail.com") } returns user
        every { jwtService.generateAccessToken(any(), any()) } returns "newAccess"
        every { jwtService.generateRefreshToken(any()) } returns "newRefresh"
        every { jwtService.addRefreshTokenCookie(response, "newRefresh") } just Runs

        val result = authResource.refresh(request, response)

        assertEquals("newAccess", result.accessToken)
        assertEquals("STUDENT", result.role)
    }

    @Test
    fun `should return 401 when refresh token missing`() {

        every { request.cookies } returns null

        val ex = assertFailsWith<ResponseStatusException> {
            authResource.refresh(request, response)
        }

        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun `should register user successfully`() {

        val requestDto = RegisterUserRequest(
            name = "Test",
            email = "test@mail.com",
            password = "password",
            role = null
        )

        every { userRepository.findByEmail("test@mail.com") } returns null
        every { encoder.encode("password") } returns "encodedPass"
        every { userRepository.save(any()) } returns mockk()

        val response = authResource.register(requestDto)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("User registered successfully", response.body!!.message)
    }

    @Test
    fun `should throw exception when email already exists`() {

        every { userRepository.findByEmail(any()) } returns mockk()

        assertFailsWith<RuntimeException> {
            authResource.register(
                RegisterUserRequest("Test", "test@mail.com", "pass")
            )
        }
    }
}