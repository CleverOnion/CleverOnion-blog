package com.cleveronion.domain.article.service

import com.cleveronion.domain.article.aggregate.Article
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.article.valueobject.ArticleStatus
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.valueobject.TagId
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException

/**
 * 文章领域服务
 * 
 * 封装跨文章聚合的业务逻辑，处理复杂的业务规则和操作。
 * 领域服务用于处理不属于单个聚合的业务逻辑。
 */
class ArticleDomainService(
    private val articleRepository: ArticleRepositoryPort
) {
    
    /**
     * 检查用户是否有权限操作指定文章
     * 
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param requireEditPermission 是否需要编辑权限，默认为false
     * @return 如果有权限返回文章实例
     * @throws EntityNotFoundException 如果文章不存在
     * @throws UnauthorizedOperationException 如果用户没有权限
     */
    suspend fun checkUserPermission(
        articleId: ArticleId,
        userId: UserId,
        requireEditPermission: Boolean = false
    ): Article {
        val article = articleRepository.findById(articleId)
            ?: throw EntityNotFoundException("Article not found: ${articleId.value}")
        
        // 检查是否为文章作者
        if (article.getAuthorId() != userId) {
            throw UnauthorizedOperationException("User does not have permission to access this article")
        }
        
        // 如果需要编辑权限，检查文章是否可编辑
        if (requireEditPermission && !article.canBeEditedBy(userId)) {
            throw UnauthorizedOperationException("Article cannot be edited in current status")
        }
        
        return article
    }
    
    /**
     * 验证文章发布的业务规则
     * 
     * @param article 要发布的文章
     * @throws BusinessRuleViolationException 如果不满足发布条件
     */
    fun validatePublishRules(article: Article) {
        // 检查文章内容是否为空
        if (article.getContent().isEmpty()) {
            throw BusinessRuleViolationException("Cannot publish article with empty content")
        }
        
        // 检查文章标题是否为空
        if (article.getTitle().isEmpty()) {
            throw BusinessRuleViolationException("Cannot publish article with empty title")
        }
        
        // 检查文章状态是否允许发布
        if (!article.getStatus().canTransitionTo(ArticleStatus.PUBLISHED)) {
            throw BusinessRuleViolationException("Cannot publish article in ${article.getStatus().name} status")
        }
    }
    
    /**
     * 验证文章归档的业务规则
     * 
     * @param article 要归档的文章
     * @throws BusinessRuleViolationException 如果不满足归档条件
     */
    fun validateArchiveRules(article: Article) {
        // 检查文章状态是否允许归档
        if (!article.getStatus().canTransitionTo(ArticleStatus.ARCHIVED)) {
            throw BusinessRuleViolationException("Cannot archive article in ${article.getStatus().name} status")
        }
    }
    
    /**
     * 检查标签是否可以从文章中移除
     * 
     * 这里可以添加复杂的业务规则，比如某些系统标签不能移除等
     * 
     * @param article 文章实例
     * @param tagId 要移除的标签ID
     * @throws BusinessRuleViolationException 如果标签不能移除
     */
    fun validateTagRemoval(article: Article, tagId: TagId) {
        if (!article.hasTag(tagId)) {
            throw BusinessRuleViolationException("Tag does not exist in article")
        }
        
        // 这里可以添加更多业务规则
        // 例如：某些系统标签不能移除
        // 例如：如果是最后一个标签且文章已发布，不能移除
    }
    
    /**
     * 检查是否可以删除文章
     * 
     * @param article 要删除的文章
     * @param userId 执行删除的用户ID
     * @throws BusinessRuleViolationException 如果不能删除
     * @throws UnauthorizedOperationException 如果用户没有权限
     */
    fun validateArticleDeletion(article: Article, userId: UserId) {
        // 检查用户权限
        if (article.getAuthorId() != userId) {
            throw UnauthorizedOperationException("Only article author can delete the article")
        }
        
        // 检查文章状态 - 已发布的文章不能直接删除，需要先归档
        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            throw BusinessRuleViolationException("Published articles must be archived before deletion")
        }
    }
    
    /**
     * 计算文章的相关性评分
     * 
     * 基于浏览次数、创建时间等因素计算文章的相关性评分
     * 
     * @param article 文章实例
     * @return 相关性评分（0-100）
     */
    fun calculateRelevanceScore(article: Article): Double {
        val viewCount = article.getViewCount()
        val tagCount = article.getTags().size
        val daysSinceCreation = java.time.Duration.between(
            article.getCreatedAt().value,
            java.time.Instant.now()
        ).toDays()
        
        // 简单的评分算法：浏览次数权重60%，标签数量权重20%，时间新鲜度权重20%
        val viewScore = minOf(viewCount.toDouble() / 1000 * 60, 60.0)
        val tagScore = minOf(tagCount.toDouble() / Article.MAX_TAGS * 20, 20.0)
        val freshnessScore = maxOf(20.0 - daysSinceCreation * 0.1, 0.0)
        
        return viewScore + tagScore + freshnessScore
    }
    
    /**
     * 检查文章是否为重复内容
     * 
     * 这是一个简化的实现，实际项目中可能需要更复杂的算法
     * 
     * @param article 要检查的文章
     * @return 如果检测到重复内容返回true，否则返回false
     */
    suspend fun isDuplicateContent(article: Article): Boolean {
        // 简化实现：检查是否有相同标题的文章
        val articlesWithSameTitle = articleRepository.search(
            keyword = article.getTitle().value,
            pagination = com.cleveronion.domain.shared.valueobject.Pagination(page = 1, pageSize = 10),
            status = ArticleStatus.PUBLISHED
        )
        
        return articlesWithSameTitle.any { existingArticle ->
            existingArticle.getId() != article.getId() &&
            existingArticle.getTitle().value.equals(article.getTitle().value, ignoreCase = true)
        }
    }
    
    /**
     * 获取文章的推荐标签
     * 
     * 基于文章内容分析推荐相关标签
     * 
     * @param article 文章实例
     * @return 推荐的标签ID集合
     */
    suspend fun getRecommendedTags(article: Article): Set<TagId> {
        // 简化实现：基于文章内容中的关键词推荐标签
        // 实际项目中可能需要使用NLP或机器学习算法
        
        val content = article.getContent().markdown.lowercase()
        val recommendedTags = mutableSetOf<TagId>()
        
        // 这里可以实现基于关键词匹配的标签推荐逻辑
        // 例如：如果内容包含"kotlin"，推荐Kotlin标签
        // 例如：如果内容包含"spring"，推荐Spring标签
        
        return recommendedTags
    }
    
    /**
     * 验证文章内容的质量
     * 
     * @param article 要验证的文章
     * @return 质量评分（0-100）和改进建议列表
     */
    fun validateContentQuality(article: Article): Pair<Double, List<String>> {
        val suggestions = mutableListOf<String>()
        var score = 100.0
        
        val content = article.getContent()
        val title = article.getTitle()
        
        // 检查标题长度
        if (title.length() < 10) {
            suggestions.add("标题过短，建议至少10个字符")
            score -= 10
        }
        
        // 检查内容长度
        if (content.markdownLength() < 100) {
            suggestions.add("内容过短，建议至少100个字符")
            score -= 20
        }
        
        // 检查是否有标签
        if (article.getTags().isEmpty()) {
            suggestions.add("建议添加相关标签以提高文章可发现性")
            score -= 10
        }
        
        // 检查内容结构（是否有标题）
        if (!content.markdown.contains("#")) {
            suggestions.add("建议使用标题结构化内容")
            score -= 5
        }
        
        return Pair(maxOf(score, 0.0), suggestions)
    }
}