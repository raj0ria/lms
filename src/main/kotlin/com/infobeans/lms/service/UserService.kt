package com.infobeans.lms.service

import com.infobeans.lms.persistence.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
}