package com.infobeans.lms.exceptions

import com.infobeans.lms.dto.ApiError
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun buildError(
        ex: Exception,
        status: HttpStatus,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        log.error("Error occurred: {}", ex.message, ex)

        val error = ApiError(
            status = status.value(),
            error = status.reasonPhrase,
            message = ex.message,
            path = request.requestURI
        )

        return ResponseEntity(error, status)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ) = buildError(ex, HttpStatus.NOT_FOUND, request)

    @ExceptionHandler(DuplicateEnrollmentException::class)
    fun handleDuplicateEnrollment(
        ex: DuplicateEnrollmentException,
        request: HttpServletRequest
    ) = buildError(ex, HttpStatus.CONFLICT, request)

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRule(
        ex: BusinessRuleViolationException,
        request: HttpServletRequest
    ) = buildError(ex, HttpStatus.BAD_REQUEST, request)

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ) = buildError(ex, HttpStatus.FORBIDDEN, request)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {

        val message = ex.bindingResult
            .fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = message,
            path = request.requestURI
        )

        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(JWTExpiredException::class)
    fun handleJwtExpired(
        ex: JWTExpiredException,
        request: HttpServletRequest
    ) = buildError(ex, HttpStatus.UNAUTHORIZED, request)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest
    ) = buildError(ex, HttpStatus.INTERNAL_SERVER_ERROR, request)
}
