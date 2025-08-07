package com.cleveronion.application.article.usecase

import com.cleveronion.application.article.command.DeleteArticleCommand
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.port.TagRepositoryPort
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.common.annotation.UseCase

/**
 * 删除文章用例
 * 
 * 实现文章删除的业务逻辑，包括权限验证和标签计数更新
 */
@UseCase
class DeleteArticleUseCase(
    private val articleRepository: ArticleRepositoryPort,
    private val tagRepository: TagRepositoryPort
) {
    
    /**
     * 执行删除文章用例
     * 
     * @param command 删除文章命令
     * @throws EntityNotFoundException 当文章不存在时
     * @throws UnauthorizedOperationException 当用户无权限删除文章时
     */
    suspend fun execute(command: DeleteArticleCommand) {
        val articleId = ArticleId.of(command.articleId)
        val userId = UserId.of(command.userId)
        
        // 查找文章
        val article = articleRepository.findById(articleId)
            ?: throw EntityNotFoundException.of("Article", command.articleId)
        
        // 验证权限
        if (!article.canBeEditedBy(userId)) {
            throw UnauthorizedOperationException("User cannot delete this article")
        }
        
        // 减少标签的文章计数
        val tagIds = article.getTags()
        tagIds.forEach { tagId ->
            tagRepository.findById(tagId)?.let { tag ->
                val updatedTag = tag.decrementArticleCount()
                tagRepository.save(updatedTag)
            }
        }
        
        // 删除文章
        val deleted = articleRepository.deleteById(articleId)
        if (!deleted) {
            throw EntityNotFoundException.of("Article", command.articleId)
        }
    }
}