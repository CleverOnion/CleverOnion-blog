package com.cleveronion.application.upload.command

/**
 * 删除图片命令
 * 
 * 包含删除图片所需的信息
 */
data class DeleteImageCommand(
    val userId: Long,
    val fileUrl: String
) {
    init {
        require(userId > 0) { "User ID must be positive" }
        require(fileUrl.isNotBlank()) { "File URL cannot be blank" }
    }
}