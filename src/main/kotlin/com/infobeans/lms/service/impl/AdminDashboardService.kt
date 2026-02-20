package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.AdminDashboardSummaryResponse
import com.infobeans.lms.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for admin dashboard analytics.
 */
@Service
class AdminDashboardService(
    private val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(AdminDashboardService::class.java)

    /**
     * Returns aggregated dashboard counts.
     */
    @Transactional(readOnly = true)
    fun getDashboardSummary(): AdminDashboardSummaryResponse {

        log.info("Fetching admin dashboard summary")

        val summary = userRepository.fetchDashboardSummary()

        log.info(
            "Dashboard summary: students={}, instructors={}, courses={}, enrollments={}",
            summary.totalStudents,
            summary.totalInstructors,
            summary.totalCourses,
            summary.totalEnrollments
        )

        return AdminDashboardSummaryResponse(
            totalStudents = summary.totalStudents,
            totalInstructors = summary.totalInstructors,
            totalCourses = summary.totalCourses,
            totalEnrollments = summary.totalEnrollments
        )
    }
}
