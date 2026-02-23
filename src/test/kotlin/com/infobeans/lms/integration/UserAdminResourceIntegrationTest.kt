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
class UserAdminResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository

    @BeforeEach
    fun cleanDb() {
        userRepository.deleteAll()
    }

    private fun seedAdmin() {
        userRepository.save(
            User(
                id = 0,
                name = "Admin",
                email = "admin@test.com",
                password = "pass",
                role = Role.ADMIN
            )
        )
    }

    // --------------------------------------------------
    // CREATE USER
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should create user`() {

        seedAdmin()

        val request = mapOf(
            "name" to "John",
            "email" to "john@test.com",
            "password" to "pass",
            "role" to "STUDENT"
        )

        mockMvc.perform(
            post("/api/v1/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("john@test.com"))
            .andExpect(jsonPath("$.role").value("STUDENT"))
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `creating duplicate email should return conflict`() {

        seedAdmin()

        userRepository.save(
            User(
                id = 0,
                name = "John",
                email = "john@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        val request = mapOf(
            "name" to "John2",
            "email" to "john@test.com",
            "password" to "pass",
            "role" to "STUDENT"
        )

        mockMvc.perform(
            post("/api/v1/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
    }

    // --------------------------------------------------
    // GET USERS
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should fetch paginated users`() {

        seedAdmin()

        userRepository.save(
            User(
                id = 0,
                name = "Student",
                email = "student@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should fetch user by id`() {

        seedAdmin()

        val saved = userRepository.save(
            User(
                id = 0,
                name = "Student",
                email = "student@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        mockMvc.perform(get("/api/v1/users/${saved.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("student@test.com"))
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `fetching non existing user should return 404`() {

        seedAdmin()

        mockMvc.perform(get("/api/v1/users/999"))
            .andExpect(status().isNotFound)
    }

    // --------------------------------------------------
    // UPDATE USER
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should update user`() {

        seedAdmin()

        val saved = userRepository.save(
            User(
                id = 0,
                name = "Old Name",
                email = "user@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        val request = mapOf(
            "name" to "New Name",
            "role" to "INSTRUCTOR"
        )

        mockMvc.perform(
            put("/api/v1/users/${saved.id}")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Name"))
            .andExpect(jsonPath("$.role").value("INSTRUCTOR"))
    }

    // --------------------------------------------------
    // DELETE USER
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should delete user`() {

        seedAdmin()

        val saved = userRepository.save(
            User(
                id = 0,
                name = "Delete Me",
                email = "delete@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        mockMvc.perform(delete("/api/v1/users/${saved.id}"))
            .andExpect(status().isNoContent)
    }

    // --------------------------------------------------
    // SECURITY
    // --------------------------------------------------

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `non admin should not access user admin endpoints`() {

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `anonymous should not access user admin endpoints`() {

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isForbidden)
    }
}