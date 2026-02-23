package com.infobeans.lms.resource

import com.infobeans.lms.dto.CreateModuleRequest
import com.infobeans.lms.dto.ModuleResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateModuleRequest
import com.infobeans.lms.service.impl.InstructorModuleService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class InstructorModuleResourceTest {

    @MockK
    lateinit var moduleService: InstructorModuleService

    @InjectMockKs
    lateinit var resource: InstructorModuleResource

    @Test
    fun `should create module`() {
        val request = CreateModuleRequest(
            name = "Introduction",
            materialUrl = "http://material"
        )

        val responseDto = ModuleResponse(
            id = 1L,
            name = "Introduction",
            materialUrl = "http://material",
            courseId = 101L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { moduleService.createModule(101L, request) } returns responseDto

        val result = resource.createModule(101L, request)

        assertEquals("Introduction", result.name)
        verify { moduleService.createModule(101L, request) }
    }

    @Test
    fun `should update module`() {
        val request = UpdateModuleRequest(
            name = "Updated",
            materialUrl = "http://updated"
        )

        val responseDto = ModuleResponse(
            id = 1L,
            name = "Updated",
            materialUrl = "http://updated",
            courseId = 101L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { moduleService.updateModule(1L, request) } returns responseDto

        val result = resource.updateModule(1L, request)

        assertEquals("Updated", result.name)
        verify { moduleService.updateModule(1L, request) }
    }

    @Test
    fun `should delete module`() {
        every { moduleService.deleteModule(1L) } just Runs

        val response: ResponseEntity<Void> = resource.deleteModule(1L)

        assertEquals(204, response.statusCode.value())
        verify { moduleService.deleteModule(1L) }
    }

    @Test
    fun `should return paginated modules`() {
        val pageable: Pageable = PageRequest.of(0, 10)

        val module = ModuleResponse(
            id = 1L,
            name = "Intro",
            materialUrl = "url",
            courseId = 101L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val paged = PagedResponse(
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1,
            content = listOf(module)
        )

        every { moduleService.getModulesByCourse(101L, pageable) } returns paged

        val response = resource.getModules(101L, pageable)

        assertEquals(200, response.statusCode.value())
        assertEquals(1, response.body!!.totalElements)
    }

    @Test
    fun `should propagate exception when module not found`() {
        every { moduleService.updateModule(any(), any()) } throws RuntimeException("Not found")

        assertThrows<RuntimeException> {
            resource.updateModule(99L, UpdateModuleRequest("x", "y"))
        }
    }
}