package com.cleveronion.application.upload.service

import com.cleveronion.application.upload.command.UploadImageCommand
import com.cleveronion.application.upload.command.DeleteImageCommand
import com.cleveronion.application.upload.dto.UploadResultDto
import com.cleveronion.application.upload.usecase.UploadImageUseCase
import com.cleveronion.application.upload.usecase.DeleteImageUseCase
import com.cleveronion.common.annotation.UseCase

/**
 * 文件上传应用服务
 * 
 * 协调文件上传相关的用例，提供统一的文件管理接口
 * 处理图片上传、删除等功能
 */
@UseCase
class UploadApplicationService(
    private val uploadImageUseCase: UploadImageUseCase,
    private val deleteImageUseCase: DeleteImageUseCase
) {
    
    /**
     * 上传图片
     * 
     * 处理图片文件上传，包括验证和存储
     * 
     * @param command 上传图片命令
     * @return 上传结果DTO
     */
    suspend fun uploadImage(command: UploadImageCommand): UploadResultDto {
        return uploadImageUseCase.execute(command)
    }
    
    /**
     * 删除图片
     * 
     * 处理图片文件删除，包括权限验证
     * 
     * @param command 删除图片命令
     */
    suspend fun deleteImage(command: DeleteImageCommand) {
        deleteImageUseCase.execute(command)
    }
    
    /**
     * 上传用户头像
     * 
     * 便捷方法，专门用于上传用户头像
     * 
     * @param userId 用户ID
     * @param fileName 文件名
     * @param fileContent 文件内容
     * @param contentType 内容类型
     * @return 上传结果DTO
     */
    suspend fun uploadUserAvatar(
        userId: Long,
        fileName: String,
        fileContent: ByteArray,
        contentType: String
    ): UploadResultDto {
        val command = UploadImageCommand(
            userId = userId,
            fileName = fileName,
            fileContent = fileContent,
            contentType = contentType,
            fileSize = fileContent.size.toLong()
        )
        return uploadImageUseCase.execute(command)
    }
    
    /**
     * 上传文章图片
     * 
     * 便捷方法，专门用于上传文章中的图片
     * 
     * @param userId 用户ID
     * @param fileName 文件名
     * @param fileContent 文件内容
     * @param contentType 内容类型
     * @return 上传结果DTO
     */
    suspend fun uploadArticleImage(
        userId: Long,
        fileName: String,
        fileContent: ByteArray,
        contentType: String
    ): UploadResultDto {
        val command = UploadImageCommand(
            userId = userId,
            fileName = fileName,
            fileContent = fileContent,
            contentType = contentType,
            fileSize = fileContent.size.toLong()
        )
        return uploadImageUseCase.execute(command)
    }
    
    /**
     * 批量上传图片
     * 
     * 处理多个图片文件的批量上传
     * 
     * @param userId 用户ID
     * @param images 图片信息列表
     * @return 上传结果列表
     */
    suspend fun uploadMultipleImages(
        userId: Long,
        images: List<ImageUploadInfo>
    ): List<UploadResultDto> {
        return images.map { imageInfo ->
            val command = UploadImageCommand(
                userId = userId,
                fileName = imageInfo.fileName,
                fileContent = imageInfo.fileContent,
                contentType = imageInfo.contentType,
                fileSize = imageInfo.fileContent.size.toLong()
            )
            uploadImageUseCase.execute(command)
        }
    }
    
    /**
     * 删除用户上传的图片
     * 
     * 便捷方法，用于删除用户上传的图片
     * 
     * @param userId 用户ID
     * @param fileUrl 文件URL
     */
    suspend fun deleteUserImage(userId: Long, fileUrl: String) {
        val command = DeleteImageCommand(
            userId = userId,
            fileUrl = fileUrl
        )
        deleteImageUseCase.execute(command)
    }
    
    /**
     * 管理员删除图片
     * 
     * 便捷方法，用于管理员删除任意图片
     * 
     * @param adminUserId 管理员用户ID
     * @param fileUrl 文件URL
     */
    suspend fun adminDeleteImage(adminUserId: Long, fileUrl: String) {
        val command = DeleteImageCommand(
            userId = adminUserId,
            fileUrl = fileUrl
        )
        deleteImageUseCase.execute(command)
    }
}

/**
 * 图片上传信息
 * 
 * 用于批量上传时传递图片信息
 */
data class ImageUploadInfo(
    val fileName: String,
    val fileContent: ByteArray,
    val contentType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageUploadInfo

        if (fileName != other.fileName) return false
        if (!fileContent.contentEquals(other.fileContent)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + fileContent.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}