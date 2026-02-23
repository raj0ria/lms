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

import com.infobeans.lms.dto.CreateUserRequest
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateUserRequest
import com.infobeans.lms.dto.UserAdminResponse
import com.infobeans.lms.service.impl.UserAdminService
import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource for administrative user management.
 */
@Tag(
    name = "Admin User Management",
    description = "Administrative endpoints for managing users"
)
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
class UserAdminResource(
    private val userAdminService: UserAdminService
) {

    private val log = LoggerFactory.getLogger(UserAdminResource::class.java)

    /**
     * Creates a new user.
     */
    @Operation(summary = "Create new user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = [Content(schema = Schema(implementation = UserAdminResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Validation or business rule violation",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Duplicate email or conflict",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PostMapping
    fun createUser(
        @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserAdminResponse> {

        log.info("POST /api/v1/users invoked")

        val response = userAdminService.createUser(request)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Returns paginated users.
     */
    @Operation(summary = "Get paginated users")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Users fetched successfully",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @GetMapping
    fun getUsers(
        @Parameter(description = "Search keyword (name or email)", example = "john")
        @RequestParam(required = false) keyword: String?,
        @ParameterObject
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<PagedResponse<UserAdminResponse>> {

        log.info("GET /api/v1/users invoked")

        val response = userAdminService.getUsers(keyword, pageable)

        return ResponseEntity.ok(response)
    }

    /**
     * Returns user by ID.
     */
    @Operation(summary = "Get user by ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User fetched successfully",
                content = [Content(schema = Schema(implementation = UserAdminResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getUserById(
        @Parameter(description = "User ID", example = "10")
        @PathVariable id: Long
    ): ResponseEntity<UserAdminResponse> {

        val response = userAdminService.getUserById(id)

        return ResponseEntity.ok(response)
    }

    /**
     * Updates user.
     */
    @Operation(summary = "Update user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User updated successfully",
                content = [Content(schema = Schema(implementation = UserAdminResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid update request",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PutMapping("/{id}")
    fun updateUser(
        @Parameter(description = "User ID", example = "10")
        @PathVariable id: Long,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserAdminResponse> {

        val response = userAdminService.updateUser(id, request)

        return ResponseEntity.ok(response)
    }

    /**
     * Deletes user.
     */
    @Operation(summary = "Delete user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "User deleted successfully"),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @DeleteMapping("/{id}")
    fun deleteUser(
        @Parameter(description = "User ID", example = "10")
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        userAdminService.deleteUser(id)

        return ResponseEntity.noContent().build()
    }
}
