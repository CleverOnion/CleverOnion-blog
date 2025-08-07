package com.cleveronion.application.article.usecase

import com.cleveronion.application.article.command.UpdateArticleCommand
import com.cleveronion.application.article.dto.ArticleDto
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.article.valueobject.ArticleContent
import com.cleveronion.domain.article.valueobject.ArticleTitle
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.port.TagRepositoryPort
import com.cleveronion.domain.tag.aggregate.Tag
import com.cleveronion.domain.tag.valueobject.TagId
import com.cleveronion.domain.tag.valueobject.TagName
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.annotation.UseCase

/**
 * 更新文章用例
 * 
 * 实现文章更新的业务逻辑，包括权限验证、内容更新和标签管理
 */
@UseCase
class UpdateArticleUseCase(
    private val articleRepository: ArticleRepositoryPort,
    private val tagRepository: TagRepositoryPort
) {
    
    /**
     * 执行更新文章用例
     * 
     * @param command 更新文章命令
     * @return 更新的文章DTO
     * @throws EntityNotFoundException 当文章不存在时
     * @throws UnauthorizedOperationException 当用户无权限更新文章时
     * @throws ValidationException 当输入验证失败时
     */
    suspend fun execute(command: UpdateArticleCommand): ArticleDto {
        val articleId = ArticleId.of(command.articleId)
        val userId = UserId.of(command.userId)
        
        // 查找文章
        val article = articleRepository.findById(articleId)
            ?: throw EntityNotFoundException.of("Article", command.articleId)
        
        // 验证权限
        if (!article.canBeEditedBy(userId)) {
            throw UnauthorizedOperationException("User cannot update this article")
        }
        
        // 更新标题和内容
        var updatedArticle = article
        
        if (command.title != null || command.contentMd != null) {
            val newTitle = command.title?.let { ArticleTitle(it) }
            val newContent = command.contentMd?.let { 
                try {
                    ArticleContent.fromMarkdown(it)
                } catch (e: Exception) {
                    throw ValidationException("Invalid markdown content: ${e.message}")
                }
            }
            
            updatedArticle = updatedArticle.updateContent(newTitle, newContent)
        }
        
        // 更新标签
        if (command.tags != null) {
            val oldTagIds = updatedArticle.getTags()
            val newTagIds = processTagsForArticle(command.tags, oldTagIds)
            updatedArticle = updatedArticle.setTags(newTagIds)
        }
        
        // 保存文章
        val savedArticle = articleRepository.save(updatedArticle)
        
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
    
    /**
     * 处理文章标签更新
     * 
     * @param newTagNames 新的标签名称列表
     * @param oldTagIds 旧的标签ID集合
     * @return 新的标签ID集合
     */
    private suspend fun processTagsForArticle(
        newTagNames: List<String>,
        oldTagIds: Set<TagId>
    ): Set<TagId> {
        // 减少旧标签的文章计数
        oldTagIds.forEach { tagId ->
            tagRepository.findById(tagId)?.let { tag ->
                val updatedTag = tag.decrementArticleCount()
                tagRepository.save(updatedTag)
            }
        }
        
        // 处理新标签
        if (newTagNames.isEmpty()) {
            return emptySet()
        }
        
        return newTagNames.map { tagName ->
            val tagNameVO = TagName(tagName.trim())
            
            // 查找现有标签
            val existingTag = tagRepository.findByName(tagNameVO)
            if (existingTag != null) {
                // 增加文章计数
                val updatedTag = existingTag.incrementArticleCount()
                tagRepository.save(updatedTag)
                existingTag.getId()
            } else {
                // 创建新标签
                val newTag = Tag.create(tagNameVO).incrementArticleCount()
                val savedTag = tagRepository.save(newTag)
                savedTag.getId()
            }
        }.toSet()
    }
}