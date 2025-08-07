package com.cleveronion.application.shared.exception

/**
 * 验证异常
 * 
 * 当输入数据验证失败时抛出此异常
 */
class ValidationException(
    message: String,
    val field: String? = null,
    val violations: List<String> = emptyList(),
    cause: Throwable? = null
) : ApplicationException(message, cause) {
    
    companion object {
        /**
         * 创建单个字段验证异常
         */
        fun of(field: String, violation: String): ValidationException {
            return ValidationException(
                message = "Validation failed for field '$field': $violation",
                field = field,
                violations = listOf(violation)
            )
        }
        
        /**
         * 创建多个验证错误的异常
         */
        fun of(violations: Map<String, List<String>>): ValidationException {
            val allViolations = violations.values.flatten()
            val message = "Validation failed: ${allViolations.joinToString(", ")}"
            return ValidationException(
                message = message,
                violations = allViolations
            )
        }
        
        /**
         * 创建通用验证异常
         */
        fun of(message: String): ValidationException {
            return ValidationException(message)
        }
    }
    
    /**
     * 检查是否有特定字段的验证错误
     */
    fun hasFieldError(fieldName: String): Boolean = field == fieldName
    
    /**
     * 获取所有验证错误信息
     */
    fun getAllViolations(): List<String> = violations
}