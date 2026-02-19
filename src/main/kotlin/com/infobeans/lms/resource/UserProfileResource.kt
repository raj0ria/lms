package com.infobeans.lms.resource

import com.infobeans.lms.dto.ChangePasswordRequest
import com.infobeans.lms.dto.UpdateProfileRequest
import com.infobeans.lms.dto.UserProfileResponse
import com.infobeans.lms.service.UserProfileService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource exposing authenticated user profile APIs.
 */
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("isAuthenticated()")
class UserProfileResource(
    private val userProfileService: UserProfileService
) {

    private val log = LoggerFactory.getLogger(UserProfileResource::class.java)

    /**
     * Returns current user's profile.
     */
    @GetMapping("/me")
    fun getProfile(): ResponseEntity<UserProfileResponse> {

        log.info("GET /api/v1/users/me invoked")

        return ResponseEntity.ok(userProfileService.getCurrentUserProfile())
    }

    /**
     * Updates current user's profile.
     */
    @PutMapping("/me")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserProfileResponse> {

        log.info("PUT /api/v1/users/me invoked")

        return ResponseEntity.ok(userProfileService.updateProfile(request))
    }

    /**
     * Changes current user's password.
     */
    @PatchMapping("/me/password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Void> {

        log.info("PATCH /api/v1/users/me/password invoked")

        userProfileService.changePassword(request)

        return ResponseEntity.noContent().build()
    }
}
