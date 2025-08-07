package com.cleveronion.domain.article.valueobject

/**
 * 文章ID值对象
 * 
 * 表示文章的唯一标识符
 */
data class ArticleId(val value: Long) {
    init {
        require(value > 0) { "Article ID must be positive" }
    }
    
    companion object {
        /**
         * 从Long值创建ArticleId
         */
        fun of(value: Long): ArticleId = ArticleId(value)
        
        /**
         * 生成新的ArticleId（简化实现，实际应使用更好的ID生成策略）
         */
        fun generate(): ArticleId = ArticleId(System.currentTimeMillis())
    }
    
    /**
     * 转换为Long值
     */
    fun toLong(): Long = value
    
    override fun toString(): String = value.toString()
}