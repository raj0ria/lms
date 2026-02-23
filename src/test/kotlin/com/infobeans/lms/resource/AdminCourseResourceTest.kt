package com.infobeans.lms.resource

import com.infobeans.lms.dto.AdminCourseResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.service.impl.AdminCourseService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AdminCourseResourceTest {

    private lateinit var adminCourseService: AdminCourseService
    private lateinit var adminCourseResource: AdminCourseResource

    @BeforeEach
    fun setup() {
        adminCourseService = mockk()
        adminCourseResource = AdminCourseResource(adminCourseService)
    }

    @Test
    fun `should return paged courses successfully`() {

        // Given
        val pageable: Pageable = PageRequest.of(0, 10)

        val course = AdminCourseResponse(
            id = 1L,
            title = "Kotlin Basics",
            description = "Learn Kotlin",
            published = true,
            capacity = 100,
            instructorName = "John Doe",
            createdAt = Instant.now()
        )

        val pagedResponse = PagedResponse(
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1,
            content = listOf(course)
        )

        every {
            adminCourseService.getAllCourses(null, pageable)
        } returns pagedResponse

        // When
        val response = adminCourseResource.getAllCourses(null, pageable)

        // Then
        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(1, response.body!!.totalElements)
        assertEquals("Kotlin Basics", response.body!!.content[0].title)

        verify(exactly = 1) {
            adminCourseService.getAllCourses(null, pageable)
        }

        confirmVerified(adminCourseService)
    }

    @Test
    fun `should delete course successfully`() {

        // Given
        val courseId = 1L

        every {
            adminCourseService.deleteCourse(courseId)
        } just Runs

        // When
        val response = adminCourseResource.deleteCourse(courseId)

        // Then
        assertEquals(204, response.statusCode.value())

        verify(exactly = 1) {
            adminCourseService.deleteCourse(courseId)
        }

        confirmVerified(adminCourseService)
    }
}