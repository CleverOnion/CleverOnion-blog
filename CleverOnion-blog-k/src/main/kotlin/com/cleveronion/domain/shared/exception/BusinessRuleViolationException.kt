package com.cleveronion.domain.shared.exception

/**
 * 业务规则违反异常
 * 
 * 当违反业务规则或不变性约束时抛出此异常
 */
class BusinessRuleViolationException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {
    
    companion object {
        /**
         * 创建业务规则违反异常
         */
        fun of(rule: String, details: String? = null): BusinessRuleViolationException {
            val message = if (details != null) {
                "Business rule violation: $rule. Details: $details"
            } else {
                "Business rule violation: $rule"
            }
            return BusinessRuleViolationException(message)
        }
    }
}