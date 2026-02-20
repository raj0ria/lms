package com.infobeans.lms.resource

import com.infobeans.lms.dto.CreateModuleRequest
import com.infobeans.lms.dto.ModuleResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateModuleRequest
import com.infobeans.lms.service.impl.InstructorModuleService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Instructor Module Controller.
 * Handles module management within instructor-owned courses.
 */
@RestController
@RequestMapping("/api/v1/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
class InstructorModuleResource(
    private val moduleService: InstructorModuleService
) {

    private val log = LoggerFactory.getLogger(InstructorModuleResource::class.java)

    /**
     * Create a module under a course.
     */
    @PostMapping("/courses/{courseId}/modules")
    fun createModule(
        @PathVariable courseId: Long,
        @Valid @RequestBody request: CreateModuleRequest
    ): ModuleResponse {

        log.info("POST module for course {}", courseId)

        return moduleService.createModule(courseId, request)
    }

    /**
     * Update existing module.
     */
    @PutMapping("/modules/{moduleId}")
    fun updateModule(
        @PathVariable moduleId: Long,
        @RequestBody request: UpdateModuleRequest
    ): ModuleResponse {

        log.info("PUT module {}", moduleId)

        return moduleService.updateModule(moduleId, request)
    }

    /**
     * Delete a module.
     */
    @DeleteMapping("/modules/{moduleId}")
    fun deleteModule(
        @PathVariable moduleId: Long
    ): ResponseEntity<Void> {

        log.info("DELETE module {}", moduleId)

        moduleService.deleteModule(moduleId)

        return ResponseEntity.noContent().build()
    }

    /**
     * Get paginated modules for a course.
     */
    @GetMapping("/courses/{courseId}/modules")
    fun getModules(
        @PathVariable courseId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): ResponseEntity<PagedResponse<ModuleResponse>> {

        log.info("GET modules for course {}", courseId)

        return ResponseEntity.ok(
            moduleService.getModulesByCourse(courseId, pageable)
        )
    }
}
