package com.infobeans.lms.dto

import jakarta.validation.constraints.NotBlank

data class CreateModuleRequest(
    @field:NotBlank(message = "Module name is required")
    val name: String,
    @field:NotBlank(message = "Module url is required")
    val materialUrl: String
)
