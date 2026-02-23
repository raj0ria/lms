package com.infobeans.lms.resource

import com.infobeans.lms.dto.*
import com.infobeans.lms.enums.Role
import com.infobeans.lms.service.impl.CourseDiscoveryService
import io.mockk.*
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class CourseDiscoveryResourceTest {

    @MockK
    lateinit var discoveryService: CourseDiscoveryService

    @InjectMockKs
    lateinit var resource: CourseDiscoveryResource

    // =========================================================
    // SEARCH COURSES
    // =========================================================

    @Test
    fun `should return paged published courses`() {

        val pageable: Pageable = PageRequest.of(0, 10)

        val course = CourseSearchResponse(
            courseId = 1L,
            title = "Spring Boot Masterclass",
            description = "Complete Spring Boot Guide",
            instructorName = "John Doe",
            capacity = 100,
            enrolledCount = 50,
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
            discoveryService.searchCourses(null, pageable)
        } returns pagedResponse

        val response = resource.searchCourses(null, pageable)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(1, response.body!!.totalElements)
        assertEquals("Spring Boot Masterclass", response.body!!.content[0].title)

        verify(exactly = 1) {
            discoveryService.searchCourses(null, pageable)
        }

        confirmVerified(discoveryService)
    }

    @Test
    fun `should search courses with keyword`() {

        val pageable: Pageable = PageRequest.of(0, 10)

        every {
            discoveryService.searchCourses("spring", pageable)
        } returns PagedResponse(
            page = 0,
            size = 10,
            totalElements = 0,
            totalPages = 0,
            content = emptyList()
        )

        val response = resource.searchCourses("spring", pageable)

        assertEquals(200, response.statusCode.value())
        assertEquals(0, response.body!!.totalElements)

        verify {
            discoveryService.searchCourses("spring", pageable)
        }
    }

    // =========================================================
    // GET COURSE DETAIL
    // =========================================================

    @Test
    fun `should return full course detail`() {

        val detail = CourseDetailResponse(
            id = 1L,
            title = "Kotlin Advanced",
            description = "Deep dive into Kotlin",
            capacity = 50,
            published = true,
            instructor = InstructorInfo(
                id = 10L,
                name = "Jane Doe",
                email = "jane@mail.com"
            ),
            modules = listOf(
                ModuleInfo(
                    id = 100L,
                    name = "Introduction",
                    materialUrl = "http://url",
                    createdAt = Instant.now()
                )
            ),
            enrollments = emptyList(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every {
            discoveryService.getCourseDetail(1L)
        } returns detail

        val response = resource.getCourseDetail(1L)

        assertEquals(200, response.statusCode.value())
        assertEquals("Kotlin Advanced", response.body!!.title)
        assertEquals("Jane Doe", response.body!!.instructor.name)

        verify(exactly = 1) {
            discoveryService.getCourseDetail(1L)
        }

        confirmVerified(discoveryService)
    }

    @Test
    fun `should propagate exception when course not found`() {

        every {
            discoveryService.getCourseDetail(999L)
        } throws RuntimeException("Course not found")

        assertFailsWith<RuntimeException> {
            resource.getCourseDetail(999L)
        }

        verify {
            discoveryService.getCourseDetail(999L)
        }
    }
}