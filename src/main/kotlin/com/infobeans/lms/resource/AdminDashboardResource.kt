package com.infobeans.lms.resource

import com.infobeans.lms.dto.AdminDashboardSummaryResponse
import com.infobeans.lms.service.impl.AdminDashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource exposing admin dashboard summary.
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
class AdminDashboardResource(
    private val adminDashboardService: AdminDashboardService
) {

    private val log = LoggerFactory.getLogger(AdminDashboardResource::class.java)

    /**
     * Returns dashboard summary statistics.
     */
    @Operation(
        summary = "Get ",
        description = "Returns Admin dashboard summary statistics."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Access denied")
        ]
    )
    @GetMapping("/summary")
    fun getDashboardSummary(): ResponseEntity<AdminDashboardSummaryResponse> {

        log.info("GET /api/v1/admin/dashboard/summary invoked")

        return ResponseEntity.ok(
            adminDashboardService.getDashboardSummary()
        )
    }
}
