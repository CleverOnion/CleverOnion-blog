package com.cleveronion.domain.user.valueobject

/**
 * 邮箱地址值对象
 * 
 * 表示用户的邮箱地址，包含格式验证
 */
data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(isValidEmail(value)) { "Invalid email format: $value" }
        require(value.length <= 254) { "Email cannot exceed 254 characters" }
    }
    
    companion object {
        /**
         * 从字符串创建Email
         */
        fun of(value: String): Email = Email(value.trim().lowercase())
        
        /**
         * 邮箱格式验证正则表达式
         */
        private val EMAIL_REGEX = Regex(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
        )
        
        /**
         * 验证邮箱格式
         */
        private fun isValidEmail(email: String): Boolean {
            return EMAIL_REGEX.matches(email)
        }
        
        /**
         * 最大长度常量
         */
        const val MAX_LENGTH = 254
    }
    
    /**
     * 获取邮箱的域名部分
     */
    fun getDomain(): String {
        return value.substringAfter("@")
    }
    
    /**
     * 获取邮箱的用户名部分
     */
    fun getLocalPart(): String {
        return value.substringBefore("@")
    }
    
    /**
     * 检查是否为指定域名的邮箱
     */
    fun isFromDomain(domain: String): Boolean {
        return getDomain().equals(domain, ignoreCase = true)
    }
    
    /**
     * 验证邮箱格式是否有效
     */
    fun isValid(): Boolean = isValidEmail(value)
    
    override fun toString(): String = value
}