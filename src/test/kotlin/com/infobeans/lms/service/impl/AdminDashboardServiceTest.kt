package com.infobeans.lms.service.impl
import com.infobeans.lms.dto.AdminDashboardSummaryResponse
import com.infobeans.lms.dto.projections.AdminDashboardSummaryProjection
import com.infobeans.lms.persistence.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AdminDashboardServiceTest {

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var adminDashboardService: AdminDashboardService

    @Test
    fun `getDashboardSummary should return mapped counts from repository projection`() {
        // GIVEN: Define the expected counts
        val expectedStudents = 150L
        val expectedInstructors = 10L
        val expectedCourses = 25L
        val expectedEnrollments = 300L

        // Mock the Projection interface found in the DTO file
        val mockProjection = mockk<AdminDashboardSummaryProjection> {
            every { totalStudents } returns expectedStudents
            every { totalInstructors } returns expectedInstructors
            every { totalCourses } returns expectedCourses
            every { totalEnrollments } returns expectedEnrollments
        }

        // Define repository behavior
        every { userRepository.fetchDashboardSummary() } returns mockProjection

        // WHEN: Calling the service method
        val result: AdminDashboardSummaryResponse = adminDashboardService.getDashboardSummary()

        // THEN: Verify the DTO mapping
        assertEquals(expectedStudents, result.totalStudents)
        assertEquals(expectedInstructors, result.totalInstructors)
        assertEquals(expectedCourses, result.totalCourses)
        assertEquals(expectedEnrollments, result.totalEnrollments)

        // Verify the repository was called exactly once
        verify(exactly = 1) { userRepository.fetchDashboardSummary() }
    }
}