package com.infobeans.lms.resource

<<<<<<< HEAD
import com.infobeans.lms.dto.StudentModuleProgressResponse
=======
import com.infobeans.lms.dto.ApiError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

>>>>>>> 9d84a80e150262bc22d25acd1c4c33d5ad70433d
import com.infobeans.lms.dto.UpdateModuleProgressRequest
import com.infobeans.lms.persistence.UserRepository
import com.infobeans.lms.service.impl.ModuleProgressService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Controller for student module progress updates.
 */
@Tag(
    name = "Student Module Progress",
    description = "Endpoints for students to update module completion status"
)
@RestController
@RequestMapping("/api/v1/modules")
@SecurityRequirement(name = "bearerAuth")
class StudentModuleProgressResource(
    private val moduleProgressService: ModuleProgressService
) {
    /**
     * Update progress status of a module for logged-in student.
     */
    @Operation(
        summary = "Update module progress",
        description = "Updates completion/progress status of a module for the logged-in STUDENT."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Module progress updated successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid progress update request",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied (Only STUDENT allowed)",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Module or enrollment not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PreAuthorize("hasRole('STUDENT')")
    @PatchMapping("/{moduleId}/progress")
    fun updateModuleProgress(
        @Parameter(description = "Module ID", example = "55")
        @PathVariable moduleId: Long,
        @RequestBody request: UpdateModuleProgressRequest
    ): ResponseEntity<Void> {

        moduleProgressService.updateProgress(moduleId, request)

        return ResponseEntity.noContent().build()
    }

}