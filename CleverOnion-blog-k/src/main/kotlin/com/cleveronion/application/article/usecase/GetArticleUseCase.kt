package com.cleveronion.application.article.usecase

import com.cleveronion.application.article.query.GetArticleQuery
import com.cleveronion.application.article.dto.ArticleDto
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.port.TagRepositoryPort
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.common.annotation.UseCase

/**
 * 获取文章用例
 * 
 * 实现文章查询的业务逻辑，包括权限验证和浏览次数更新
 */
@UseCase
class GetArticleUseCase(
    private val articleRepository: ArticleRepositoryPort,
    private val tagRepository: TagRepositoryPort
) {
    
    /**
     * 执行获取文章用例
     * 
     * @param query 获取文章查询
     * @return 文章DTO
     * @throws EntityNotFoundException 当文章不存在时
     * @throws UnauthorizedOperationException 当用户无权限查看文章时
     */
    suspend fun execute(query: GetArticleQuery): ArticleDto {
        val articleId = ArticleId.of(query.articleId)
        
        // 查找文章
        val article = articleRepository.findById(articleId)
            ?: throw EntityNotFoundException.of("Article", query.articleId)
        
        // 检查文章可见性
        if (!article.isPubliclyVisible()) {
            if (!query.includeUnpublished) {
                throw EntityNotFoundException.of("Article", query.articleId)
            }
            
            // 如果要查看未发布文章，需要验证权限
            query.requestUserId?.let { userId ->
                val requestUserId = UserId.of(userId)
                if (!article.canBeEditedBy(requestUserId)) {
                    throw UnauthorizedOperationException("User cannot view unpublished article")
                }
            } ?: throw UnauthorizedOperationException("Authentication required to view unpublished article")
        }
        
        // 增加浏览次数（只对公开可见的文章）
        val updatedArticle = if (article.isPubliclyVisible()) {
            val articleWithIncrementedView = article.incrementViewCount()
            articleRepository.save(articleWithIncrementedView)
        } else {
            article
        }
        
        // 获取标签信息用于DTO
        val tags = if (updatedArticle.getTags().isNotEmpty()) {
            updatedArticle.getTags().mapNotNull { tagId ->
                tagRepository.findById(tagId)
            }
        } else {
            emptyList()
        }
        
        return ArticleDto.fromDomain(updatedArticle, tags)
    }
}