package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
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
class InstructorCourseResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var courseRepository: CourseRepository

    @BeforeEach
    fun cleanDb() {
        courseRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun seedInstructor(email: String = "inst@test.com"): User =
        userRepository.save(
            User(
                name = "Instructor",
                email = email,
                password = "pass",
                role = Role.INSTRUCTOR
            )
        )

    private fun seedCourse(instructor: User): Long =
        courseRepository.save(
            Course(
                title = "Spring Boot",
                description = "Backend",
                published = false,
                capacity = 20
            ).apply { this.instructor = instructor }
        ).id

    // --------------------------------------------------
    // CREATE
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should create course`() {

        seedInstructor()

        val request = mapOf(
            "title" to "Kotlin Masterclass",
            "description" to "Advanced Kotlin",
            "capacity" to 30
        )

        mockMvc.perform(
            post("/api/v1/instructor/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Kotlin Masterclass"))

        assert(courseRepository.count() == 1L)
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `student should not create course`() {

        val request = mapOf(
            "title" to "Illegal Course",
            "capacity" to 10
        )

        mockMvc.perform(
            post("/api/v1/instructor/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `should fail validation when capacity is invalid`() {

        seedInstructor()

        val request = mapOf(
            "title" to "",
            "capacity" to -5
        )

        mockMvc.perform(
            post("/api/v1/instructor/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // --------------------------------------------------
    // UPDATE
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should update own course`() {

        val instructor = seedInstructor()
        val courseId = seedCourse(instructor)

        val request = mapOf(
            "title" to "Updated Title",
            "description" to "Updated",
            "capacity" to 50
        )

        mockMvc.perform(
            put("/api/v1/instructor/courses/$courseId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Updated Title"))
    }

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `should return 404 when updating non existing course`() {

        seedInstructor()

        val request = mapOf(
            "title" to "X",
            "description" to "Y",
            "capacity" to 10
        )

        mockMvc.perform(
            put("/api/v1/instructor/courses/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should delete course`() {

        val instructor = seedInstructor()
        val courseId = seedCourse(instructor)

        mockMvc.perform(delete("/api/v1/instructor/courses/$courseId"))
            .andExpect(status().isNoContent)

        assert(courseRepository.count() == 0L)
    }

    // --------------------------------------------------
    // GET OWN COURSES
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should fetch own courses`() {

        val instructor = seedInstructor()
        seedCourse(instructor)

        mockMvc.perform(get("/api/v1/instructor/courses"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }
}