package com.infobeans.lms.service.impl
import com.infobeans.lms.dto.AdminCourseResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.projections.AdminCourseProjection
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.Course
import com.infobeans.lms.persistence.CourseRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class AdminCourseServiceTest {

    @MockK
    lateinit var courseRepository: CourseRepository

    @InjectMockKs
    lateinit var adminCourseService: AdminCourseService

    @Test
    fun `getAllCourses should return paginated AdminCourseResponse`() {
        // GIVEN: Setup mock data and pageable
        val pageable: Pageable = PageRequest.of(0, 10)
        val keyword = "Kotlin"
        val now = Instant.now()

        // Create a mock of the projection interface
        val mockProjection = mockk<AdminCourseProjection> {
            every { id } returns 1L
            every { title } returns "Advanced Kotlin"
            every { description } returns "Mastering MockK"
            every { published } returns true
            every { capacity } returns 50
            every { instructorName } returns "John Doe"
            every { createdAt } returns now
        }

        val page = PageImpl(listOf(mockProjection), pageable, 1L)

        // Define repository behavior
        every { courseRepository.findAllAdminCourses(keyword, pageable) } returns page

        // WHEN: Execute the service call
        val result: PagedResponse<AdminCourseResponse> = adminCourseService.getAllCourses(keyword, pageable)

        // THEN: Verify results and mapping logic
        assertNotNull(result)
        assertEquals(1, result.totalElements)
        assertEquals("Advanced Kotlin", result.content[0].title)

        verify(exactly = 1) { courseRepository.findAllAdminCourses(keyword, pageable) }
    }

    @Test
    fun `deleteCourse should succeed when course exists`() {
        // GIVEN: A course exists in the DB
        val courseId = 100L
        val mockCourse = mockk<Course>() // Mock the actual entity

        every { courseRepository.findById(courseId) } returns Optional.of(mockCourse)
        every { courseRepository.delete(mockCourse) } returns Unit

        // WHEN: Calling delete
        adminCourseService.deleteCourse(courseId)

        // THEN: Verify repository interactions
        verify(exactly = 1) { courseRepository.findById(courseId) }
        verify(exactly = 1) { courseRepository.delete(mockCourse) }
    }

    @Test
    fun `deleteCourse should throw ResourceNotFoundException when course does not exist`() {
        // GIVEN: Repository returns empty Optional
        val courseId = 999L
        every { courseRepository.findById(courseId) } returns Optional.empty()

        // WHEN & THEN: Assert exception is thrown [cite: 3]
        val exception = assertThrows<ResourceNotFoundException> {
            adminCourseService.deleteCourse(courseId)
        }

        assertEquals("Course not found", exception.message)
        verify(exactly = 1) { courseRepository.findById(courseId) }
        verify(exactly = 0) { courseRepository.delete(any()) } // Ensure delete was never called
    }
}