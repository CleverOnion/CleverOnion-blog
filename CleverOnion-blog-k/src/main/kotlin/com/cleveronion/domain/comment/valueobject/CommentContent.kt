package com.cleveronion.domain.comment.valueobject

/**
 * 评论内容值对象
 * 
 * 表示评论的文本内容，包含验证规则
 */
data class CommentContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Comment content cannot be blank" }
        require(value.length <= MAX_LENGTH) { "Comment content cannot exceed $MAX_LENGTH characters" }
        require(value.trim() == value) { "Comment content cannot have leading or trailing whitespace" }
    }
    
    companion object {
        /**
         * 从字符串创建CommentContent
         */
        fun of(value: String): CommentContent = CommentContent(value.trim())
        
        /**
         * 最大长度常量
         */
        const val MAX_LENGTH = 1000
        
        /**
         * 最小长度常量
         */
        const val MIN_LENGTH = 1
    }
    
    /**
     * 获取内容长度
     */
    fun length(): Int = value.length
    
    /**
     * 检查是否为空内容
     */
    fun isEmpty(): Boolean = value.isBlank()
    
    /**
     * 检查是否包含指定关键词
     */
    fun contains(keyword: String, ignoreCase: Boolean = true): Boolean {
        return value.contains(keyword, ignoreCase)
    }
    
    /**
     * 获取内容摘要（前100个字符）
     */
    fun getSummary(maxLength: Int = 100): String {
        return if (value.length <= maxLength) {
            value
        } else {
            value.substring(0, maxLength) + "..."
        }
    }
    
    /**
     * 检查内容是否过短
     */
    fun isTooShort(): Boolean = value.length < MIN_LENGTH
    
    /**
     * 检查内容是否过长
     */
    fun isTooLong(): Boolean = value.length > MAX_LENGTH
    
    override fun toString(): String = value
}