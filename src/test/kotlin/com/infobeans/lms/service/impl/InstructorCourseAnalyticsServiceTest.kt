package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.InstructorCourseStudentResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.projections.InstructorCourseStudentProjection
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.EnrollmentRepository
import com.infobeans.lms.persistence.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class InstructorCourseAnalyticsServiceTest {

    @MockK
    lateinit var enrollmentRepository: EnrollmentRepository

    @MockK
    lateinit var courseRepository: CourseRepository

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var analyticsService: InstructorCourseAnalyticsService

    private val instructorEmail = "teacher@lms.com"
    private val courseId = 100L

    @BeforeEach
    fun setup() {
        mockkStatic(SecurityContextHolder::class)
        val securityContext = mockk<SecurityContext>()
        val authentication = mockk<Authentication>()
        every { SecurityContextHolder.getContext() } returns securityContext
        every { securityContext.authentication } returns authentication
        every { authentication.name } returns instructorEmail
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SecurityContextHolder::class)
    }

    @Test
    fun `getCourseStudents should calculate progress correctly for authorized instructor`() {
        // GIVEN
        val pageable = PageRequest.of(0, 10)

        val instructorUser = mockk<User> {
            every { id } returns 10L
            every { role } returns Role.INSTRUCTOR
        }

        val mockCourse = mockk<Course>(relaxed = true) {
            every { id } returns courseId
            every { instructor.id } returns 10L
        }

        val mockProjection = mockk<InstructorCourseStudentProjection> {
            every { studentId } returns 1L
            every { studentName } returns "Jane Student"
            every { studentEmail } returns "jane@lms.com"
            every { enrolledAt } returns Instant.now()
            every { totalModules } returns 10L
            every { completedModules } returns 7L // Expect 70%
        }

        // Explicitly type the list to help the compiler infer type variable T
        val projectionsList: List<InstructorCourseStudentProjection> = listOf(mockProjection)
        val page = PageImpl(projectionsList, pageable, 1L)

        every { userRepository.findByEmail(instructorEmail) } returns instructorUser
        every { courseRepository.findById(courseId) } returns Optional.of(mockCourse)
        every { enrollmentRepository.findInstructorCourseStudents(courseId, pageable) } returns page

        // WHEN
        val result: PagedResponse<InstructorCourseStudentResponse> = analyticsService.getCourseStudents(courseId, pageable)

        // THEN
        // Using fully qualified name to resolve overload ambiguity between JUnit and Kotlin Test
        org.junit.jupiter.api.Assertions.assertNotNull(result)
        assertEquals(70, result.content[0].progressPercentage)
        verify { enrollmentRepository.findInstructorCourseStudents(courseId, pageable) }
    }

    @Test
    fun `getCourseStudents should throw AccessDenied if instructor does not own the course`() {
        // GIVEN
        val instructorUser = mockk<User> {
            every { id } returns 10L
            every { role } returns Role.INSTRUCTOR
        }
        val foreignCourse = mockk<Course>(relaxed = true) {
            every { instructor.id } returns 999L // Mismatched ownership
        }

        every { userRepository.findByEmail(instructorEmail) } returns instructorUser
        every { courseRepository.findById(courseId) } returns Optional.of(foreignCourse)

        // WHEN & THEN
        assertThrows<AccessDeniedException> {
            analyticsService.getCourseStudents(courseId, PageRequest.of(0, 10))
        }
    }
}