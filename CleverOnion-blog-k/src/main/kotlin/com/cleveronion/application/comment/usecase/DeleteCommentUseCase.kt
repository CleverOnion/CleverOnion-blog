package com.cleveronion.application.comment.usecase

import com.cleveronion.application.comment.command.DeleteCommentCommand
import com.cleveronion.domain.comment.port.CommentRepositoryPort
import com.cleveronion.domain.comment.valueobject.CommentId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.common.annotation.UseCase

/**
 * 删除评论用例
 * 
 * 实现评论删除的业务逻辑，包括权限验证和软删除操作
 */
@UseCase
class DeleteCommentUseCase(
    private val commentRepository: CommentRepositoryPort
) {
    
    /**
     * 执行删除评论用例
     * 
     * @param command 删除评论命令
     * @throws EntityNotFoundException 当评论不存在时
     * @throws UnauthorizedOperationException 当用户无权限删除评论时
     */
    suspend fun execute(command: DeleteCommentCommand) {
        val commentId = CommentId.of(command.commentId)
        val userId = UserId.of(command.userId)
        
        // 查找评论
        val comment = commentRepository.findById(commentId)
            ?: throw EntityNotFoundException.of("Comment", command.commentId)
        
        // 验证权限
        if (!comment.canBeDeletedBy(userId)) {
            throw UnauthorizedOperationException("User cannot delete this comment")
        }
        
        // 软删除评论
        val deletedComment = comment.delete()
        commentRepository.save(deletedComment)
    }
}