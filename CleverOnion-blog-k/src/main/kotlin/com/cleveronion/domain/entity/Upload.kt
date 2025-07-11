package com.cleveronion.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * 文件上传响应数据类
 * 用于返回文件上传成功后的信息
 */
@Serializable
data class UploadResponse(
    val success: Boolean,
    val message: String,
    val data: UploadData? = null
)

/**
 * 上传文件数据类
 * 包含上传成功后的文件信息
 */
@Serializable
data class UploadData(
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val fileType: String,
    val url: String,
    val uploadTime: String
)

/**
 * 文件上传错误类型枚举
 * 定义各种可能的上传错误
 */
enum class UploadError(val code: String, val message: String) {
    FILE_TOO_LARGE("FILE_TOO_LARGE", "文件大小超过限制"),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "不支持的文件类型"),
    UPLOAD_FAILED("UPLOAD_FAILED", "文件上传失败"),
    NO_FILE_PROVIDED("NO_FILE_PROVIDED", "未提供文件"),
    INVALID_FILE_NAME("INVALID_FILE_NAME", "无效的文件名"),
    OSS_CONFIG_ERROR("OSS_CONFIG_ERROR", "OSS配置错误"),
    NETWORK_ERROR("NETWORK_ERROR", "网络连接错误")
}

/**
 * 文件上传异常类
 * 用于处理文件上传过程中的各种异常情况
 */
class UploadException(
    val error: UploadError,
    message: String = error.message,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * 文件信息数据类
 * 用于在上传过程中传递文件信息
 */
data class FileInfo(
    val originalFileName: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
    val extension: String,
    val content: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileInfo

        if (originalFileName != other.originalFileName) return false
        if (fileName != other.fileName) return false
        if (fileSize != other.fileSize) return false
        if (contentType != other.contentType) return false
        if (extension != other.extension) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalFileName.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}