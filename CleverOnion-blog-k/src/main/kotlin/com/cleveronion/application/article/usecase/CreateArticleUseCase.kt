package com.cleveronion.application.article.usecase

import com.cleveronion.application.article.command.CreateArticleCommand
import com.cleveronion.application.article.dto.ArticleDto
import com.cleveronion.domain.article.aggregate.Article
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleContent
import com.cleveronion.domain.article.valueobject.ArticleTitle
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.port.TagRepositoryPort
import com.cleveronion.domain.tag.aggregate.Tag
import com.cleveronion.domain.tag.valueobject.TagId
import com.cleveronion.domain.tag.valueobject.TagName
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.annotation.UseCase

/**
 * 创建文章用例
 * 
 * 实现文章创建的业务逻辑，包括验证用户权限、处理标签和保存文章
 */
@UseCase
class CreateArticleUseCase(
    private val articleRepository: ArticleRepositoryPort,
    private val userRepository: UserRepositoryPort,
    private val tagRepository: TagRepositoryPort
) {
    
    /**
     * 执行创建文章用例
     * 
     * @param command 创建文章命令
     * @return 创建的文章DTO
     * @throws EntityNotFoundException 当用户不存在时
     * @throws ValidationException 当输入验证失败时
     */
    suspend fun execute(command: CreateArticleCommand): ArticleDto {
        // 验证用户存在
        val authorId = UserId.of(command.authorId)
        val user = userRepository.findById(authorId)
            ?: throw EntityNotFoundException.of("User", command.authorId)
        
        // 验证用户是否活跃
        if (!user.isActive()) {
            throw ValidationException("Inactive user cannot create articles")
        }
        
        // 处理标签
        val tagIds = processTagsForArticle(command.tags)
        
        // 创建文章内容值对象
        val articleContent = try {
            ArticleContent.fromMarkdown(command.contentMd)
        } catch (e: Exception) {
            throw ValidationException("Invalid markdown content: ${e.message}")
        }
        
        // 创建文章
        val article = Article.create(
            title = ArticleTitle(command.title),
            content = articleContent,
            authorId = authorId,
            tags = tagIds
        )
        
        // 保存文章
        val savedArticle = articleRepository.save(article)
        
        // 获取标签信息用于DTO
        val tags = if (tagIds.isNotEmpty()) {
            tagIds.mapNotNull { tagId ->
                tagRepository.findById(tagId)
            }
        } else {
            emptyList()
        }
        
        return ArticleDto.fromDomain(savedArticle, tags)
    }
    
    /**
     * 处理文章标签
     * 
     * 对于不存在的标签，会自动创建新标签
     * 
     * @param tagNames 标签名称列表
     * @return 标签ID集合
     */
    private suspend fun processTagsForArticle(tagNames: List<String>): Set<TagId> {
        if (tagNames.isEmpty()) {
            return emptySet()
        }
        
        return tagNames.map { tagName ->
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