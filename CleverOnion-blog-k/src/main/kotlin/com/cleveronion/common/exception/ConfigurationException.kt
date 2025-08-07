package com.cleveronion.common.exception

/**
 * 配置异常
 * 
 * 当系统配置错误或缺失时抛出此异常
 */
class ConfigurationException(
    message: String,
    val configKey: String? = null,
    cause: Throwable? = null
) : InfrastructureException(message, cause) {
    
    companion object {
        /**
         * 创建配置缺失异常
         */
        fun missingConfig(configKey: String): ConfigurationException {
            return ConfigurationException(
                message = "Required configuration '$configKey' is missing",
                configKey = configKey
            )
        }
        
        /**
         * 创建配置无效异常
         */
        fun invalidConfig(configKey: String, value: String, reason: String): ConfigurationException {
            return ConfigurationException(
                message = "Invalid configuration '$configKey' with value '$value': $reason",
                configKey = configKey
            )
        }
        
        /**
         * 创建通用配置异常
         */
        fun of(message: String, configKey: String? = null): ConfigurationException {
            return ConfigurationException(message, configKey)
        }
    }
}