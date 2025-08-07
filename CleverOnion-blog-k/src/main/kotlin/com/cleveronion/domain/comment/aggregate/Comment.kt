package com.cleveronion.domain.comment.aggregate

import com.cleveronion.domain.comment.valueobject.*
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.shared.aggregate.AggregateRoot
import com.cleveronion.domain.shared.valueobject.CreatedAt
import com.cleveronion.domain.shared.valueobject.UpdatedAt
import com.cleveronion.domain.shared.event.CommentUpdatedEvent
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException

/**
 * 评论聚合根
 * 
 * 评论聚合的核心实体，包含评论的所有业务逻辑和状态管理。
 * 负责维护评论的一致性和业务规则。
 */
class Comment private constructor(
    private val id: CommentId,
    private val articleId: ArticleId,
    private val userId: UserId,
    private var content: CommentContent,
    private val parentId: CommentId?,
    private var isDeleted: Boolean,
    private val createdAt: CreatedAt,
    private var updatedAt: UpdatedAt
) : AggregateRoot<CommentId>() {
    
    companion object {
        /**
         * 创建新评论
         * 
         * @param articleId 文章ID
         * @param userId 用户ID
         * @param content 评论内容
         * @param parentId 父评论ID，如果为null则为顶级评论
         * @return 新创建的评论实例
         */
        fun create(
            articleId: ArticleId,
            userId: UserId,
            content: CommentContent,
            parentId: CommentId? = null
        ): Comment {
            val now = CreatedAt.now()
            return Comment(
                id = CommentId.generate(),
                articleId = articleId,
                userId = userId,
                content = content,
                parentId = parentId,
                isDeleted = false,
                createdAt = now,
                updatedAt = UpdatedAt.now()
            ).also { comment ->
                comment.ensureBusinessRules()
            }
        }
        
        /**
         * 从现有数据重建评论聚合
         * 
         * 用于从数据库或其他持久化存储中重建聚合实例
         */
        fun reconstruct(
            id: CommentId,
            articleId: ArticleId,
            userId: UserId,
            content: CommentContent,
            parentId: CommentId?,
            isDeleted: Boolean,
            createdAt: CreatedAt,
            updatedAt: UpdatedAt
        ): Comment {
            return Comment(
                id = id,
                articleId = articleId,
                userId = userId,
                content = content,
                parentId = parentId,
                isDeleted = isDeleted,
                createdAt = createdAt,
                updatedAt = updatedAt
            ).also { comment ->
                comment.ensureBusinessRules()
            }
        }
    }    
  
  /**
     * 更新评论内容
     * 
     * @param newContent 新的评论内容
     * @return 当前评论实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果评论已删除或内容无效
     */
    fun updateContent(newContent: CommentContent): Comment {
        if (isDeleted) {
            throw BusinessRuleViolationException("Cannot update deleted comment")
        }
        
        if (content != newContent) {
            content = newContent
            updatedAt = UpdatedAt.now()
            
            // 发布评论更新事件
            addDomainEvent(
                CommentUpdatedEvent(
                    commentId = id.value,
                    articleId = articleId.value,
                    userId = userId.value
                )
            )
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 软删除评论
     * 
     * 将评论标记为已删除，但不从数据库中物理删除
     * 
     * @return 当前评论实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果评论已删除
     */
    fun delete(): Comment {
        if (isDeleted) {
            throw BusinessRuleViolationException("Comment is already deleted")
        }
        
        isDeleted = true
        updatedAt = UpdatedAt.now()
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 恢复已删除的评论
     * 
     * @return 当前评论实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果评论未删除
     */
    fun restore(): Comment {
        if (!isDeleted) {
            throw BusinessRuleViolationException("Comment is not deleted")
        }
        
        isDeleted = false
        updatedAt = UpdatedAt.now()
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 检查是否为回复评论
     * 
     * @return 如果是回复评论返回true，否则返回false
     */
    fun isReply(): Boolean = parentId != null
    
    /**
     * 检查是否为顶级评论
     * 
     * @return 如果是顶级评论返回true，否则返回false
     */
    fun isTopLevel(): Boolean = parentId == null
    
    /**
     * 检查指定用户是否可以编辑此评论
     * 
     * @param userId 用户ID
     * @return 如果用户可以编辑则返回true，否则返回false
     */
    fun canBeEditedBy(userId: UserId): Boolean {
        return this.userId == userId && !isDeleted
    }
    
    /**
     * 检查指定用户是否可以删除此评论
     * 
     * @param userId 用户ID
     * @return 如果用户可以删除则返回true，否则返回false
     */
    fun canBeDeletedBy(userId: UserId): Boolean {
        return this.userId == userId && !isDeleted
    }
    
    /**
     * 检查评论是否可见
     * 
     * @return 如果评论可见返回true，否则返回false
     */
    fun isVisible(): Boolean = !isDeleted
    
    /**
     * 检查评论是否属于指定文章
     * 
     * @param articleId 文章ID
     * @return 如果属于指定文章返回true，否则返回false
     */
    fun belongsToArticle(articleId: ArticleId): Boolean {
        return this.articleId == articleId
    }
    
    /**
     * 检查评论是否由指定用户创建
     * 
     * @param userId 用户ID
     * @return 如果由指定用户创建返回true，否则返回false
     */
    fun isCreatedBy(userId: UserId): Boolean {
        return this.userId == userId
    }
    
    /**
     * 检查评论是否为指定评论的回复
     * 
     * @param commentId 评论ID
     * @return 如果是指定评论的回复返回true，否则返回false
     */
    fun isReplyTo(commentId: CommentId): Boolean {
        return parentId == commentId
    }
    
    /**
     * 获取评论的层级深度
     * 
     * 顶级评论深度为0，回复评论深度为1
     * 
     * @return 评论层级深度
     */
    fun getDepth(): Int {
        return if (isTopLevel()) 0 else 1
    }
    
    // 实现聚合根基类的抽象方法
    override fun ensureBusinessRules() {
        // 验证评论内容不能为空
        if (content.isEmpty()) {
            throw BusinessRuleViolationException("Comment content cannot be empty")
        }
        
        // 验证评论内容长度
        if (content.isTooLong()) {
            throw BusinessRuleViolationException("Comment content is too long")
        }
        
        // 验证更新时间不能早于创建时间
        if (!updatedAt.isAfterCreation(createdAt) && updatedAt.value != createdAt.value) {
            throw BusinessRuleViolationException("Updated time cannot be before created time")
        }
    }
    
    override fun getId(): CommentId = id
    
    // Getters - 提供对内部状态的只读访问
    fun getArticleId(): ArticleId = articleId
    fun getUserId(): UserId = userId
    fun getContent(): CommentContent = content
    fun getParentId(): CommentId? = parentId
    fun isDeleted(): Boolean = isDeleted
    fun getCreatedAt(): CreatedAt = createdAt
    fun getUpdatedAt(): UpdatedAt = updatedAt
}