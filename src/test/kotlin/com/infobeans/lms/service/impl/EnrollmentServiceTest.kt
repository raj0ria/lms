package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.EnrollmentResponse
import org.junit.jupiter.api.Assertions.assertNotNull
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.BusinessRuleViolationException
import com.infobeans.lms.model.*
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.EnrollmentRepository
import com.infobeans.lms.persistence.UserRepository
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

@ExtendWith(MockKExtension::class)
class EnrollmentServiceTest {

    @MockK lateinit var enrollmentRepository: EnrollmentRepository
    @MockK lateinit var courseRepository: CourseRepository
    @MockK lateinit var userRepository: UserRepository
    @MockK lateinit var meterRegistry: MeterRegistry
    @MockK(relaxed = true) lateinit var enrollmentCounter: Counter

    @InjectMockKs
    lateinit var enrollmentService: EnrollmentService

    @BeforeEach
    fun setup() {
        // Mock Micrometer
        every { meterRegistry.counter("enrollment_count") } returns enrollmentCounter

        // Mock Security Context
        mockkStatic(SecurityContextHolder::class)
        val securityContext = mockk<SecurityContext>()
        val authentication = mockk<Authentication>()
        every { SecurityContextHolder.getContext() } returns securityContext
        every { securityContext.authentication } returns authentication
        every { authentication.name } returns "student@infobeans.com"
    }

    @AfterEach
    fun tearDown() = unmockkStatic(SecurityContextHolder::class)

    @Test
    fun `enrollInCourse should succeed and initialize module statuses`() {
        // GIVEN
        val student = mockk<User>(relaxed = true) {
            every { id } returns 1L
            every { role } returns Role.STUDENT
        }

        // Setup modules to test the loop logic in your service
        val mockModule = mockk<Module>(relaxed = true)
        val course = mockk<Course>(relaxed = true) {
            every { id } returns 101L
            every { published } returns true
            every { capacity } returns 10
            every { modules } returns mutableListOf(mockModule)
        }

        every { userRepository.findByEmail(any()) } returns student
        every { courseRepository.findById(101L) } returns Optional.of(course)
        every { enrollmentRepository.existsByUserIdAndCourseId(1L, 101L) } returns false
        every { enrollmentRepository.countByCourseId(101L) } returns 5L

        // Mock the enrollment return
        val savedEnrollment = mockk<Enrollment>(relaxed = true) {
            every { id } returns 999L
            every { course.id } returns 101L
            every { course.title } returns "Test Course"
        }
        every { enrollmentRepository.save(any()) } returns savedEnrollment

        // WHEN
        val result = enrollmentService.enrollInCourse(101L)

        // THEN
        assertNotNull(result)
        assertEquals(999L, result.id)

        // Verify that internal logic like module status initialization was attempted
        verify { enrollmentRepository.save(match {
            it.moduleStatuses.size == 1
        }) }
    }

    @Test
    fun `enrollInCourse should throw exception when course is unpublished`() {
        // GIVEN
        val student = mockk<User>(relaxed = true) { every { role } returns Role.STUDENT }
        val course = mockk<Course>(relaxed = true) {
            every { published } returns false
        }

        every { userRepository.findByEmail(any()) } returns student
        every { courseRepository.findById(any()) } returns Optional.of(course)

        // WHEN & THEN
        val exception = assertThrows<BusinessRuleViolationException> {
            enrollmentService.enrollInCourse(101L)
        }
        assertEquals("Course is not published", exception.message)
    }
}