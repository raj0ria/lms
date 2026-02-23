package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.Enrollment
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.EnrollmentRepository
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
class AdminDashboardResourceIntegrationTest : IntegrationTestBase() {

    @Autowired
    override lateinit var mockMvc: MockMvc

    @Autowired
    override lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var enrollmentRepository: EnrollmentRepository

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun `admin should retrieve dashboard summary`() {

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
                title = "Test Course",
                description = "Desc",
                published = true,
                capacity = 10
            ).apply {
                this.instructor = instructor
            }
        )

        val enrollment = Enrollment().apply {
            this.user = student
            this.course = course
        }

        enrollmentRepository.save(enrollment)

        mockMvc.perform(
            get("/api/v1/admin/dashboard/summary")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalStudents").value(1))
            .andExpect(jsonPath("$.totalInstructors").value(1))
            .andExpect(jsonPath("$.totalCourses").value(1))
            .andExpect(jsonPath("$.totalEnrollments").value(1))
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `non admin should get forbidden`() {

        mockMvc.perform(
            get("/api/v1/admin/dashboard/summary")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `unauthenticated user should also get forbidden`() {

        mockMvc.perform(
            get("/api/v1/admin/dashboard/summary")
        )
            .andExpect(status().isForbidden)
    }
}