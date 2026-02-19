package com.infobeans.lms.dto

import com.infobeans.lms.enums.Role

data class AuthRequest(
    val email: String,
    val password: String
)
