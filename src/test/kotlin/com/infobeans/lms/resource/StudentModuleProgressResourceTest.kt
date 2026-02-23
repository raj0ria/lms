package com.infobeans.lms.resource

import com.infobeans.lms.dto.UpdateModuleProgressRequest
import com.infobeans.lms.enums.EnrollmentStatus
import com.infobeans.lms.service.impl.ModuleProgressService
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
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class StudentModuleProgressResourceTest {

    @MockK
    lateinit var moduleProgressService: ModuleProgressService

    @InjectMockKs
    lateinit var resource: StudentModuleProgressResource

    @Test
    fun `should update module progress`() {
        val request = UpdateModuleProgressRequest(
            status = EnrollmentStatus.COMPLETED
        )

        every { moduleProgressService.updateProgress(1L, request) } just Runs

        val response: ResponseEntity<Void> =
            resource.updateModuleProgress(1L, request)

        assertEquals(204, response.statusCode.value())
        verify { moduleProgressService.updateProgress(1L, request) }
    }

}