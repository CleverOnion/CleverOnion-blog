package com.cleveronion.domain.user.valueobject

/**
 * 用户ID值对象
 * 
 * 表示用户的唯一标识符
 */
data class UserId(val value: Long) {
    init {
        require(value > 0) { "User ID must be positive" }
    }
    
    companion object {
        /**
         * 从Long值创建UserId
         */
        fun of(value: Long): UserId = UserId(value)
        
        /**
         * 生成新的UserId（简化实现，实际应使用更好的ID生成策略）
         */
        fun generate(): UserId = UserId(System.currentTimeMillis())
    }
    
    /**
     * 转换为Long值
     */
    fun toLong(): Long = value
    
    override fun toString(): String = value.toString()
}