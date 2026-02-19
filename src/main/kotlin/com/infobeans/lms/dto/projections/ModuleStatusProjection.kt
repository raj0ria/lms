package com.infobeans.lms.dto.projections

import com.infobeans.lms.enums.EnrollmentStatus

interface ModuleStatusProjection {
    val id: Long
    val status: EnrollmentStatus
    val enrollmentUserId: Long
}