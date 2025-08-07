package com.cleveronion.application.upload.usecase

import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.application.shared.port.FileStoragePort
import com.cleveronion.application.upload.command.UploadImageCommand
import com.cleveronion.application.upload.dto.UploadResultDto
import com.cleveronion.common.annotation.UseCase
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.UserId
import java.time.Instant
import java.util.*

/**
 * 上传图片用例
 * 
 * 处理图片上传的业务逻辑，包括用户验证、文件验证和存储
 */
@UseCase
class UploadImageUseCase(
    private val fileStoragePort: FileStoragePort,
    private val userRepositoryPort: UserRepositoryPort
) {
    
    companion object {
        private val ALLOWED_IMAGE_TYPES = setOf(
            "image/jpeg",
            "image/jpg", 
            "image/png",
            "image/gif",
            "image/webp"
        )
        
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        private const val MIN_FILE_SIZE = 1024L // 1KB
    }
    
    /**
     * 执行图片上传
     * 
     * @param command 上传命令
     * @return 上传结果
     * @throws UnauthorizedOperationException 用户不存在或无权限
     * @throws ValidationException 文件验证失败
     */
    suspend fun execute(command: UploadImageCommand): UploadResultDto {
        // 验证用户存在
        val userId = UserId(command.userId)
        val user = userRepositoryPort.findById(userId)
            ?: throw UnauthorizedOperationException("User not found: ${command.userId}")
        
        // 验证文件
        validateImageFile(command)
        
        // 生成唯一文件名
        val uniqueFileName = generateUniqueFileName(command.fileName)
        
        // 上传文件
        val fileUrl = fileStoragePort.uploadFile(
            fileName = uniqueFileName,
            fileContent = command.fileContent,
            contentType = command.contentType
        )
        
        // 返回上传结果
        return UploadResultDto(
            fileName = uniqueFileName,
            originalFileName = command.fileName,
            url = fileUrl,
            contentType = command.contentType,
            fileSize = command.fileSize,
            uploadedAt = Instant.now().toString()
        )
    }
    
    /**
     * 验证图片文件
     */
    private fun validateImageFile(command: UploadImageCommand) {
        // 验证文件类型
        if (command.contentType !in ALLOWED_IMAGE_TYPES) {
            throw ValidationException(
                "Unsupported image type: ${command.contentType}. " +
                "Allowed types: ${ALLOWED_IMAGE_TYPES.joinToString(", ")}"
            )
        }
        
        // 验证文件大小
        if (command.fileSize > MAX_FILE_SIZE) {
            throw ValidationException(
                "File size ${command.fileSize} bytes exceeds maximum allowed size of $MAX_FILE_SIZE bytes"
            )
        }
        
        if (command.fileSize < MIN_FILE_SIZE) {
            throw ValidationException(
                "File size ${command.fileSize} bytes is below minimum required size of $MIN_FILE_SIZE bytes"
            )
        }
        
        // 验证文件内容不为空
        if (command.fileContent.isEmpty()) {
            throw ValidationException("File content cannot be empty")
        }
        
        // 验证实际文件大小与声明大小一致
        if (command.fileContent.size.toLong() != command.fileSize) {
            throw ValidationException(
                "Declared file size ${command.fileSize} does not match actual size ${command.fileContent.size}"
            )
        }
        
        // 验证文件扩展名
        val fileExtension = getFileExtension(command.fileName)
        if (!isValidImageExtension(fileExtension)) {
            throw ValidationException(
                "Invalid file extension: $fileExtension. " +
                "Allowed extensions: jpg, jpeg, png, gif, webp"
            )
        }
    }
    
    /**
     * 生成唯一文件名
     * 格式: images/yyyy/MM/dd/uuid.extension
     */
    private fun generateUniqueFileName(originalFileName: String): String {
        val now = Instant.now()
        val year = now.toString().substring(0, 4)
        val month = now.toString().substring(5, 7)
        val day = now.toString().substring(8, 10)
        
        val extension = getFileExtension(originalFileName)
        val uuid = UUID.randomUUID().toString().replace("-", "")
        
        return "images/$year/$month/$day/$uuid.$extension"
    }
    
    /**
     * 获取文件扩展名
     */
    private fun getFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
            fileName.substring(lastDotIndex + 1).lowercase()
        } else {
            ""
        }
    }
    
    /**
     * 验证图片扩展名
     */
    private fun isValidImageExtension(extension: String): Boolean {
        return extension.lowercase() in setOf("jpg", "jpeg", "png", "gif", "webp")
    }
}