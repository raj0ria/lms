package com.infobeans.lms.dto.projections

/**
 * Projection representing admin dashboard summary counts.
 */
interface AdminDashboardSummaryProjection {

    val totalStudents: Long
    val totalInstructors: Long
    val totalCourses: Long
    val totalEnrollments: Long
}
