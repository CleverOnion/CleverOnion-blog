package com.cleveronion.application.comment.dto

import com.cleveronion.domain.comment.aggregate.Comment

/**
 * 评论数据传输对象
 * 
 * 用于在应用层和外部层之间传输评论数据
 */
data class CommentDto(
    val id: Long,
    val articleId: Long,
    val userId: Long,
    val content: String,
    val parentId: Long?,
    val isReply: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val replies: List<CommentDto> = emptyList()
) {
    companion object {
        /**
         * 从领域模型创建DTO
         */
        fun fromDomain(comment: Comment, replies: List<CommentDto> = emptyList()): CommentDto {
            return CommentDto(
                id = comment.getId().value,
                articleId = comment.getArticleId().value,
                userId = comment.getUserId().value,
                content = comment.getContent().value,
                parentId = comment.getParentId()?.value,
                isReply = comment.isReply(),
                createdAt = comment.getCreatedAt().value.toString(),
                updatedAt = comment.getUpdatedAt().value.toString(),
                replies = replies
            )
        }
    }
}

/**
 * 评论列表数据传输对象
 * 
 * 用于传输评论列表和分页信息
 */
data class CommentListDto(
    val comments: List<CommentDto>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
) {
    companion object {
        /**
         * 创建评论列表DTO
         */
        fun create(
            comments: List<CommentDto>,
            totalCount: Long,
            page: Int,
            pageSize: Int
        ): CommentListDto {
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pageSize + 1).toInt()
            return CommentListDto(
                comments = comments,
                totalCount = totalCount,
                page = page,
                pageSize = pageSize,
                totalPages = totalPages
            )
        }
    }
}