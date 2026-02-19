package com.infobeans.lms.dto

import com.infobeans.lms.enums.EnrollmentStatus

data class UpdateModuleProgressRequest(
    val status: EnrollmentStatus
)

