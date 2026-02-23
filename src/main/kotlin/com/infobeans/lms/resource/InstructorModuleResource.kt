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

import com.infobeans.lms.dto.CreateModuleRequest
import com.infobeans.lms.dto.ModuleResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateModuleRequest
import com.infobeans.lms.service.impl.InstructorModuleService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Instructor Module Controller.
 * Handles module management within instructor-owned courses.
 */
@Tag(
    name = "Instructor Module Management",
    description = "Manage modules within instructor-owned courses"
)
@RestController
@RequestMapping("/api/v1/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
@SecurityRequirement(name = "bearerAuth")
class InstructorModuleResource(
    private val moduleService: InstructorModuleService
) {

    private val log = LoggerFactory.getLogger(InstructorModuleResource::class.java)

    /**
     * Create a module under a course.
     */
    @Operation(summary = "Create module under course")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Module created successfully",
                content = [Content(schema = Schema(implementation = ModuleResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Validation or business rule violation",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Course not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PostMapping("/courses/{courseId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createModule(
        @Parameter(description = "Course ID", example = "101")
        @PathVariable courseId: Long,
        @Valid @RequestBody request: CreateModuleRequest
    ): ModuleResponse {

        log.info("POST module for course {}", courseId)

        return moduleService.createModule(courseId, request)
    }

    /**
     * Update existing module.
     */
    @Operation(summary = "Update module")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Module updated successfully",
                content = [Content(schema = Schema(implementation = ModuleResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Module not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PutMapping("/modules/{moduleId}")
    fun updateModule(
        @Parameter(description = "Module ID", example = "55")
        @PathVariable moduleId: Long,
        @RequestBody request: UpdateModuleRequest
    ): ModuleResponse {

        log.info("PUT module {}", moduleId)

        return moduleService.updateModule(moduleId, request)
    }

    /**
     * Delete a module.
     */
    @Operation(summary = "Delete module")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Module deleted successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Module not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @DeleteMapping("/modules/{moduleId}")
    fun deleteModule(
        @Parameter(description = "Module ID", example = "55")
        @PathVariable moduleId: Long
    ): ResponseEntity<Void> {

        log.info("DELETE module {}", moduleId)

        moduleService.deleteModule(moduleId)

        return ResponseEntity.noContent().build()
    }

    /**
     * Get paginated modules for a course.
     */
    @Operation(summary = "Get modules for course")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Modules fetched successfully",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Course not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @GetMapping("/courses/{courseId}/modules")
    fun getModules(
        @Parameter(description = "Course ID", example = "101")
        @PathVariable courseId: Long,
        @ParameterObject
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): ResponseEntity<PagedResponse<ModuleResponse>> {

        log.info("GET modules for course {}", courseId)

        return ResponseEntity.ok(
            moduleService.getModulesByCourse(courseId, pageable)
        )
    }
}
