package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.dto.RegisterUserRequest
import com.infobeans.lms.enums.Role
import com.infobeans.lms.persistence.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    override lateinit var mockMvc: MockMvc

    @Autowired
    override lateinit var objectMapper: ObjectMapper

    @Test
    fun `should register new user successfully`() {

        val request = RegisterUserRequest(
            name = "John Doe",
            email = "john@test.com",
            password = "password123",
            role = "STUDENT"
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$.message").value("User registered successfully")
            )

        // Verify DB insert
        val savedUser = userRepository.findByEmail("john@test.com")

        assertNotNull(savedUser)
        assertEquals("John Doe", savedUser!!.name)
        assertEquals(Role.STUDENT, savedUser.role)
    }

    @Test
    fun `should fail when registering duplicate email`() {

        // First registration
        val request = RegisterUserRequest(
            name = "John Doe",
            email = "duplicate@test.com",
            password = "password123",
            role = "STUDENT"
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // Second registration with same email
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is5xxServerError)
    }
}