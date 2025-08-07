package com.cleveronion.domain.tag.valueobject

/**
 * 标签ID值对象
 * 
 * 表示标签的唯一标识符
 */
data class TagId(val value: Long) {
    init {
        require(value > 0) { "Tag ID must be positive" }
    }
    
    companion object {
        /**
         * 从Long值创建TagId
         */
        fun of(value: Long): TagId = TagId(value)
        
        /**
         * 生成新的TagId（简化实现，实际应使用更好的ID生成策略）
         */
        fun generate(): TagId = TagId(System.currentTimeMillis())
    }
    
    /**
     * 转换为Long值
     */
    fun toLong(): Long = value
    
    override fun toString(): String = value.toString()
}