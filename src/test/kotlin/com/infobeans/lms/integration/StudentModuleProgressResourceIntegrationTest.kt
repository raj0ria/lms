package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.dto.UpdateModuleProgressRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentModuleProgressResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var courseRepository: CourseRepository
    @Autowired lateinit var moduleRepository: ModuleRepository
    @Autowired lateinit var enrollmentRepository: EnrollmentRepository
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
                id = 0,
                name = "Instructor",
                email = "inst@test.com",
                password = "pass",
                role = Role.INSTRUCTOR
            )
        )

        val student = userRepository.save(
            User(
                id = 0,
                name = "Student",
                email = "student@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        val course = courseRepository.save(
            Course(
                id = 0,
                title = "Spring Boot",
                description = "Backend",
                published = true,
                capacity = 10
            ).apply {
                this.instructor = instructor
            }
        )

        val module = moduleRepository.save(
            Module(
                id = 0,
                name = "Intro",
                materialUrl = "url"
            ).apply {
                this.course = course
            }
        )

        val enrollment = enrollmentRepository.save(
            Enrollment(
                id = 0
            ).apply {
                this.user = student
                this.course = course
            }
        )

        statusRepository.save(
            StudentEnrollmentStatus(
                id = 0,
                status = EnrollmentStatus.NOT_STARTED
            ).apply {
                this.enrollment = enrollment
                this.module = module
            }
        )

        return module.id
    }

    // --------------------------------------------------
    // SUCCESS
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `student should update module progress`() {

        val moduleId = seedData()

        val request = UpdateModuleProgressRequest(
            status = EnrollmentStatus.COMPLETED
        )

        mockMvc.perform(
            patch("/api/v1/modules/$moduleId/progress")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNoContent)
    }

    // --------------------------------------------------
    // MODULE NOT FOUND
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "student@test.com", roles = ["STUDENT"])
    fun `updating non existing module should return 404`() {

        userRepository.save(
            User(
                id = 0,
                name = "Student",
                email = "student@test.com",
                password = "pass",
                role = Role.STUDENT
            )
        )

        val request = UpdateModuleProgressRequest(
            status = EnrollmentStatus.COMPLETED
        )

        mockMvc.perform(
            patch("/api/v1/modules/999/progress")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError)
    }

    // --------------------------------------------------
    // ROLE RESTRICTIONS
    // --------------------------------------------------

    @Test
    @WithMockUser(roles = ["INSTRUCTOR"])
    fun `instructor should not update module progress`() {

        mockMvc.perform(
            patch("/api/v1/modules/1/progress")
        )
            .andExpect(status().is5xxServerError)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin should not update module progress`() {

        mockMvc.perform(
            patch("/api/v1/modules/1/progress")
        )
            .andExpect(status().is5xxServerError)
    }

    @Test
    fun `anonymous should not update module progress`() {

        mockMvc.perform(
            patch("/api/v1/modules/1/progress")
        )
            .andExpect(status().isForbidden)
    }
}