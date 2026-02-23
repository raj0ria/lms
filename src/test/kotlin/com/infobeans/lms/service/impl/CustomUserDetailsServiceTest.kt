package com.infobeans.lms.service.impl

import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.userdetails.UsernameNotFoundException

@ExtendWith(MockKExtension::class)
class CustomUserDetailsServiceTest {

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    fun `loadUserByUsername should return UserDetails when user exists`() {
        // GIVEN
        val email = "test@infobeans.com"
        val mockUserEntity = User(
            id = 1L,
            name = "Test User",
            email = email,
            password = "encoded_password",
            role = Role.STUDENT // From source [cite: 9]
        )

        every { userRepository.findByEmail(email) } returns mockUserEntity

        // WHEN
        val result = customUserDetailsService.loadUserByUsername(email)

        // THEN
        assertEquals(email, result.username)
        assertEquals("encoded_password", result.password)
        assertTrue(result.authorities.any { it.authority == "ROLE_STUDENT" })

        verify(exactly = 1) { userRepository.findByEmail(email) }
    }

    @Test
    fun `loadUserByUsername should throw UsernameNotFoundException when user missing`() {
        // GIVEN
        val email = "nonexistent@infobeans.com"
        every { userRepository.findByEmail(email) } returns null

        // WHEN & THEN
        assertThrows<UsernameNotFoundException> {
            customUserDetailsService.loadUserByUsername(email)
        }

        verify(exactly = 1) { userRepository.findByEmail(email) }
    }
}