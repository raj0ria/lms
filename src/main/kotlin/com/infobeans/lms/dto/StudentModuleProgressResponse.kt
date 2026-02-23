package com.infobeans.lms.dto

import com.infobeans.lms.enums.EnrollmentStatus

data class StudentModuleProgressResponse(
    val moduleId: Long,
    val name: String,
    val materialUrl: String?,
    val status: EnrollmentStatus
)