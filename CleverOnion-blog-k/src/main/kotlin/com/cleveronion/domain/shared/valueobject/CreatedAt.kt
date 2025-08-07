package com.cleveronion.domain.shared.valueobject

import java.time.Instant

/**
 * 创建时间值对象
 * 
 * 表示实体的创建时间，一旦设置后不可变更
 */
data class CreatedAt(val value: Instant) {
    init {
        require(!value.isAfter(Instant.now().plusSeconds(60))) { 
            "Created time cannot be in the future" 
        }
    }
    
    companion object {
        /**
         * 创建当前时间的CreatedAt实例
         */
        fun now(): CreatedAt = CreatedAt(Instant.now())
        
        /**
         * 从时间戳创建CreatedAt实例
         */
        fun fromEpochMilli(epochMilli: Long): CreatedAt = CreatedAt(Instant.ofEpochMilli(epochMilli))
        
        /**
         * 从ISO字符串创建CreatedAt实例
         */
        fun fromString(isoString: String): CreatedAt = CreatedAt(Instant.parse(isoString))
    }
    
    /**
     * 检查是否在指定时间之前创建
     */
    fun isBefore(other: Instant): Boolean = value.isBefore(other)
    
    /**
     * 检查是否在指定时间之后创建
     */
    fun isAfter(other: Instant): Boolean = value.isAfter(other)
    
    /**
     * 转换为ISO字符串格式
     */
    override fun toString(): String = value.toString()
}