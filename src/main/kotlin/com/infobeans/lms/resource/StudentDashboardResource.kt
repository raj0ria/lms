package com.infobeans.lms.resource

import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.StudentCourseDashboardResponse
import com.infobeans.lms.service.StudentDashboardService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Student Dashboard Controller.
 * Provides enrolled courses with progress details.
 */
@RestController
@RequestMapping("/api/v1/students")
class StudentDashboardResource(
    private val studentDashboardService: StudentDashboardService
) {

    private val log = LoggerFactory.getLogger(StudentDashboardResource::class.java)

    /**
     * Get paginated list of logged-in student's enrolled courses.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me/courses")
    fun getMyCourses(
        @PageableDefault(
            size = 10,
            sort = ["enrolledAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable
    ): ResponseEntity<PagedResponse<StudentCourseDashboardResponse>> {

        val response = studentDashboardService.getMyCourses(pageable)

        return ResponseEntity.ok(response)
    }

}


