package com.cleveronion.application.upload.dto

/**
 * 上传结果数据传输对象
 * 
 * 用于传输文件上传结果
 */
data class UploadResultDto(
    val fileName: String,
    val originalFileName: String,
    val url: String,
    val contentType: String,
    val fileSize: Long,
    val uploadedAt: String
)

/**
 * 删除结果数据传输对象
 * 
 * 用于传输文件删除结果
 */
data class DeleteResultDto(
    val url: String,
    val success: Boolean,
    val message: String? = null
)