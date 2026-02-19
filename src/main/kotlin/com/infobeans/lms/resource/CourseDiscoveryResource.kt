package com.infobeans.lms.resource

import com.infobeans.lms.dto.CourseDetailResponse
import com.infobeans.lms.dto.CourseSearchResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.service.CourseDiscoveryService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource exposing course discovery endpoints.
 */
@RestController
@RequestMapping("/api/v1/courses")
class CourseDiscoveryResource(
    private val discoveryService: CourseDiscoveryService
) {

    private val log = LoggerFactory.getLogger(CourseDiscoveryResource::class.java)

    /**
     * Returns paginated list of published courses.
     */
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @GetMapping
    fun searchCourses(
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable
    ): ResponseEntity<PagedResponse<CourseSearchResponse>> {

        log.info("GET /api/v1/courses invoked")

        val response = discoveryService.searchCourses(keyword, pageable)

        return ResponseEntity.ok(response)
    }

    /**
     * Get full course detail
     */
    @GetMapping("/{courseId}/full-detail")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    fun getCourseDetail(
        @PathVariable courseId: Long
    ): ResponseEntity<CourseDetailResponse> {

        log.info("GET /api/v1/courses/{} invoked", courseId)

        return ResponseEntity.ok(
            discoveryService.getCourseDetail(courseId)
        )
    }
}
