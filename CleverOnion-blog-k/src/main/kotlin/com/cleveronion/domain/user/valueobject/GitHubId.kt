package com.cleveronion.domain.user.valueobject

/**
 * GitHub ID值对象
 * 
 * 表示用户的GitHub唯一标识符
 */
data class GitHubId(val value: Long) {
    init {
        require(value > 0) { "GitHub ID must be positive" }
    }
    
    companion object {
        /**
         * 从Long值创建GitHubId
         */
        fun of(value: Long): GitHubId = GitHubId(value)
    }
    
    /**
     * 转换为Long值
     */
    fun toLong(): Long = value
    
    override fun toString(): String = value.toString()
}