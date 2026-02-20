package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.ChangePasswordRequest
import com.infobeans.lms.dto.UpdateProfileRequest
import com.infobeans.lms.dto.UserProfileResponse
import com.infobeans.lms.exceptions.BusinessRuleViolationException
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for authenticated user profile operations.
 */
@Service
class UserProfileService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val log = LoggerFactory.getLogger(UserProfileService::class.java)

    /**
     * Returns currently authenticated user's profile.
     */
    @Transactional(readOnly = true)
    fun getCurrentUserProfile(): UserProfileResponse {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("Fetching profile for user={}", email)

        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        return user.toResponse()
    }

    /**
     * Updates authenticated user's profile.
     */
    @Transactional
    fun updateProfile(request: UpdateProfileRequest): UserProfileResponse {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("User {} attempting to update profile", email)

        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        user.name = request.name.trim()

        log.info("User {} profile updated successfully", email)

        return user.toResponse()
    }

    /**
     * Changes password for authenticated user.
     */
    @Transactional
    fun changePassword(request: ChangePasswordRequest) {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("User {} attempting to change password", email)

        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            log.warn("Invalid current password attempt by user={}", email)
            throw BusinessRuleViolationException("Current password is incorrect")
        }

        if (passwordEncoder.matches(request.newPassword, user.password)) {
            throw BusinessRuleViolationException("New password must be different")
        }

        user.password = passwordEncoder.encode(request.newPassword)

        log.info("User {} changed password successfully", email)
    }

    private fun User.toResponse(): UserProfileResponse =
        UserProfileResponse(
            id = this.id,
            name = this.name,
            email = this.email,
            role = this.role,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
}
