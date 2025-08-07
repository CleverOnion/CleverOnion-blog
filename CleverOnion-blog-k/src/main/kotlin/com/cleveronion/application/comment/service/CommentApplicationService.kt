package com.cleveronion.application.comment.service

import com.cleveronion.application.comment.command.CreateCommentCommand
import com.cleveronion.application.comment.command.ReplyToCommentCommand
import com.cleveronion.application.comment.command.DeleteCommentCommand
import com.cleveronion.application.comment.dto.CommentDto
import com.cleveronion.application.comment.usecase.CreateCommentUseCase
import com.cleveronion.application.comment.usecase.ReplyToCommentUseCase
import com.cleveronion.application.comment.usecase.DeleteCommentUseCase
import com.cleveronion.common.annotation.UseCase

/**
 * 评论应用服务
 * 
 * 协调评论相关的用例，提供统一的评论管理接口
 * 处理评论创建、回复、删除等功能
 */
@UseCase
class CommentApplicationService(
    private val createCommentUseCase: CreateCommentUseCase,
    private val replyToCommentUseCase: ReplyToCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase
) {
    
    /**
     * 创建评论
     * 
     * 处理在文章下创建新评论
     * 
     * @param command 创建评论命令
     * @return 创建的评论DTO
     */
    suspend fun createComment(command: CreateCommentCommand): CommentDto {
        return createCommentUseCase.execute(command)
    }
    
    /**
     * 回复评论
     * 
     * 处理对现有评论的回复
     * 
     * @param command 回复评论命令
     * @return 创建的回复评论DTO
     */
    suspend fun replyToComment(command: ReplyToCommentCommand): CommentDto {
        return replyToCommentUseCase.execute(command)
    }
    
    /**
     * 删除评论
     * 
     * 处理评论删除，包括权限验证
     * 
     * @param command 删除评论命令
     */
    suspend fun deleteComment(command: DeleteCommentCommand) {
        deleteCommentUseCase.execute(command)
    }
    
    /**
     * 为文章添加评论
     * 
     * 便捷方法，用于在指定文章下添加评论
     * 
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param content 评论内容
     * @return 创建的评论DTO
     */
    suspend fun addCommentToArticle(
        articleId: Long,
        userId: Long,
        content: String
    ): CommentDto {
        val command = CreateCommentCommand(
            articleId = articleId,
            userId = userId,
            content = content
        )
        return createCommentUseCase.execute(command)
    }
    
    /**
     * 回复指定评论
     * 
     * 便捷方法，用于回复特定的评论
     * 
     * @param parentCommentId 父评论ID
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param content 回复内容
     * @return 创建的回复评论DTO
     */
    suspend fun replyToSpecificComment(
        parentCommentId: Long,
        articleId: Long,
        userId: Long,
        content: String
    ): CommentDto {
        val command = ReplyToCommentCommand(
            parentCommentId = parentCommentId,
            articleId = articleId,
            userId = userId,
            content = content
        )
        return replyToCommentUseCase.execute(command)
    }
    
    /**
     * 删除用户评论
     * 
     * 便捷方法，用于删除用户的评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    suspend fun deleteUserComment(commentId: Long, userId: Long) {
        val command = DeleteCommentCommand(
            commentId = commentId,
            userId = userId
        )
        deleteCommentUseCase.execute(command)
    }
    
    /**
     * 管理员删除评论
     * 
     * 便捷方法，用于管理员删除任意评论
     * 
     * @param commentId 评论ID
     * @param adminUserId 管理员用户ID
     */
    suspend fun adminDeleteComment(commentId: Long, adminUserId: Long) {
        val command = DeleteCommentCommand(
            commentId = commentId,
            userId = adminUserId
        )
        deleteCommentUseCase.execute(command)
    }
}