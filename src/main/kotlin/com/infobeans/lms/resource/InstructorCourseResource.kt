package com.infobeans.lms.resource

import com.infobeans.lms.dto.CreateCourseRequest
import com.infobeans.lms.dto.CourseResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateCourseRequest
import com.infobeans.lms.service.impl.InstructorCourseService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * REST resource for instructor course management.
 */
@RestController
@RequestMapping("/api/v1/instructor/courses")
@PreAuthorize("hasRole('INSTRUCTOR')")
class InstructorCourseResource(
    private val courseService: InstructorCourseService
) {

    private val log = LoggerFactory.getLogger(InstructorCourseResource::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCourse(
        @Valid @RequestBody request: CreateCourseRequest
    ): CourseResponse {
        log.info("POST /api/v1/instructor/courses invoked")
        return courseService.createCourse(request)
    }

    @PutMapping("/{courseId}")
    fun updateCourse(
        @PathVariable courseId: Long,
        @RequestBody request: UpdateCourseRequest
    ): CourseResponse {

        log.info("PUT /api/v1/instructor/courses/{}", courseId)

        return courseService.updateCourse(courseId, request)
    }

    @DeleteMapping("/{courseId}")
    fun deleteCourse(
        @PathVariable courseId: Long
    ): ResponseEntity<Void> {

        log.info("DELETE /api/v1/instructor/courses/{}", courseId)

        courseService.deleteCourse(courseId)

        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{courseId}/publish")
    fun publishCourse(
        @PathVariable courseId: Long
    ): CourseResponse {

        log.info("PATCH /api/v1/instructor/courses/{}/publish", courseId)

        return courseService.publishCourse(courseId)
    }

    @GetMapping
    fun getOwnCourses(
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<PagedResponse<CourseResponse>> {

        log.info("GET /api/v1/instructor/courses invoked")

        return ResponseEntity.ok(courseService.getInstructorCourses(pageable))
    }
}

