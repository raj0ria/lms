package com.infobeans.lms.resource

import com.infobeans.lms.dto.InstructorCourseStudentResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.service.InstructorCourseAnalyticsService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource exposing instructor analytics endpoints.
 */
@RestController
@RequestMapping("/api/v1/instructors")
class InstructorCourseAnalyticsResource(
    private val analyticsService: InstructorCourseAnalyticsService
) {

    private val log = LoggerFactory.getLogger(InstructorCourseAnalyticsResource::class.java)

    /**
     * Returns paginated list of enrolled students for a course.
     */
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/courses/{courseId}/students")
    fun getCourseStudents(
        @PathVariable courseId: Long,
        @PageableDefault(
            size = 10,
            sort = ["enrolledAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable
    ): ResponseEntity<PagedResponse<InstructorCourseStudentResponse>> {

        log.info("GET /api/instructors/courses/{}/students invoked", courseId)

        val response = analyticsService.getCourseStudents(courseId, pageable)

        return ResponseEntity.ok(response)
    }
}
