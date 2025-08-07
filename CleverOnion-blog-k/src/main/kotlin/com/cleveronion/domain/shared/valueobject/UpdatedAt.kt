package com.cleveronion.domain.shared.valueobject

import java.time.Instant

/**
 * 更新时间值对象
 * 
 * 表示实体的最后更新时间，可以被修改
 */
data class UpdatedAt(val value: Instant) {
    init {
        require(!value.isAfter(Instant.now().plusSeconds(60))) { 
            "Updated time cannot be in the future" 
        }
    }
    
    companion object {
        /**
         * 创建当前时间的UpdatedAt实例
         */
        fun now(): UpdatedAt = UpdatedAt(Instant.now())
        
        /**
         * 从时间戳创建UpdatedAt实例
         */
        fun fromEpochMilli(epochMilli: Long): UpdatedAt = UpdatedAt(Instant.ofEpochMilli(epochMilli))
        
        /**
         * 从ISO字符串创建UpdatedAt实例
         */
        fun fromString(isoString: String): UpdatedAt = UpdatedAt(Instant.parse(isoString))
    }
    
    /**
     * 检查是否在指定时间之前更新
     */
    fun isBefore(other: Instant): Boolean = value.isBefore(other)
    
    /**
     * 检查是否在指定时间之后更新
     */
    fun isAfter(other: Instant): Boolean = value.isAfter(other)
    
    /**
     * 检查是否在创建时间之后更新
     */
    fun isAfterCreation(createdAt: CreatedAt): Boolean = value.isAfter(createdAt.value)
    
    /**
     * 转换为ISO字符串格式
     */
    override fun toString(): String = value.toString()
}