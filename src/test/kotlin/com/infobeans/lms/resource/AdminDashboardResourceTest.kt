package com.infobeans.lms.resource

import com.infobeans.lms.dto.AdminDashboardSummaryResponse
import com.infobeans.lms.service.impl.AdminDashboardService
import io.mockk.every
import io.mockk.verify
import io.mockk.confirmVerified
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class AdminDashboardResourceTest {

    @MockK
    lateinit var adminDashboardService: AdminDashboardService

    @InjectMockKs
    lateinit var adminDashboardResource: AdminDashboardResource

    @Test
    fun `should return dashboard summary successfully`() {

        // Given
        val summaryResponse = AdminDashboardSummaryResponse(
            totalStudents = 100,
            totalInstructors = 10,
            totalCourses = 25,
            totalEnrollments = 350
        )

        every {
            adminDashboardService.getDashboardSummary()
        } returns summaryResponse

        // When
        val response = adminDashboardResource.getDashboardSummary()

        // Then
        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)

        assertEquals(100, response.body!!.totalStudents)
        assertEquals(10, response.body!!.totalInstructors)
        assertEquals(25, response.body!!.totalCourses)
        assertEquals(350, response.body!!.totalEnrollments)

        verify(exactly = 1) {
            adminDashboardService.getDashboardSummary()
        }

        confirmVerified(adminDashboardService)
    }
}