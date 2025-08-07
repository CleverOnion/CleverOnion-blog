package com.cleveronion.domain.article.valueobject

/**
 * 文章标题值对象
 * 
 * 表示文章的标题，包含验证规则
 */
data class ArticleTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "Article title cannot be blank" }
        require(value.length <= 255) { "Article title cannot exceed 255 characters" }
        require(value.trim() == value) { "Article title cannot have leading or trailing whitespace" }
    }
    
    companion object {
        /**
         * 从字符串创建ArticleTitle
         */
        fun of(value: String): ArticleTitle = ArticleTitle(value.trim())
        
        /**
         * 最大长度常量
         */
        const val MAX_LENGTH = 255
    }
    
    /**
     * 获取标题长度
     */
    fun length(): Int = value.length
    
    /**
     * 检查是否为空标题
     */
    fun isEmpty(): Boolean = value.isBlank()
    
    /**
     * 检查是否包含指定关键词
     */
    fun contains(keyword: String, ignoreCase: Boolean = true): Boolean {
        return value.contains(keyword, ignoreCase)
    }
    
    override fun toString(): String = value
}