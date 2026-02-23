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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnrollmentResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var courseRepository: CourseRepository
    @Autowired lateinit var enrollmentRepository: EnrollmentRepository
    @Autowired lateinit var moduleRepository: ModuleRepository

    @BeforeEach
    fun cleanDb() {
        enrollmentRepository.deleteAll()
        moduleRepository.deleteAll()
        courseRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun seedCourse(capacity: Int = 10, published: Boolean = true): Long {

        val instructor = userRepository.save(
            User(
                name = "Instructor",
                email = "inst@test.com",
                password = "pass",
                role = Role.INSTRUCTOR
            )
        )

        val course = courseRepository.save(
            Course(
                title = "Spring Boot",
                description = "Backend",
                published = published,
                capacity = capacity
            ).apply { this.instructor = instructor }
        )

        moduleRepository.save(
            Module(
                name = "Intro",
                materialUrl = "url"
            ).apply { this.course = course }
        )

        return course.id
    }

    private fun seedStudent(email: String = "student@test.com"): User =
        userRepository.save(
            User(
                name = "Student",
                email = email,
                password = "pass",
                role = Role.STUDENT
            )
        )

    // --------------------------------------------------
    // ENROLL SUCCESS
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `student should enroll successfully`() {

        val student = seedStudent()
        val courseId = seedCourse()

        mockMvc.perform(post("/api/v1/courses/$courseId/enroll"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.courseTitle").value("Spring Boot"))

        assert(enrollmentRepository.count() == 1L)
    }

    // --------------------------------------------------
    // DUPLICATE ENROLLMENT
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `should return conflict when already enrolled`() {

        val student = seedStudent()
        val courseId = seedCourse()

        enrollmentRepository.save(
            Enrollment().apply {
                this.user = student
                this.course = courseRepository.findById(courseId).get()
            }
        )

        mockMvc.perform(post("/api/v1/courses/$courseId/enroll"))
            .andExpect(status().isBadRequest)
    }

    // --------------------------------------------------
    // CAPACITY EXCEEDED
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `should return bad request when capacity exceeded`() {

        val student = seedStudent()
        val courseId = seedCourse(capacity = 0)

        mockMvc.perform(post("/api/v1/courses/$courseId/enroll"))
            .andExpect(status().isBadRequest)
    }

    // --------------------------------------------------
    // COURSE NOT FOUND
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `should return not found when course does not exist`() {

        seedStudent()

        mockMvc.perform(post("/api/v1/courses/9999/enroll"))
            .andExpect(status().isNotFound)
    }

    // --------------------------------------------------
    // UNAUTHORIZED ROLE
    // --------------------------------------------------

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin should not enroll`() {

        val courseId = seedCourse()

        mockMvc.perform(post("/api/v1/courses/$courseId/enroll"))
            .andExpect(status().isForbidden)
    }

    // --------------------------------------------------
    // UNENROLL SUCCESS
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `student should unenroll successfully`() {

        val student = seedStudent()
        val courseId = seedCourse()

        val enrollment = enrollmentRepository.save(
            Enrollment().apply {
                this.user = student
                this.course = courseRepository.findById(courseId).get()
            }
        )

        mockMvc.perform(delete("/api/v1/courses/$courseId/unenroll"))
            .andExpect(status().isNoContent)

        assert(enrollmentRepository.count() == 0L)
    }

    // --------------------------------------------------
    // UNENROLL NOT FOUND
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `should businees rule violationwhen unenrollment does not exist`() {

        seedStudent()
        val courseId = seedCourse()

        mockMvc.perform(delete("/api/v1/courses/$courseId/unenroll"))
            .andExpect(status().isBadRequest)
    }
}