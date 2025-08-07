package com.cleveronion.domain.shared.valueobject

/**
 * 版本值对象
 * 
 * 表示实体的版本号，用于乐观锁控制
 */
data class Version(val value: Long) {
    init {
        require(value >= 0) { "Version must be non-negative" }
    }
    
    companion object {
        /**
         * 初始版本
         */
        fun initial(): Version = Version(0)
        
        /**
         * 从长整型创建版本
         */
        fun of(value: Long): Version = Version(value)
    }
    
    /**
     * 获取下一个版本
     */
    fun next(): Version = Version(value + 1)
    
    /**
     * 检查是否为初始版本
     */
    fun isInitial(): Boolean = value == 0L
    
    /**
     * 检查是否比另一个版本新
     */
    fun isNewerThan(other: Version): Boolean = value > other.value
    
    /**
     * 检查是否比另一个版本旧
     */
    fun isOlderThan(other: Version): Boolean = value < other.value
}