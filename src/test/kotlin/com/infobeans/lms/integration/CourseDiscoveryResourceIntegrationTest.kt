package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.Enrollment
import com.infobeans.lms.model.Module
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.EnrollmentRepository
import com.infobeans.lms.persistence.ModuleRepository
import com.infobeans.lms.persistence.UserRepository
import org.junit.jupiter.api.BeforeEach
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
class CourseDiscoveryResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var courseRepository: CourseRepository
    @Autowired lateinit var moduleRepository: ModuleRepository
    @Autowired lateinit var enrollmentRepository: EnrollmentRepository

    @BeforeEach
    fun cleanDb() {
        enrollmentRepository.deleteAll()
        moduleRepository.deleteAll()
        courseRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun seedPublishedCourse(): Long {

        val instructor = userRepository.save(
            User(
                name = "Instructor",
                email = "inst@test.com",
                password = "pass",
                role = Role.INSTRUCTOR
            )
        )

        val student = userRepository.save(
            User(
                name = "Student",
                email = "stud@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        val course = courseRepository.save(
            Course(
                title = "Spring Boot Mastery",
                description = "Advanced backend",
                published = true,
                capacity = 50
            ).apply { this.instructor = instructor }
        )

        val module = moduleRepository.save(
            Module(
                name = "Intro",
                materialUrl = "https://example.com"
            ).apply { this.course = course }
        )

        enrollmentRepository.save(
            Enrollment().apply {
                this.user = student
                this.course = course
            }
        )

        return course.id
    }

    // --------------------------------------------------
    // SEARCH COURSES
    // --------------------------------------------------

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `student should search published courses`() {

        seedPublishedCourse()

        mockMvc.perform(get("/api/v1/courses"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Spring Boot Mastery"))
    }

    @Test
    @WithMockUser(roles = ["INSTRUCTOR"])
    fun `instructor should search published courses`() {

        seedPublishedCourse()

        mockMvc.perform(get("/api/v1/courses"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin should search published courses`() {

        seedPublishedCourse()

        mockMvc.perform(get("/api/v1/courses"))
            .andExpect(status().isOk)
    }

    @Test
    fun `anonymous should not access course search`() {

        mockMvc.perform(get("/api/v1/courses"))
            .andExpect(status().isForbidden)
    }

    // --------------------------------------------------
    // FULL COURSE DETAIL
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should access full course detail`() {

        userRepository.save(
            User(
                name = "Admin",
                email = "admin@test.com",
                password = "pass",
                role = Role.ADMIN
            )
        )

        val courseId = seedPublishedCourse()

        mockMvc.perform(get("/api/v1/courses/$courseId/full-detail"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Spring Boot Mastery"))
    }

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should access full course detail`() {

        val courseId = seedPublishedCourse()

        mockMvc.perform(get("/api/v1/courses/$courseId/full-detail"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `student should not access full detail`() {

        val courseId = seedPublishedCourse()

        mockMvc.perform(get("/api/v1/courses/$courseId/full-detail"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should return 404 when course not found`() {

        mockMvc.perform(get("/api/v1/courses/9999/full-detail"))
            .andExpect(status().isNotFound)
    }
}