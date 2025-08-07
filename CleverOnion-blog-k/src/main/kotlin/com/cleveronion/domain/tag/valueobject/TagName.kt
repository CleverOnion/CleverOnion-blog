package com.cleveronion.domain.tag.valueobject

/**
 * 标签名称值对象
 * 
 * 表示标签的名称，包含验证规则和格式化逻辑
 */
data class TagName(val value: String) {
    init {
        require(value.isNotBlank()) { "Tag name cannot be blank" }
        require(value.length <= MAX_LENGTH) { "Tag name cannot exceed $MAX_LENGTH characters" }
        require(value.length >= MIN_LENGTH) { "Tag name must be at least $MIN_LENGTH characters" }
        require(value.trim() == value) { "Tag name cannot have leading or trailing whitespace" }
        require(isValidFormat(value)) { 
            "Tag name can only contain letters, numbers, Chinese characters, underscore and hyphen" 
        }
    }
    
    companion object {
        /**
         * 从字符串创建TagName
         */
        fun of(value: String): TagName = TagName(value.trim().lowercase())
        
        /**
         * 最大长度常量
         */
        const val MAX_LENGTH = 50
        
        /**
         * 最小长度常量
         */
        const val MIN_LENGTH = 1
        
        /**
         * 标签名称格式验证正则表达式
         * 允许字母、数字、中文字符、下划线和连字符
         */
        private val NAME_REGEX = Regex("^[a-zA-Z0-9\\u4e00-\\u9fa5_-]+$")
        
        /**
         * 验证标签名称格式
         */
        private fun isValidFormat(name: String): Boolean {
            return NAME_REGEX.matches(name)
        }
        
        /**
         * 预定义的系统标签
         */
        val SYSTEM_TAGS = setOf(
            "技术", "编程", "后端", "前端", "数据库", "架构",
            "kotlin", "java", "spring", "react", "vue", "javascript"
        )
    }
    
    /**
     * 获取标签名称长度
     */
    fun length(): Int = value.length
    
    /**
     * 检查是否为空标签名
     */
    fun isEmpty(): Boolean = value.isBlank()
    
    /**
     * 检查是否为系统标签
     */
    fun isSystemTag(): Boolean = value.lowercase() in SYSTEM_TAGS.map { it.lowercase() }
    
    /**
     * 检查是否包含中文字符
     */
    fun containsChinese(): Boolean = value.any { it in '\u4e00'..'\u9fa5' }
    
    /**
     * 检查是否为英文标签
     */
    fun isEnglish(): Boolean = value.all { it.isLetter() && it.code < 128 }
    
    /**
     * 转换为URL友好的格式
     */
    fun toUrlSlug(): String {
        return value.lowercase()
            .replace(" ", "-")
            .replace("_", "-")
    }
    
    /**
     * 获取标签的显示名称（首字母大写）
     */
    fun getDisplayName(): String {
        return if (containsChinese()) {
            value
        } else {
            value.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
        }
    }
    
    /**
     * 检查是否与另一个标签名相似
     */
    fun isSimilarTo(other: TagName): Boolean {
        val thisNormalized = value.lowercase().replace("-", "").replace("_", "")
        val otherNormalized = other.value.lowercase().replace("-", "").replace("_", "")
        
        return thisNormalized == otherNormalized
    }
    
    override fun toString(): String = value
}