package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.*
import com.infobeans.lms.dto.projections.CourseDetailProjection
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant

@ExtendWith(MockKExtension::class)
class CourseDiscoveryServiceTest {

    @MockK lateinit var courseRepository: CourseRepository
    @MockK lateinit var moduleRepository: ModuleRepository
    @MockK lateinit var enrollmentRepository: EnrollmentRepository
    @MockK lateinit var userRepository: UserRepository

    @InjectMockKs lateinit var courseDiscoveryService: CourseDiscoveryService

    @BeforeEach
    fun setupSecurity() {
        mockkStatic(SecurityContextHolder::class)
    }

    @AfterEach
    fun clearSecurity() {
        unmockkStatic(SecurityContextHolder::class)
    }

    private fun mockAuthentication(email: String) {
        val securityContext = mockk<SecurityContext>()
        val authentication = mockk<Authentication>()
        every { SecurityContextHolder.getContext() } returns securityContext
        every { securityContext.authentication } returns authentication
        every { authentication.name } returns email
    }

    @Test
    fun `getCourseDetail should return full detail for Admin`() {
        // GIVEN
        val email = "admin@lms.com"
        val courseId = 1L
        mockAuthentication(email)

        val user = mockk<User> { every { role } returns Role.ADMIN }
        val courseDetail = mockk<CourseDetailProjection> {
            every { id } returns courseId
            every { title } returns "Admin Course"
            every { description } returns "Desc"
            every { capacity } returns 10
            every { published } returns true
            every { instructorId } returns 2L
            every { instructorName } returns "Prof"
            every { instructorEmail } returns "prof@lms.com"
            every { createdAt } returns Instant.now()
            every { updatedAt } returns Instant.now()
        }

        every { userRepository.findByEmail(email) } returns user
        every { courseRepository.findCourseDetail(courseId) } returns courseDetail
        every { moduleRepository.findModulesByCourseId(courseId) } returns emptyList()
        every { enrollmentRepository.findEnrolledUsersByCourseId(courseId) } returns emptyList()

        // WHEN
        val result = courseDiscoveryService.getCourseDetail(courseId)

        // THEN
        assertEquals("Admin Course", result.title)
        verify { courseRepository.findCourseDetail(courseId) }
    }

    @Test
    fun `getCourseDetail should throw AccessDenied when Instructor views another's course`() {
        // GIVEN
        val instructorEmail = "instructor@lms.com"
        val courseId = 1L
        mockAuthentication(instructorEmail)

        val instructorUser = mockk<User> {
            every { email } returns instructorEmail
            every { role } returns Role.INSTRUCTOR
        }

        // Explicitly mock the interface getter
        val foreignCourse = mockk<CourseDetailProjection>()
        every { foreignCourse.instructorEmail } returns "different@lms.com"

        every { userRepository.findByEmail(instructorEmail) } returns instructorUser
        every { courseRepository.findCourseDetail(courseId) } returns foreignCourse

        // WHEN & THEN
        assertThrows<AccessDeniedException> {
            courseDiscoveryService.getCourseDetail(courseId)
        }
    }
}