package com.cleveronion.application.comment.command

/**
 * 回复评论命令
 * 
 * 包含回复评论所需的信息
 */
data class ReplyToCommentCommand(
    val parentCommentId: Long,
    val articleId: Long,
    val userId: Long,
    val content: String
) {
    init {
        require(parentCommentId > 0) { "Parent comment ID must be positive" }
        require(articleId > 0) { "Article ID must be positive" }
        require(userId > 0) { "User ID must be positive" }
        require(content.isNotBlank()) { "Content cannot be blank" }
        require(content.length <= 1000) { "Content cannot exceed 1000 characters" }
    }
}