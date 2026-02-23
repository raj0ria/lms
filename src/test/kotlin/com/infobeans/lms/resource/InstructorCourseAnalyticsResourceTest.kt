package com.infobeans.lms.resource

import com.infobeans.lms.dto.InstructorCourseStudentResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.service.impl.InstructorCourseAnalyticsService
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class InstructorCourseAnalyticsResourceTest {

    @MockK
    lateinit var analyticsService: InstructorCourseAnalyticsService

    @InjectMockKs
    lateinit var resource: InstructorCourseAnalyticsResource

    @Test
    fun `should return course students`() {
        val pageable: Pageable = PageRequest.of(0, 10)

        val student = InstructorCourseStudentResponse(
            studentId = 1L,
            studentName = "John",
            studentEmail = "john@mail.com",
            enrolledAt = Instant.now(),
            totalModules = 5,
            completedModules = 3,
            progressPercentage = 60
        )

        val paged = PagedResponse(
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1,
            content = listOf(student)
        )

        every { analyticsService.getCourseStudents(101L, pageable) } returns paged

        val response: ResponseEntity<PagedResponse<InstructorCourseStudentResponse>> =
            resource.getCourseStudents(101L, pageable)

        assertEquals(200, response.statusCode.value())
        assertEquals(1, response.body!!.totalElements)
    }
}