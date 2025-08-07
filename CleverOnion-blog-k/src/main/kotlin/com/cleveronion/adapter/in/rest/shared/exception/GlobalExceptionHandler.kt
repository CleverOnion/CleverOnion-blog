package com.cleveronion.adapter.`in`.rest.shared.exception

import com.cleveronion.application.shared.exception.ApplicationException
import com.cleveronion.application.shared.exception.ResourceNotFoundException
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.exception.ConfigurationException
import com.cleveronion.common.exception.DatabaseException
import com.cleveronion.common.exception.ExternalServiceException
import com.cleveronion.common.exception.InfrastructureException
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException
import com.cleveronion.domain.shared.exception.ConcurrencyException
import com.cleveronion.domain.shared.exception.DomainException
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

/**
 * 全局异常处理器
 * 
 * 统一处理应用中的各种异常，将其转换为适当的HTTP响应
 */
object GlobalExceptionHandler {
    
    /**
     * 配置Ktor的状态页面插件来处理异常
     */
    fun configure(config: StatusPagesConfig) {
        // 领域异常处理
        config.exception<EntityNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "NOT_FOUND",
                    message = cause.message ?: "Resource not found",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<BusinessRuleViolationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "BUSINESS_RULE_VIOLATION",
                    message = cause.message ?: "Business rule violation",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<UnauthorizedOperationException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(
                    error = "UNAUTHORIZED_OPERATION",
                    message = cause.message ?: "Operation not authorized",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<ConcurrencyException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    error = "CONCURRENCY_CONFLICT",
                    message = cause.message ?: "Concurrency conflict occurred",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<DomainException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "DOMAIN_ERROR",
                    message = cause.message ?: "Domain error occurred",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // 应用异常处理
        config.exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ValidationErrorResponse(
                    error = "VALIDATION_ERROR",
                    message = cause.message ?: "Validation failed",
                    field = cause.field,
                    violations = cause.violations,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<ResourceNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "RESOURCE_NOT_FOUND",
                    message = cause.message ?: "Resource not found",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<ApplicationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "APPLICATION_ERROR",
                    message = cause.message ?: "Application error occurred",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // 基础设施异常处理
        config.exception<DatabaseException> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "DATABASE_ERROR",
                    message = "Database operation failed",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<ExternalServiceException> { call, cause ->
            val statusCode = when (cause.statusCode) {
                in 400..499 -> HttpStatusCode.BadGateway
                in 500..599 -> HttpStatusCode.ServiceUnavailable
                else -> HttpStatusCode.BadGateway
            }
            call.respond(
                statusCode,
                ErrorResponse(
                    error = "EXTERNAL_SERVICE_ERROR",
                    message = "External service call failed",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<ConfigurationException> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "CONFIGURATION_ERROR",
                    message = "System configuration error",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<InfrastructureException> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "INFRASTRUCTURE_ERROR",
                    message = "Internal server error",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // 通用异常处理
        config.exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "INVALID_ARGUMENT",
                    message = cause.message ?: "Invalid argument",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        config.exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "INTERNAL_SERVER_ERROR",
                    message = "An unexpected error occurred",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}

/**
 * 标准错误响应
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Long
)

/**
 * 验证错误响应
 */
@Serializable
data class ValidationErrorResponse(
    val error: String,
    val message: String,
    val field: String? = null,
    val violations: List<String> = emptyList(),
    val timestamp: Long
)