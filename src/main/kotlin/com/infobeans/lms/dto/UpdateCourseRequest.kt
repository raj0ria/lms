package com.infobeans.lms.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class UpdateCourseRequest(

    val title: String,

    val description: String?,

    @field:Positive
    val capacity: Int
)
