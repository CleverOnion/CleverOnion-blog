package com.cleveronion.config

import io.ktor.server.application.*
import io.ktor.server.config.*

/**
 * 阿里云OSS配置类
 * 负责从配置文件中读取OSS相关配置信息
 * 遵循单一职责原则，专注于配置管理
 */
data class OssConfig(
    val endpoint: String,
    val accessKeyId: String,
    val accessKeySecret: String,
    val bucketName: String,
    val maxFileSize: Long,
    val allowedExtensions: List<String>,
    val urlPrefix: String
) {
    companion object {
        /**
         * 从应用配置中创建OSS配置实例
         * 使用工厂模式创建配置对象
         */
        fun fromApplicationConfig(config: ApplicationConfig): OssConfig {
            return OssConfig(
                endpoint = config.property("oss.endpoint").getString(),
                accessKeyId = config.property("oss.accessKeyId").getString(),
                accessKeySecret = config.property("oss.accessKeySecret").getString(),
                bucketName = config.property("oss.bucketName").getString(),
                maxFileSize = config.property("oss.maxFileSize").getString().toLong(),
                allowedExtensions = config.property("oss.allowedExtensions").getList(),
                urlPrefix = config.property("oss.urlPrefix").getString()
            )
        }
    }
    
    /**
     * 验证配置是否有效
     * 确保所有必要的配置项都已设置
     */
    fun validate(): Boolean {
        return endpoint.isNotBlank() &&
                accessKeyId.isNotBlank() &&
                accessKeySecret.isNotBlank() &&
                bucketName.isNotBlank() &&
                maxFileSize > 0 &&
                allowedExtensions.isNotEmpty() &&
                urlPrefix.isNotBlank()
    }
    
    /**
     * 检查文件扩展名是否被允许
     */
    fun isExtensionAllowed(extension: String): Boolean {
        return allowedExtensions.contains(extension.lowercase())
    }
    
    /**
     * 检查文件大小是否在允许范围内
     */
    fun isFileSizeAllowed(size: Long): Boolean {
        return size <= maxFileSize
    }
}