package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.enums.EnrollmentStatus
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.*
import com.infobeans.lms.persistence.*
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
class InstructorCourseAnalyticsResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var courseRepository: CourseRepository
    @Autowired lateinit var enrollmentRepository: EnrollmentRepository
    @Autowired lateinit var moduleRepository: ModuleRepository
    @Autowired lateinit var statusRepository: StudentEnrollmentStatusRepository

    @BeforeEach
    fun cleanDb() {
        statusRepository.deleteAll()
        enrollmentRepository.deleteAll()
        moduleRepository.deleteAll()
        courseRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun seedData(): Long {

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
                title = "Spring Boot",
                description = "Backend",
                published = true,
                capacity = 10
            ).apply { this.instructor = instructor }
        )

        val module1 = moduleRepository.save(
            Module(
                name = "Intro",
                materialUrl = "url"
            ).apply { this.course = course }
        )

        val module2 = moduleRepository.save(
            Module(
                name = "JPA",
                materialUrl = "url"
            ).apply { this.course = course }
        )

        val enrollment = enrollmentRepository.save(
            Enrollment().apply {
                this.user = student
                this.course = course
            }
        )

        statusRepository.saveAll(
            listOf(
                StudentEnrollmentStatus(
                    status = EnrollmentStatus.COMPLETED
                ).apply {
                    this.enrollment = enrollment
                    this.module = module1
                },
                StudentEnrollmentStatus(
                    status = EnrollmentStatus.NOT_STARTED
                ).apply {
                    this.enrollment = enrollment
                    this.module = module2
                }
            )
        )

        return course.id
    }

    // --------------------------------------------------
    // SUCCESS
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should fetch enrolled students`() {

        val courseId = seedData()

        mockMvc.perform(get("/api/v1/instructors/courses/$courseId/students"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].studentName").value("Student"))
            .andExpect(jsonPath("$.content[0].totalModules").value(2))
            .andExpect(jsonPath("$.content[0].completedModules").value(1))
            .andExpect(jsonPath("$.content[0].progressPercentage").value(50))
    }

    // --------------------------------------------------
    // ROLE RESTRICTIONS
    // --------------------------------------------------

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `student should not access analytics`() {

        val courseId = seedData()

        mockMvc.perform(get("/api/v1/instructors/courses/$courseId/students"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin should not access analytics`() {

        val courseId = seedData()

        mockMvc.perform(get("/api/v1/instructors/courses/$courseId/students"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `anonymous should not access analytics`() {

        val courseId = seedData()

        mockMvc.perform(get("/api/v1/instructors/courses/$courseId/students"))
            .andExpect(status().isForbidden)
    }

    // --------------------------------------------------
    // NOT FOUND
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `should return 404 when course not found`() {

        userRepository.save(
            User(
                name = "Instructor",
                email = "inst@test.com",
                password = "pass",
                role = Role.INSTRUCTOR
            )
        )

        mockMvc.perform(get("/api/v1/instructors/courses/9999/students"))
            .andExpect(status().isNotFound)
    }
}