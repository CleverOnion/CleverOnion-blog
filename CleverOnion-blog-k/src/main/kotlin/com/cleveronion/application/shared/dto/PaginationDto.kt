package com.cleveronion.application.shared.dto

import com.cleveronion.domain.shared.valueobject.Pagination
import kotlinx.serialization.Serializable

/**
 * 分页DTO
 * 
 * 用于API请求和响应中的分页信息传输
 */
@Serializable
data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val totalElements: Long? = null,
    val totalPages: Int? = null,
    val hasNext: Boolean? = null,
    val hasPrevious: Boolean? = null
) {
    companion object {
        /**
         * 从领域值对象创建DTO
         */
        fun fromDomain(pagination: Pagination, totalElements: Long? = null): PaginationDto {
            return PaginationDto(
                page = pagination.page,
                pageSize = pagination.pageSize,
                totalElements = totalElements,
                totalPages = totalElements?.let { pagination.getTotalPages(it) },
                hasNext = totalElements?.let { !pagination.isLastPage(it) },
                hasPrevious = !pagination.isFirstPage()
            )
        }
    }
    
    /**
     * 转换为领域值对象
     */
    fun toDomain(): Pagination {
        return Pagination(page, pageSize)
    }
}