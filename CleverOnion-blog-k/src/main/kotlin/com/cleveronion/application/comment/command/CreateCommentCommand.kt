package com.cleveronion.application.comment.command

/**
 * 创建评论命令
 * 
 * 包含创建评论所需的信息
 */
data class CreateCommentCommand(
    val articleId: Long,
    val userId: Long,
    val content: String,
    val parentId: Long? = null
) {
    init {
        require(articleId > 0) { "Article ID must be positive" }
        require(userId > 0) { "User ID must be positive" }
        require(content.isNotBlank()) { "Content cannot be blank" }
        require(content.length <= 1000) { "Content cannot exceed 1000 characters" }
        parentId?.let { require(it > 0) { "Parent ID must be positive" } }
    }
}