package com.cleveronion.application.article.usecase

import com.cleveronion.application.article.command.PublishArticleCommand
import com.cleveronion.application.article.dto.ArticleDto
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.port.TagRepositoryPort
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.application.shared.port.EventPublisherPort
import com.cleveronion.common.annotation.UseCase

/**
 * 发布文章用例
 * 
 * 实现文章发布的业务逻辑，包括权限验证和事件发布
 */
@UseCase
class PublishArticleUseCase(
    private val articleRepository: ArticleRepositoryPort,
    private val tagRepository: TagRepositoryPort,
    private val eventPublisher: EventPublisherPort
) {
    
    /**
     * 执行发布文章用例
     * 
     * @param command 发布文章命令
     * @return 发布的文章DTO
     * @throws EntityNotFoundException 当文章不存在时
     * @throws UnauthorizedOperationException 当用户无权限发布文章时
     */
    suspend fun execute(command: PublishArticleCommand): ArticleDto {
        val articleId = ArticleId.of(command.articleId)
        val userId = UserId.of(command.userId)
        
        // 查找文章
        val article = articleRepository.findById(articleId)
            ?: throw EntityNotFoundException.of("Article", command.articleId)
        
        // 验证权限
        if (!article.canBeEditedBy(userId)) {
            throw UnauthorizedOperationException("User cannot publish this article")
        }
        
        // 发布文章
        val publishedArticle = article.publish()
        val savedArticle = articleRepository.save(publishedArticle)
        
        // 发布领域事件
        val domainEvents = savedArticle.getDomainEvents()
        if (domainEvents.isNotEmpty()) {
            eventPublisher.publishAll(domainEvents)
            savedArticle.clearDomainEvents()
        }
        
        // 获取标签信息用于DTO
        val tags = if (savedArticle.getTags().isNotEmpty()) {
            savedArticle.getTags().mapNotNull { tagId ->
                tagRepository.findById(tagId)
            }
        } else {
            emptyList()
        }
        
        return ArticleDto.fromDomain(savedArticle, tags)
    }
}