package com.cleveronion.application.upload.usecase

import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.application.shared.port.FileStoragePort
import com.cleveronion.application.upload.command.DeleteImageCommand
import com.cleveronion.application.upload.dto.DeleteResultDto
import com.cleveronion.common.annotation.UseCase
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.UserId

/**
 * 删除图片用例
 * 
 * 处理图片删除的业务逻辑，包括用户验证和文件删除
 */
@UseCase
class DeleteImageUseCase(
    private val fileStoragePort: FileStoragePort,
    private val userRepositoryPort: UserRepositoryPort
) {
    
    /**
     * 执行图片删除
     * 
     * @param command 删除命令
     * @return 删除结果
     * @throws UnauthorizedOperationException 用户不存在或无权限
     * @throws ValidationException 文件URL验证失败
     */
    suspend fun execute(command: DeleteImageCommand): DeleteResultDto {
        // 验证用户存在
        val userId = UserId(command.userId)
        val user = userRepositoryPort.findById(userId)
            ?: throw UnauthorizedOperationException("User not found: ${command.userId}")
        
        // 验证文件URL
        validateFileUrl(command.fileUrl)
        
        // 检查文件是否存在
        val fileExists = fileStoragePort.fileExists(command.fileUrl)
        if (!fileExists) {
            return DeleteResultDto(
                url = command.fileUrl,
                success = false,
                message = "File not found"
            )
        }
        
        // 删除文件
        val deleteSuccess = try {
            fileStoragePort.deleteFile(command.fileUrl)
        } catch (e: Exception) {
            return DeleteResultDto(
                url = command.fileUrl,
                success = false,
                message = "Failed to delete file: ${e.message}"
            )
        }
        
        return if (deleteSuccess) {
            DeleteResultDto(
                url = command.fileUrl,
                success = true,
                message = "File deleted successfully"
            )
        } else {
            DeleteResultDto(
                url = command.fileUrl,
                success = false,
                message = "Failed to delete file"
            )
        }
    }
    
    /**
     * 验证文件URL
     */
    private fun validateFileUrl(fileUrl: String) {
        if (fileUrl.isBlank()) {
            throw ValidationException("File URL cannot be blank")
        }
        
        // 验证URL格式（简单验证）
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://") && !fileUrl.startsWith("images/")) {
            throw ValidationException("Invalid file URL format: $fileUrl")
        }
        
        // 验证是否为图片文件
        val lowercaseUrl = fileUrl.lowercase()
        val validExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
        val hasValidExtension = validExtensions.any { lowercaseUrl.endsWith(it) }
        
        if (!hasValidExtension) {
            throw ValidationException(
                "URL does not point to a valid image file: $fileUrl. " +
                "Supported extensions: ${validExtensions.joinToString(", ")}"
            )
        }
    }
}