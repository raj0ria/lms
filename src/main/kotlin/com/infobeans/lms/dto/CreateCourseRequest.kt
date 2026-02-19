package com.infobeans.lms.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

/**
 * /**
 *  * Only instructor can create a course
 *  */
 */
data class CreateCourseRequest(
    @field:NotBlank
    val title: String,

    val description: String?,

    @field:Positive
    val capacity: Int
)
