package com.infobeans.lms.resource

import com.infobeans.lms.dto.UpdateModuleProgressRequest
import com.infobeans.lms.service.impl.ModuleProgressService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Controller for student module progress updates.
 */
@RestController
@RequestMapping("/api/v1/modules")
class StudentModuleProgressResource(
    private val moduleProgressService: ModuleProgressService
) {
    /**
     * Update progress status of a module for logged-in student.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PatchMapping("/{moduleId}/progress")
    fun updateModuleProgress(
        @PathVariable moduleId: Long,
        @RequestBody request: UpdateModuleProgressRequest
    ): ResponseEntity<Void> {

        moduleProgressService.updateProgress(moduleId, request)

        return ResponseEntity.noContent().build()
    }
}