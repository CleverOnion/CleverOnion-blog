package com.cleveronion.application.shared.dto

import kotlinx.serialization.Serializable

/**
 * 统一API响应DTO
 * 
 * 用于包装所有API响应，提供统一的响应格式
 */
@Serializable
data class ApiResponseDto<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 创建成功响应
         */
        fun <T> success(data: T, message: String? = null): ApiResponseDto<T> {
            return ApiResponseDto(
                success = true,
                data = data,
                message = message
            )
        }
        
        /**
         * 创建成功响应（无数据）
         */
        fun success(message: String? = null): ApiResponseDto<Unit> {
            return ApiResponseDto(
                success = true,
                data = Unit,
                message = message
            )
        }
        
        /**
         * 创建失败响应
         */
        fun <T> failure(message: String): ApiResponseDto<T> {
            return ApiResponseDto(
                success = false,
                data = null,
                message = message
            )
        }
    }
}

/**
 * 分页响应DTO
 * 
 * 用于包装分页查询的响应数据
 */
@Serializable
data class PagedResponseDto<T>(
    val content: List<T>,
    val pagination: PaginationDto
) {
    companion object {
        /**
         * 创建分页响应
         */
        fun <T> of(content: List<T>, pagination: PaginationDto): PagedResponseDto<T> {
            return PagedResponseDto(content, pagination)
        }
    }
}