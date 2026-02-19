package com.infobeans.lms.dto

data class PagedResponse<T>(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val content: List<T>
)
