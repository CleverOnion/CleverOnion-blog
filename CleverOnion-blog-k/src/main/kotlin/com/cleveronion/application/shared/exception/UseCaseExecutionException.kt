package com.cleveronion.application.shared.exception

/**
 * 用例执行异常
 * 
 * 当用例执行过程中发生错误时抛出此异常
 */
class UseCaseExecutionException(
    message: String,
    val useCaseName: String,
    cause: Throwable? = null
) : ApplicationException(message, cause) {
    
    companion object {
        /**
         * 创建用例执行异常
         */
        fun of(useCaseName: String, error: String, cause: Throwable? = null): UseCaseExecutionException {
            return UseCaseExecutionException(
                message = "Failed to execute use case '$useCaseName': $error",
                useCaseName = useCaseName,
                cause = cause
            )
        }
    }
}