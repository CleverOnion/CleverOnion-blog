package com.cleveronion.domain.article.aggregate

import com.cleveronion.domain.article.valueobject.*
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.valueobject.TagId
import com.cleveronion.domain.shared.aggregate.AggregateRoot
import com.cleveronion.domain.shared.valueobject.CreatedAt
import com.cleveronion.domain.shared.valueobject.UpdatedAt
import com.cleveronion.domain.shared.event.ArticlePublishedEvent
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException

/**
 * 文章聚合根
 * 
 * 文章聚合的核心实体，包含文章的所有业务逻辑和状态管理。
 * 负责维护文章的一致性和业务规则。
 */
class Article private constructor(
    private val id: ArticleId,
    private var title: ArticleTitle,
    private var content: ArticleContent,
    private var status: ArticleStatus,
    private val authorId: UserId,
    private var tags: MutableSet<TagId>,
    private var viewCount: Int,
    private val createdAt: CreatedAt,
    private var updatedAt: UpdatedAt
) : AggregateRoot<ArticleId>() {
    
    companion object {
        /**
         * 创建新文章
         * 
         * @param title 文章标题
         * @param content 文章内容
         * @param authorId 作者ID
         * @param tags 标签集合，默认为空
         * @return 新创建的文章实例
         */
        fun create(
            title: ArticleTitle,
            content: ArticleContent,
            authorId: UserId,
            tags: Set<TagId> = emptySet()
        ): Article {
            val now = CreatedAt.now()
            return Article(
                id = ArticleId.generate(),
                title = title,
                content = content,
                status = ArticleStatus.DRAFT,
                authorId = authorId,
                tags = tags.toMutableSet(),
                viewCount = 0,
                createdAt = now,
                updatedAt = UpdatedAt.now()
            ).also { article ->
                article.ensureBusinessRules()
            }
        }
        
        /**
         * 从现有数据重建文章聚合
         * 
         * 用于从数据库或其他持久化存储中重建聚合实例
         */
        fun reconstruct(
            id: ArticleId,
            title: ArticleTitle,
            content: ArticleContent,
            status: ArticleStatus,
            authorId: UserId,
            tags: Set<TagId>,
            viewCount: Int,
            createdAt: CreatedAt,
            updatedAt: UpdatedAt
        ): Article {
            return Article(
                id = id,
                title = title,
                content = content,
                status = status,
                authorId = authorId,
                tags = tags.toMutableSet(),
                viewCount = viewCount,
                createdAt = createdAt,
                updatedAt = updatedAt
            ).also { article ->
                article.ensureBusinessRules()
            }
        }
        
        /**
         * 标签数量限制
         */
        const val MAX_TAGS = 10
    }
    
    /**
     * 发布文章
     * 
     * 将文章状态从草稿变更为已发布，并触发文章发布事件
     * 
     * @return 当前文章实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果文章已经发布或状态不允许发布
     */
    fun publish(): Article {
        if (status == ArticleStatus.PUBLISHED) {
            throw BusinessRuleViolationException("Article is already published")
        }
        
        if (!status.canTransitionTo(ArticleStatus.PUBLISHED)) {
            throw BusinessRuleViolationException("Cannot publish article in ${status.name} status")
        }
        
        status = ArticleStatus.PUBLISHED
        updatedAt = UpdatedAt.now()
        
        // 发布领域事件
        addDomainEvent(
            ArticlePublishedEvent(
                articleId = id.value,
                authorId = authorId.value,
                title = title.value
            )
        )
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 归档文章
     * 
     * 将文章状态变更为已归档
     * 
     * @return 当前文章实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果文章已经归档或状态不允许归档
     */
    fun archive(): Article {
        if (status == ArticleStatus.ARCHIVED) {
            throw BusinessRuleViolationException("Article is already archived")
        }
        
        if (!status.canTransitionTo(ArticleStatus.ARCHIVED)) {
            throw BusinessRuleViolationException("Cannot archive article in ${status.name} status")
        }
        
        status = ArticleStatus.ARCHIVED
        updatedAt = UpdatedAt.now()
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 更新文章内容
     * 
     * @param newTitle 新标题，如果为null则不更新
     * @param newContent 新内容，如果为null则不更新
     * @return 当前文章实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果文章状态不允许编辑
     */
    fun updateContent(newTitle: ArticleTitle? = null, newContent: ArticleContent? = null): Article {
        if (!canBeEdited()) {
            throw BusinessRuleViolationException("Cannot edit article in ${status.name} status")
        }
        
        var hasChanges = false
        
        newTitle?.let {
            if (title != it) {
                title = it
                hasChanges = true
            }
        }
        
        newContent?.let {
            if (content != it) {
                content = it
                hasChanges = true
            }
        }
        
        if (hasChanges) {
            updatedAt = UpdatedAt.now()
            ensureBusinessRules()
        }
        
        return this
    }
    
    /**
     * 添加标签
     * 
     * @param tagId 要添加的标签ID
     * @return 当前文章实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果标签数量超过限制或标签已存在
     */
    fun addTag(tagId: TagId): Article {
        if (tags.size >= MAX_TAGS) {
            throw BusinessRuleViolationException("Article cannot have more than $MAX_TAGS tags")
        }
        
        if (tagId in tags) {
            throw BusinessRuleViolationException("Tag already exists in article")
        }
        
        tags.add(tagId)
        updatedAt = UpdatedAt.now()
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 移除标签
     * 
     * @param tagId 要移除的标签ID
     * @return 当前文章实例（支持链式调用）
     */
    fun removeTag(tagId: TagId): Article {
        if (tags.remove(tagId)) {
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 批量设置标签
     * 
     * @param newTags 新的标签集合
     * @return 当前文章实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果标签数量超过限制
     */
    fun setTags(newTags: Set<TagId>): Article {
        if (newTags.size > MAX_TAGS) {
            throw BusinessRuleViolationException("Article cannot have more than $MAX_TAGS tags")
        }
        
        if (tags != newTags.toMutableSet()) {
            tags.clear()
            tags.addAll(newTags)
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 增加浏览次数
     * 
     * @return 当前文章实例（支持链式调用）
     */
    fun incrementViewCount(): Article {
        viewCount++
        return this
    }
    
    /**
     * 检查指定用户是否可以编辑此文章
     * 
     * @param userId 用户ID
     * @return 如果用户可以编辑则返回true，否则返回false
     */
    fun canBeEditedBy(userId: UserId): Boolean {
        return authorId == userId && canBeEdited()
    }
    
    /**
     * 检查文章是否可以被编辑
     * 
     * @return 如果文章可以编辑则返回true，否则返回false
     */
    fun canBeEdited(): Boolean {
        return status in ArticleStatus.getEditableStatuses()
    }
    
    /**
     * 检查文章是否对公众可见
     * 
     * @return 如果文章对公众可见则返回true，否则返回false
     */
    fun isPubliclyVisible(): Boolean {
        return status.isPubliclyVisible()
    }
    
    /**
     * 检查文章是否包含指定标签
     * 
     * @param tagId 标签ID
     * @return 如果包含指定标签则返回true，否则返回false
     */
    fun hasTag(tagId: TagId): Boolean {
        return tagId in tags
    }
    
    // 实现聚合根基类的抽象方法
    override fun ensureBusinessRules() {
        // 验证已发布文章必须有内容
        if (status == ArticleStatus.PUBLISHED && content.isEmpty()) {
            throw BusinessRuleViolationException("Published article must have content")
        }
        
        // 验证标签数量限制
        if (tags.size > MAX_TAGS) {
            throw BusinessRuleViolationException("Article cannot have more than $MAX_TAGS tags")
        }
        
        // 验证浏览次数不能为负数
        if (viewCount < 0) {
            throw BusinessRuleViolationException("View count cannot be negative")
        }
        
        // 验证更新时间不能早于创建时间
        if (!updatedAt.isAfterCreation(createdAt) && updatedAt.value != createdAt.value) {
            throw BusinessRuleViolationException("Updated time cannot be before created time")
        }
    }
    
    override fun getId(): ArticleId = id
    
    // Getters - 提供对内部状态的只读访问
    fun getTitle(): ArticleTitle = title
    fun getContent(): ArticleContent = content
    fun getStatus(): ArticleStatus = status
    fun getAuthorId(): UserId = authorId
    fun getTags(): Set<TagId> = tags.toSet()
    fun getViewCount(): Int = viewCount
    fun getCreatedAt(): CreatedAt = createdAt
    fun getUpdatedAt(): UpdatedAt = updatedAt
}