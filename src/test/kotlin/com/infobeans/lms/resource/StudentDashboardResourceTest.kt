package com.infobeans.lms.resource

import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.StudentCourseDashboardResponse
import com.infobeans.lms.service.impl.StudentDashboardService
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
class StudentDashboardResourceTest {

    @MockK
    lateinit var service: StudentDashboardService

    @InjectMockKs
    lateinit var resource: StudentDashboardResource

    @Test
    fun `should return enrolled courses`() {
        val pageable: Pageable = PageRequest.of(0, 10)

        val dto = StudentCourseDashboardResponse(
            courseId = 1L,
            courseTitle = "Spring",
            enrolledAt = Instant.now(),
            totalModules = 5,
            completedModules = 3,
            progressPercentage = 60
        )

        val paged = PagedResponse(0, 10, 1, 1, listOf(dto))

        every { service.getMyCourses(pageable) } returns paged

        val response: ResponseEntity<PagedResponse<StudentCourseDashboardResponse>> =
            resource.getMyCourses(pageable)

        assertEquals(200, response.statusCode.value())
        assertEquals(1, response.body!!.totalElements)
    }
}