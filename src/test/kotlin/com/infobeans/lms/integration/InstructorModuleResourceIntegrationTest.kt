package com.infobeans.lms.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.Module
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.ModuleRepository
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
class InstructorModuleResourceIntegrationTest : IntegrationTestBase() {

    @Autowired override lateinit var mockMvc: MockMvc
    @Autowired override lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var courseRepository: CourseRepository
    @Autowired lateinit var moduleRepository: ModuleRepository

    @BeforeEach
    fun cleanDb() {
        moduleRepository.deleteAll()
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

    private fun seedModule(courseId: Long): Long =
        moduleRepository.save(
            Module(
                name = "Intro",
                materialUrl = "url"
            ).apply {
                this.course = courseRepository.findById(courseId).get()
            }
        ).id

    // --------------------------------------------------
    // CREATE MODULE
    // --------------------------------------------------


    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `student should not create module`() {

        mockMvc.perform(
            post("/api/v1/instructor/courses/1/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"X","materialUrl":"url"}""")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `should return 404 when course not found`() {

        seedInstructor()

        mockMvc.perform(
            post("/api/v1/instructor/courses/9999/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"X","materialUrl":"url"}""")
        )
            .andExpect(status().isNotFound)
    }

    // --------------------------------------------------
    // UPDATE MODULE
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `should return 404 when module not found`() {

        seedInstructor()

        mockMvc.perform(
            put("/api/v1/instructor/modules/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"X"}""")
        )
            .andExpect(status().isNotFound)
    }

    // --------------------------------------------------
    // DELETE MODULE
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should delete module`() {

        val instructor = seedInstructor()
        val courseId = seedCourse(instructor)
        val moduleId = seedModule(courseId)

        mockMvc.perform(delete("/api/v1/instructor/modules/$moduleId"))
            .andExpect(status().isNoContent)

        assert(moduleRepository.count() == 0L)
    }

    // --------------------------------------------------
    // GET MODULES
    // --------------------------------------------------

    @Test
    @WithMockUser(username = "inst@test.com", roles = ["INSTRUCTOR"])
    fun `instructor should fetch modules for course`() {

        val instructor = seedInstructor()
        val courseId = seedCourse(instructor)
        seedModule(courseId)

        mockMvc.perform(get("/api/v1/instructor/courses/$courseId/modules"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `anonymous should not access module endpoints`() {

        mockMvc.perform(get("/api/v1/instructor/courses/1/modules"))
            .andExpect(status().isForbidden)
    }
}