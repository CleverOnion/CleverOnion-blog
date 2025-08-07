package com.cleveronion.domain.article.valueobject

/**
 * 文章状态值对象
 * 
 * 表示文章的发布状态，包含状态转换规则
 */
enum class ArticleStatus {
    /**
     * 草稿状态 - 文章正在编写中，未发布
     */
    DRAFT,
    
    /**
     * 已发布状态 - 文章已公开发布
     */
    PUBLISHED,
    
    /**
     * 已归档状态 - 文章已归档，不再显示
     */
    ARCHIVED;
    
    /**
     * 检查是否可以转换到指定状态
     * 
     * 状态转换规则：
     * - DRAFT -> PUBLISHED, ARCHIVED
     * - PUBLISHED -> ARCHIVED
     * - ARCHIVED -> 无法转换到其他状态
     */
    fun canTransitionTo(newStatus: ArticleStatus): Boolean {
        return when (this) {
            DRAFT -> newStatus in setOf(PUBLISHED, ARCHIVED)
            PUBLISHED -> newStatus == ARCHIVED
            ARCHIVED -> false
        }
    }
    
    /**
     * 检查是否为已发布状态
     */
    fun isPublished(): Boolean = this == PUBLISHED
    
    /**
     * 检查是否为草稿状态
     */
    fun isDraft(): Boolean = this == DRAFT
    
    /**
     * 检查是否为已归档状态
     */
    fun isArchived(): Boolean = this == ARCHIVED
    
    /**
     * 检查是否为公开可见状态
     */
    fun isPubliclyVisible(): Boolean = this == PUBLISHED
    
    /**
     * 获取状态的中文描述
     */
    fun getDisplayName(): String {
        return when (this) {
            DRAFT -> "草稿"
            PUBLISHED -> "已发布"
            ARCHIVED -> "已归档"
        }
    }
    
    companion object {
        /**
         * 从字符串创建ArticleStatus
         */
        fun fromString(value: String): ArticleStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid article status: $value")
            }
        }
        
        /**
         * 获取所有可公开访问的状态
         */
        fun getPublicStatuses(): Set<ArticleStatus> = setOf(PUBLISHED)
        
        /**
         * 获取所有可编辑的状态
         */
        fun getEditableStatuses(): Set<ArticleStatus> = setOf(DRAFT)
    }
}