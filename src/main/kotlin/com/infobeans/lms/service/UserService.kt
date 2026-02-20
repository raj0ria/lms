package com.infobeans.lms.service

import com.infobeans.lms.persistence.UserRepository
import org.springframework.stereotype.Service

/**
 * Service for user-related business operations.
 * Acts as an abstraction layer over UserRepository.
 */
@Service
class UserService(
    private val userRepository: UserRepository
) {
}