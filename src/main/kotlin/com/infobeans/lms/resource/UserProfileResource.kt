package com.infobeans.lms.resource

import com.infobeans.lms.dto.ApiError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

import com.infobeans.lms.dto.ChangePasswordRequest
import com.infobeans.lms.dto.UpdateProfileRequest
import com.infobeans.lms.dto.UserProfileResponse
import com.infobeans.lms.service.impl.UserProfileService
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
 * Profile Management
 * User Self APIs
 * REST resource exposing authenticated user profile APIs.
 */
@Tag(
    name = "User Profile",
    description = "Self-service profile management for authenticated users"
)
@RestController
@RequestMapping("/api/v1/users/me")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
class UserProfileResource(
    private val userProfileService: UserProfileService
) {

    private val log = LoggerFactory.getLogger(UserProfileResource::class.java)

    /**
     * Returns current user's profile.
     */
    @Operation(
        summary = "Get current user profile",
        description = "Returns profile details of the authenticated user."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profile fetched successfully",
                content = [Content(schema = Schema(implementation = UserProfileResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @GetMapping
    fun getProfile(): ResponseEntity<UserProfileResponse> {

        log.info("GET /api/v1/users/me invoked")

        return ResponseEntity.ok(userProfileService.getCurrentUserProfile())
    }

    /**
     * Updates current user's profile.
     */
    @Operation(
        summary = "Update profile",
        description = "Updates profile information of the authenticated user."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profile updated successfully",
                content = [Content(schema = Schema(implementation = UserProfileResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Validation or business rule violation",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PutMapping
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserProfileResponse> {

        log.info("PUT /api/v1/users/me invoked")

        return ResponseEntity.ok(userProfileService.updateProfile(request))
    }

    /**
     * Changes current user's password.
     */
    @Operation(
        summary = "Change password",
        description = "Changes password of the authenticated user. Requires current password validation."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Password changed successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid password change request",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Current password incorrect",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PatchMapping("/password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Void> {

        log.info("PATCH /api/v1/users/me/password invoked")

        userProfileService.changePassword(request)

        return ResponseEntity.noContent().build()
    }
}
