package com.infobeans.lms.resource

import com.infobeans.lms.dto.CourseDetailResponse
import com.infobeans.lms.dto.CourseSearchResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.service.impl.CourseDiscoveryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
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
@Tag(
    name = "Course Discovery",
    description = "Endpoints for searching and retrieving published courses"
)
@SecurityRequirement(name = "bearerAuth")
class CourseDiscoveryResource(
    private val discoveryService: CourseDiscoveryService
) {

    private val log = LoggerFactory.getLogger(CourseDiscoveryResource::class.java)

    /**
     * Returns paginated list of published courses.
     */
    @Operation(
        summary = "Search published courses",
        description = "Returns paginated list of published courses. Accessible to STUDENT, INSTRUCTOR and ADMIN roles."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Courses fetched successfully",
                content = [Content(
                    schema = Schema(implementation = PagedResponse::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Access denied")
        ]
    )
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @GetMapping
    fun searchCourses(
        @Parameter(
            description = "Keyword to search in course title or description",
            example = "spring boot",
            required = false
        )
        @RequestParam(required = false) keyword: String?,
        @ParameterObject
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
    @Operation(
        summary = "Get full course detail",
        description = "Returns complete course details including modules. Accessible only to ADMIN and INSTRUCTOR roles."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Course detail fetched successfully",
                content = [Content(
                    schema = Schema(implementation = CourseDetailResponse::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Access denied"),
            ApiResponse(responseCode = "404", description = "Course not found")
        ]
    )
    @GetMapping("/{courseId}/full-detail")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    fun getCourseDetail(
        @Parameter(
            description = "ID of the course",
            example = "101",
            required = true,
            `in` = ParameterIn.PATH
        )
        @PathVariable courseId: Long
    ): ResponseEntity<CourseDetailResponse> {

        log.info("GET /api/v1/courses/{} invoked", courseId)

        return ResponseEntity.ok(
            discoveryService.getCourseDetail(courseId)
        )
    }
}
