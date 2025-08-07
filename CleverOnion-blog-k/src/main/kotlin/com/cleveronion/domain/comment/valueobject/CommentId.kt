package com.cleveronion.domain.comment.valueobject

/**
 * 评论ID值对象
 * 
 * 表示评论的唯一标识符
 */
data class CommentId(val value: Long) {
    init {
        require(value > 0) { "Comment ID must be positive" }
    }
    
    companion object {
        /**
         * 从Long值创建CommentId
         */
        fun of(value: Long): CommentId = CommentId(value)
        
        /**
         * 生成新的CommentId（简化实现，实际应使用更好的ID生成策略）
         */
        fun generate(): CommentId = CommentId(System.currentTimeMillis())
    }
    
    /**
     * 转换为Long值
     */
    fun toLong(): Long = value
    
    override fun toString(): String = value.toString()
}