package com.cleveronion.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class PaginationInfo(
    val currentPage: Int,
    val pageSize: Int,
    val totalCount: Long,
    val totalPages: Int
)