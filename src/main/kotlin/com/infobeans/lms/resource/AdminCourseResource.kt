package com.infobeans.lms.resource

import com.infobeans.lms.dto.AdminCourseResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.service.impl.AdminCourseService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource exposing admin course management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
class AdminCourseResource(
    private val adminCourseService: AdminCourseService
) {

    private val log = LoggerFactory.getLogger(AdminCourseResource::class.java)

    /**
     * Returns paginated list of all courses (published & unpublished).
     */
    @GetMapping
    fun getAllCourses(
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: org.springframework.data.domain.Pageable
    ): ResponseEntity<PagedResponse<AdminCourseResponse>> {

        log.info("GET /api/v1/admin/courses invoked")

        val response = adminCourseService.getAllCourses(keyword, pageable)

        return ResponseEntity.ok(response)
    }

    /**
     * Deletes a course by ID.
     */
    @DeleteMapping("/{courseId}")
    fun deleteCourse(
        @PathVariable courseId: Long
    ): ResponseEntity<Void> {

        log.info("DELETE /api/v1/admin/courses/{} invoked", courseId)

        adminCourseService.deleteCourse(courseId)

        return ResponseEntity.noContent().build()
    }


}
