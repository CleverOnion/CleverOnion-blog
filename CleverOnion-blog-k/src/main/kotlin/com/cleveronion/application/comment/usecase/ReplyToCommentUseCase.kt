package com.cleveronion.application.comment.usecase

import com.cleveronion.application.comment.command.ReplyToCommentCommand
import com.cleveronion.application.comment.dto.CommentDto
import com.cleveronion.domain.comment.aggregate.Comment
import com.cleveronion.domain.comment.port.CommentRepositoryPort
import com.cleveronion.domain.comment.valueobject.CommentId
import com.cleveronion.domain.comment.valueobject.CommentContent
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.annotation.UseCase

/**
 * 回复评论用例
 * 
 * 实现评论回复的业务逻辑，包括验证父评论有效性和权限检查
 */
@UseCase
class ReplyToCommentUseCase(
    private val commentRepository: CommentRepositoryPort,
    private val articleRepository: ArticleRepositoryPort,
    private val userRepository: UserRepositoryPort
) {
    
    /**
     * 执行回复评论用例
     * 
     * @param command 回复评论命令
     * @return 创建的回复评论DTO
     * @throws EntityNotFoundException 当文章、用户或父评论不存在时
     * @throws ValidationException 当输入验证失败时
     */
    suspend fun execute(command: ReplyToCommentCommand): CommentDto {
        val parentCommentId = CommentId.of(command.parentCommentId)
        val articleId = ArticleId.of(command.articleId)
        val userId = UserId.of(command.userId)
        
        // 验证父评论存在
        val parentComment = commentRepository.findById(parentCommentId)
            ?: throw EntityNotFoundException.of("Comment", command.parentCommentId)
        
        // 验证父评论属于指定文章
        if (!parentComment.belongsToArticle(articleId)) {
            throw ValidationException("Parent comment does not belong to the specified article")
        }
        
        // 验证父评论未被删除
        if (!parentComment.isVisible()) {
            throw ValidationException("Cannot reply to deleted comment")
        }
        
        // 验证不能回复回复（只支持两级评论）
        if (parentComment.isReply()) {
            throw ValidationException("Cannot reply to a reply comment")
        }
        
        // 验证文章存在且可以评论
        val article = articleRepository.findById(articleId)
            ?: throw EntityNotFoundException.of("Article", command.articleId)
        
        if (!article.isPubliclyVisible()) {
            throw ValidationException("Cannot reply to comment on unpublished article")
        }
        
        // 验证用户存在且活跃
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException.of("User", command.userId)
        
        if (!user.isActive()) {
            throw ValidationException("Inactive user cannot reply to comments")
        }
        
        // 创建回复内容
        val content = CommentContent.of(command.content)
        
        // 创建回复评论
        val replyComment = Comment.create(
            articleId = articleId,
            userId = userId,
            content = content,
            parentId = parentCommentId
        )
        
        // 保存回复评论
        val savedComment = commentRepository.save(replyComment)
        
        return CommentDto.fromDomain(savedComment)
    }
}