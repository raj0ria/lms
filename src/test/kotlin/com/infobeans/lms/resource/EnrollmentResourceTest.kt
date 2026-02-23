package com.infobeans.lms.resource

import com.infobeans.lms.dto.EnrollmentResponse
import com.infobeans.lms.service.impl.EnrollmentService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class EnrollmentResourceTest {

    @MockK
    lateinit var enrollmentService: EnrollmentService

    @InjectMockKs
    lateinit var resource: EnrollmentResource

    @Test
    fun `should enroll successfully`() {
        val dto = EnrollmentResponse(
            id = 1L,
            courseId = 101L,
            courseTitle = "Spring Boot",
            enrolledAt = Instant.now()
        )

        every { enrollmentService.enrollInCourse(101L) } returns dto

        val response: ResponseEntity<EnrollmentResponse> =
            resource.enrollInCourse(101L)

        assertEquals(201, response.statusCode.value())
        assertEquals(101L, response.body!!.courseId)

        verify { enrollmentService.enrollInCourse(101L) }
    }

    @Test
    fun `should unenroll successfully`() {
        every { enrollmentService.unenrollFromCourse(101L) } just Runs

        val response = resource.unenrollFromCourse(101L)

        assertEquals(204, response.statusCode.value())
        verify { enrollmentService.unenrollFromCourse(101L) }
    }
}