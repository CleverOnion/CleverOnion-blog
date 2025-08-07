package com.cleveronion.application.upload.command

/**
 * 上传图片命令
 * 
 * 包含上传图片所需的信息
 */
data class UploadImageCommand(
    val userId: Long,
    val fileName: String,
    val fileContent: ByteArray,
    val contentType: String,
    val fileSize: Long
) {
    init {
        require(userId > 0) { "User ID must be positive" }
        require(fileName.isNotBlank()) { "File name cannot be blank" }
        require(fileContent.isNotEmpty()) { "File content cannot be empty" }
        require(contentType.isNotBlank()) { "Content type cannot be blank" }
        require(fileSize > 0) { "File size must be positive" }
        require(fileSize <= 10 * 1024 * 1024) { "File size cannot exceed 10MB" }
        require(contentType.startsWith("image/")) { "Content type must be an image" }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as UploadImageCommand
        
        if (userId != other.userId) return false
        if (fileName != other.fileName) return false
        if (!fileContent.contentEquals(other.fileContent)) return false
        if (contentType != other.contentType) return false
        if (fileSize != other.fileSize) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileContent.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + fileSize.hashCode()
        return result
    }
}