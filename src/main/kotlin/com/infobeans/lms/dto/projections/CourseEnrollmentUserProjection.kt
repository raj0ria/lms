package com.infobeans.lms.dto.projections

import com.infobeans.lms.enums.Role
import java.time.Instant

interface CourseEnrollmentUserProjection {
    val userId: Long
    val name: String
    val email: String
    val role: Role
    val enrolledAt: Instant
}
