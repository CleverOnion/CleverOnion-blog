package com.cleveronion.controller

import com.cleveronion.config.OssConfig
import com.cleveronion.domain.entity.*
import com.cleveronion.service.FileUploadService
import com.cleveronion.service.OssFileUploadService
import com.cleveronion.getUserId
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 文件上传控制器
 * 提供RESTful API接口，处理文件上传相关的HTTP请求
 * 遵循单一职责原则，专注于HTTP层的处理
 */
fun Route.uploadRoutes() {
    // 从应用配置中获取OSS配置
    val ossConfig = OssConfig.fromApplicationConfig(application.environment.config)
    
    // 验证OSS配置
    if (!ossConfig.validate()) {
        application.log.error("OSS配置无效，文件上传功能将不可用")
        return
    }
    
    // 创建文件上传服务实例
    val fileUploadService: FileUploadService = OssFileUploadService(ossConfig)
    
    route("/upload") {
        
        /**
         * 上传图片文件
         * POST /api/v1/upload/image
         * 需要认证，支持multipart/form-data格式
         */
        authenticate("auth-jwt") {
            post("/image") {
                try {
                    // 获取当前用户ID（用于日志记录）
                    val userId = call.getUserId()
                    application.log.info("用户 $userId 开始上传图片")
                    
                    // 解析multipart请求
                    val multipart = call.receiveMultipart()
                    var fileInfo: FileInfo? = null
                    
                    // 处理multipart数据
                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                if (part.name == "file") {
                                    fileInfo = processFilePart(part, ossConfig)
                                }
                            }
                            else -> {
                                // 忽略其他类型的part
                            }
                        }
                        part.dispose()
                    }
                    
                    // 检查是否提供了文件
                    if (fileInfo == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            UploadResponse(
                                success = false,
                                message = UploadError.NO_FILE_PROVIDED.message
                            )
                        )
                        return@post
                    }
                    
                    // 上传文件
                    val uploadData = fileUploadService.uploadFile(fileInfo!!)
                    
                    application.log.info("用户 $userId 成功上传图片: ${uploadData.fileName}")
                    
                    // 返回成功响应
                    call.respond(
                        HttpStatusCode.OK,
                        UploadResponse(
                            success = true,
                            message = "图片上传成功",
                            data = uploadData
                        )
                    )
                    
                } catch (e: UploadException) {
                    application.log.warn("文件上传失败: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UploadResponse(
                            success = false,
                            message = e.message ?: "上传失败"
                        )
                    )
                } catch (e: Exception) {
                    application.log.error("文件上传异常", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        UploadResponse(
                            success = false,
                            message = "服务器内部错误"
                        )
                    )
                }
            }
        }
        
        /**
         * 删除图片文件
         * DELETE /api/v1/upload/image/{fileName}
         * 需要认证
         */
        authenticate("auth-jwt") {
            delete("/image/{fileName}") {
                try {
                    val fileName = call.parameters["fileName"]
                    if (fileName.isNullOrBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            UploadResponse(
                                success = false,
                                message = "文件名不能为空"
                            )
                        )
                        return@delete
                    }
                    
                    val userId = call.getUserId()
                    application.log.info("用户 $userId 请求删除图片: $fileName")
                    
                    // 检查文件是否存在
                    if (!fileUploadService.fileExists(fileName)) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            UploadResponse(
                                success = false,
                                message = "文件不存在"
                            )
                        )
                        return@delete
                    }
                    
                    // 删除文件
                    val deleted = fileUploadService.deleteFile(fileName)
                    
                    if (deleted) {
                        application.log.info("用户 $userId 成功删除图片: $fileName")
                        call.respond(
                            HttpStatusCode.OK,
                            UploadResponse(
                                success = true,
                                message = "文件删除成功"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            UploadResponse(
                                success = false,
                                message = "文件删除失败"
                            )
                        )
                    }
                    
                } catch (e: Exception) {
                    application.log.error("删除文件异常", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        UploadResponse(
                            success = false,
                            message = "服务器内部错误"
                        )
                    )
                }
            }
        }
        
        /**
         * 获取上传配置信息
         * GET /api/v1/upload/config
         * 公开接口，返回客户端需要的配置信息
         */
        get("/config") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "maxFileSize" to ossConfig.maxFileSize,
                    "allowedExtensions" to ossConfig.allowedExtensions,
                    "maxFileSizeMB" to ossConfig.maxFileSize / 1024 / 1024
                )
            )
        }
    }
}

/**
 * 处理文件part，提取文件信息
 * 私有函数，用于解析multipart中的文件数据
 */
private suspend fun processFilePart(part: PartData.FileItem, ossConfig: OssConfig): FileInfo {
    val originalFileName = part.originalFileName ?: throw UploadException(UploadError.INVALID_FILE_NAME)
    
    // 提取文件扩展名
    val extension = File(originalFileName).extension.lowercase()
    if (extension.isBlank()) {
        throw UploadException(UploadError.INVALID_FILE_TYPE, "文件必须有扩展名")
    }
    
    // 检查文件类型
    if (!ossConfig.isExtensionAllowed(extension)) {
        throw UploadException(
            UploadError.INVALID_FILE_TYPE,
            "不支持的文件类型: $extension，支持的类型: ${ossConfig.allowedExtensions.joinToString(", ")}"
        )
    }
    
    // 读取文件内容
    val fileBytes = withContext(Dispatchers.IO) {
        part.streamProvider().readBytes()
    }
    
    // 检查文件大小
    if (!ossConfig.isFileSizeAllowed(fileBytes.size.toLong())) {
        throw UploadException(
            UploadError.FILE_TOO_LARGE,
            "文件大小超过限制，最大允许: ${ossConfig.maxFileSize / 1024 / 1024}MB"
        )
    }
    
    // 检查文件内容是否为空
    if (fileBytes.isEmpty()) {
        throw UploadException(UploadError.INVALID_FILE_NAME, "文件内容不能为空")
    }
    
    return FileInfo(
        originalFileName = originalFileName,
        fileName = originalFileName,
        fileSize = fileBytes.size.toLong(),
        contentType = part.contentType?.toString() ?: "application/octet-stream",
        extension = extension,
        content = fileBytes
    )
}