package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminCourseResourceIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    override lateinit var mockMvc: MockMvc

    @Autowired
    override lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should fetch all courses with pagination`() {

        val instructor = userRepository.save(
            User(
                name = "Instructor",
                email = "inst@test.com",
                password = "pass",
                role = Role.INSTRUCTOR
            )
        )

        courseRepository.save(
            Course(
                title = "Spring Boot",
                description = "Backend course",
                capacity = 10,
                published = true
            ).apply { this.instructor = instructor }
        )

        courseRepository.save(
            Course(
                title = "Kotlin Advanced",
                description = "Kotlin deep dive",
                capacity = 20,
                published = false
            ).apply { this.instructor = instructor }
        )

        mockMvc.perform(
            get("/api/v1/admin/courses")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.page").value(0))
    }
}