package com.cleveronion.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * 统一API响应格式
 * @param T 数据类型
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
) {
    companion object {
        /**
         * 创建成功响应
         */
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        /**
         * 创建成功响应（无数据）
         */
        fun success(message: String): ApiResponse<Unit> {
            return ApiResponse(
                success = true,
                data = Unit,
                message = message
            )
        }

        /**
         * 创建错误响应
         */
        fun <T> error(error: String, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = error,
                message = message
            )
        }

        /**
         * 创建分页响应
         */
        fun <T> paginated(data: List<T>, pagination: PaginationInfo, message: String? = null): ApiResponse<PaginatedData<T>> {
            return ApiResponse(
                success = true,
                data = PaginatedData(data, pagination),
                message = message
            )
        }
    }
}

/**
 * 分页数据包装器
 */
@Serializable
data class PaginatedData<T>(
    val items: List<T>,
    val pagination: PaginationInfo
)

/**
 * 搜索结果数据包装器
 */
@Serializable
data class SearchData<T>(
    val keyword: String,
    val items: List<T>,
    val total: Int
)

/**
 * 批量操作结果数据包装器
 */
@Serializable
data class BatchOperationData<T>(
    val items: List<T>,
    val total: Int,
    val success: Int,
    val failed: Int = 0
)