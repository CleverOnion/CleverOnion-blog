package com.cleveronion.application.comment.command

/**
 * 删除评论命令
 * 
 * 包含删除评论所需的信息
 */
data class DeleteCommentCommand(
    val commentId: Long,
    val userId: Long
) {
    init {
        require(commentId > 0) { "Comment ID must be positive" }
        require(userId > 0) { "User ID must be positive" }
    }
}