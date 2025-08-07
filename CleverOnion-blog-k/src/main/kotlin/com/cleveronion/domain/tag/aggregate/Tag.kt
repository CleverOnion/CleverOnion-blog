package com.cleveronion.domain.tag.aggregate

import com.cleveronion.domain.tag.valueobject.*
import com.cleveronion.domain.shared.aggregate.AggregateRoot
import com.cleveronion.domain.shared.valueobject.CreatedAt
import com.cleveronion.domain.shared.valueobject.UpdatedAt
import com.cleveronion.domain.shared.event.TagRenamedEvent
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException

/**
 * 标签聚合根
 * 
 * 标签聚合的核心实体，包含标签的所有业务逻辑和状态管理。
 * 负责维护标签的一致性和业务规则。
 */
class Tag private constructor(
    private val id: TagId,
    private var name: TagName,
    private var description: String?,
    private var color: String?,
    private var articleCount: Int,
    private var isActive: Boolean,
    private val createdAt: CreatedAt,
    private var updatedAt: UpdatedAt
) : AggregateRoot<TagId>() {
    
    companion object {
        /**
         * 创建新标签
         * 
         * @param name 标签名称
         * @param description 标签描述，可选
         * @param color 标签颜色（十六进制格式），可选
         * @return 新创建的标签实例
         */
        fun create(
            name: TagName,
            description: String? = null,
            color: String? = null
        ): Tag {
            val now = CreatedAt.now()
            return Tag(
                id = TagId.generate(),
                name = name,
                description = description?.trim()?.takeIf { it.isNotBlank() },
                color = color?.let { validateAndNormalizeColor(it) },
                articleCount = 0,
                isActive = true,
                createdAt = now,
                updatedAt = UpdatedAt.now()
            ).also { tag ->
                tag.ensureBusinessRules()
            }
        }
        
        /**
         * 从现有数据重建标签聚合
         * 
         * 用于从数据库或其他持久化存储中重建聚合实例
         */
        fun reconstruct(
            id: TagId,
            name: TagName,
            description: String?,
            color: String?,
            articleCount: Int,
            isActive: Boolean,
            createdAt: CreatedAt,
            updatedAt: UpdatedAt
        ): Tag {
            return Tag(
                id = id,
                name = name,
                description = description,
                color = color,
                articleCount = articleCount,
                isActive = isActive,
                createdAt = createdAt,
                updatedAt = updatedAt
            ).also { tag ->
                tag.ensureBusinessRules()
            }
        }
        
        /**
         * 默认标签颜色
         */
        private val DEFAULT_COLORS = listOf(
            "#3B82F6", "#EF4444", "#10B981", "#F59E0B", 
            "#8B5CF6", "#EC4899", "#06B6D4", "#84CC16"
        )
        
        /**
         * 验证并规范化颜色值
         */
        private fun validateAndNormalizeColor(color: String): String {
            val normalizedColor = color.trim().uppercase()
            require(normalizedColor.matches(Regex("^#[0-9A-F]{6}$"))) {
                "Color must be in hexadecimal format (#RRGGBB)"
            }
            return normalizedColor
        }
        
        /**
         * 获取随机默认颜色
         */
        fun getRandomDefaultColor(): String {
            return DEFAULT_COLORS.random()
        }
        
        /**
         * 描述最大长度
         */
        const val MAX_DESCRIPTION_LENGTH = 200
    }    
    /*
*
     * 更新标签名称
     * 
     * @param newName 新的标签名称
     * @return 当前标签实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果新名称无效
     */
    fun updateName(newName: TagName): Tag {
        if (name != newName) {
            val oldName = name.value
            name = newName
            updatedAt = UpdatedAt.now()
            
            // 发布标签重命名事件
            addDomainEvent(
                TagRenamedEvent(
                    tagId = id.value,
                    oldName = oldName,
                    newName = newName.value
                )
            )
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 更新标签描述
     * 
     * @param newDescription 新的标签描述，可以为null表示移除描述
     * @return 当前标签实例（支持链式调用）
     */
    fun updateDescription(newDescription: String?): Tag {
        val trimmedDescription = newDescription?.trim()?.takeIf { it.isNotBlank() }
        
        if (description != trimmedDescription) {
            description = trimmedDescription
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 更新标签颜色
     * 
     * @param newColor 新的标签颜色（十六进制格式），可以为null表示使用默认颜色
     * @return 当前标签实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果颜色格式无效
     */
    fun updateColor(newColor: String?): Tag {
        val validatedColor = newColor?.let { validateAndNormalizeColor(it) }
        
        if (color != validatedColor) {
            color = validatedColor
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 增加文章计数
     * 
     * @return 当前标签实例（支持链式调用）
     */
    fun incrementArticleCount(): Tag {
        articleCount++
        updatedAt = UpdatedAt.now()
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 减少文章计数
     * 
     * @return 当前标签实例（支持链式调用）
     */
    fun decrementArticleCount(): Tag {
        if (articleCount > 0) {
            articleCount--
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 设置文章计数
     * 
     * @param count 新的文章计数
     * @return 当前标签实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果计数为负数
     */
    fun setArticleCount(count: Int): Tag {
        require(count >= 0) { "Article count cannot be negative" }
        
        if (articleCount != count) {
            articleCount = count
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 激活标签
     * 
     * @return 当前标签实例（支持链式调用）
     */
    fun activate(): Tag {
        if (!isActive) {
            isActive = true
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 停用标签
     * 
     * @return 当前标签实例（支持链式调用）
     */
    fun deactivate(): Tag {
        if (isActive) {
            isActive = false
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 检查标签是否为系统标签
     * 
     * @return 如果是系统标签返回true，否则返回false
     */
    fun isSystemTag(): Boolean = name.isSystemTag()
    
    /**
     * 检查标签是否为热门标签
     * 
     * @param threshold 热门标签的文章数量阈值，默认为10
     * @return 如果是热门标签返回true，否则返回false
     */
    fun isPopular(threshold: Int = 10): Boolean = articleCount >= threshold
    
    /**
     * 检查标签是否为空标签（没有关联文章）
     * 
     * @return 如果是空标签返回true，否则返回false
     */
    fun isEmpty(): Boolean = articleCount == 0
    
    /**
     * 检查标签是否可以被删除
     * 
     * 系统标签和有关联文章的标签不能被删除
     * 
     * @return 如果可以删除返回true，否则返回false
     */
    fun canBeDeleted(): Boolean = !isSystemTag() && isEmpty()
    
    /**
     * 检查标签是否需要清理
     * 
     * 长时间未使用且没有关联文章的标签可能需要清理
     * 
     * @param daysThreshold 天数阈值，默认为90天
     * @return 如果需要清理返回true，否则返回false
     */
    fun needsCleanup(daysThreshold: Long = 90): Boolean {
        if (isSystemTag() || !isEmpty()) {
            return false
        }
        
        val daysSinceLastUpdate = java.time.Duration.between(
            updatedAt.value,
            java.time.Instant.now()
        ).toDays()
        
        return daysSinceLastUpdate > daysThreshold
    }
    
    /**
     * 获取标签的显示颜色
     * 
     * @return 标签颜色，如果未设置则返回随机默认颜色
     */
    fun getDisplayColor(): String = color ?: getRandomDefaultColor()
    
    /**
     * 检查标签是否与另一个标签相似
     * 
     * @param other 另一个标签
     * @return 如果相似返回true，否则返回false
     */
    fun isSimilarTo(other: Tag): Boolean = name.isSimilarTo(other.name)
    
    // 实现聚合根基类的抽象方法
    override fun ensureBusinessRules() {
        // 验证标签名称不能为空
        if (name.isEmpty()) {
            throw BusinessRuleViolationException("Tag name cannot be empty")
        }
        
        // 验证描述长度
        description?.let { desc ->
            if (desc.length > MAX_DESCRIPTION_LENGTH) {
                throw BusinessRuleViolationException("Tag description cannot exceed $MAX_DESCRIPTION_LENGTH characters")
            }
        }
        
        // 验证文章计数不能为负数
        if (articleCount < 0) {
            throw BusinessRuleViolationException("Article count cannot be negative")
        }
        
        // 验证颜色格式（如果提供）
        color?.let { colorValue ->
            if (!colorValue.matches(Regex("^#[0-9A-F]{6}$"))) {
                throw BusinessRuleViolationException("Invalid color format")
            }
        }
        
        // 验证更新时间不能早于创建时间
        if (!updatedAt.isAfterCreation(createdAt) && updatedAt.value != createdAt.value) {
            throw BusinessRuleViolationException("Updated time cannot be before created time")
        }
    }
    
    override fun getId(): TagId = id
    
    // Getters - 提供对内部状态的只读访问
    fun getName(): TagName = name
    fun getDescription(): String? = description
    fun getColor(): String? = color
    fun getArticleCount(): Int = articleCount
    fun isActive(): Boolean = isActive
    fun getCreatedAt(): CreatedAt = createdAt
    fun getUpdatedAt(): UpdatedAt = updatedAt
}