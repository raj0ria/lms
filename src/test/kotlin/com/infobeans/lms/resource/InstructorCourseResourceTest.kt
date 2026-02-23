package com.infobeans.lms.resource

import com.infobeans.lms.dto.CourseResponse
import com.infobeans.lms.dto.CreateCourseRequest
import com.infobeans.lms.service.impl.InstructorCourseService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class InstructorCourseResourceTest {

    @MockK
    lateinit var courseService: InstructorCourseService

    @InjectMockKs
    lateinit var resource: InstructorCourseResource

    @Test
    fun `should create course`() {
        val request = CreateCourseRequest("Kotlin", "Desc", 50)

        val dto = CourseResponse(
            id = 1L,
            title = "Kotlin",
            description = "Desc",
            published = false,
            capacity = 50,
            instructorName = "John",
            instructorEmail = "john@mail.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { courseService.createCourse(request) } returns dto

        val response = resource.createCourse(request)

        assertEquals("Kotlin", response.title)
    }

    @Test
    fun `should delete course`() {
        every { courseService.deleteCourse(1L) } just Runs

        val response: ResponseEntity<Void> = resource.deleteCourse(1L)

        assertEquals(204, response.statusCode.value())
    }
}