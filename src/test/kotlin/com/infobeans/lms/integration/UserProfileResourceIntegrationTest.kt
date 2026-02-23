package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun cleanDb() {
        userRepository.deleteAll()
    }

    private fun seedUser(): User {
        return userRepository.save(
            User(
                id = 0,
                name = "John",
                email = "john@test.com",
                password = passwordEncoder.encode("oldpass"),
                role = Role.STUDENT
            )
        )
    }

    // --------------------------------------------------
    // GET PROFILE
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "john@test.com", roles = ["STUDENT"])
    fun `authenticated user should fetch profile`() {

        seedUser()

        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("john@test.com"))
            .andExpect(jsonPath("$.name").value("John"))
            .andExpect(jsonPath("$.role").value("STUDENT"))
    }

    @Test
    fun `anonymous should not access profile`() {

        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isForbidden)
    }

    // --------------------------------------------------
    // UPDATE PROFILE
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "john@test.com", roles = ["STUDENT"])
    fun `user should update profile name`() {

        seedUser()

        val request = mapOf(
            "name" to "John Updated"
        )

        mockMvc.perform(
            put("/api/v1/users/me")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("John Updated"))
    }

    @Test
    @WithMockUser(username = "john@test.com", roles = ["STUDENT"])
    fun `blank name should return 400`() {

        seedUser()

        val request = mapOf(
            "name" to ""
        )

        mockMvc.perform(
            put("/api/v1/users/me")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // --------------------------------------------------
    // CHANGE PASSWORD
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "john@test.com", roles = ["STUDENT"])
    fun `user should change password successfully`() {

        val user = seedUser()

        val request = mapOf(
            "currentPassword" to "oldpass",
            "newPassword" to "newpass"
        )

        mockMvc.perform(
            patch("/api/v1/users/me/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNoContent)

        val updated = userRepository.findById(user.id).get()
        assert(passwordEncoder.matches("newpass", updated.password))
    }

    @Test
    @WithMockUser(username = "john@test.com", roles = ["STUDENT"])
    fun `wrong current password should return forbidden`() {

        seedUser()

        val request = mapOf(
            "currentPassword" to "wrongpass",
            "newPassword" to "newpass"
        )

        mockMvc.perform(
            patch("/api/v1/users/me/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError)
    }
}