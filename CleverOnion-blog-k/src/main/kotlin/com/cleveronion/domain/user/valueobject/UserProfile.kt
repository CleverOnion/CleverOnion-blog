package com.cleveronion.domain.user.valueobject

/**
 * 用户档案值对象
 * 
 * 包含用户的基本信息和个人资料
 */
data class UserProfile(
    val githubLogin: String,
    val email: Email?,
    val name: String?,
    val bio: String?,
    val avatarUrl: String?
) {
    init {
        require(githubLogin.isNotBlank()) { "GitHub login cannot be blank" }
        require(githubLogin.length <= 100) { "GitHub login cannot exceed 100 characters" }
        require(githubLogin.matches(Regex("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$"))) {
            "GitHub login must contain only alphanumeric characters and hyphens, and cannot start or end with a hyphen"
        }
        
        name?.let { 
            require(it.isNotBlank()) { "Name cannot be blank if provided" }
            require(it.length <= 100) { "Name cannot exceed 100 characters" }
        }
        
        bio?.let { 
            require(it.length <= 500) { "Bio cannot exceed 500 characters" }
        }
        
        avatarUrl?.let {
            require(it.isNotBlank()) { "Avatar URL cannot be blank if provided" }
            require(it.length <= 500) { "Avatar URL cannot exceed 500 characters" }
            require(isValidUrl(it)) { "Invalid avatar URL format" }
        }
    }
    
    companion object {
        /**
         * 创建基本的用户档案（仅包含GitHub登录名）
         */
        fun basic(githubLogin: String): UserProfile {
            return UserProfile(
                githubLogin = githubLogin,
                email = null,
                name = null,
                bio = null,
                avatarUrl = null
            )
        }
        
        /**
         * 创建完整的用户档案
         */
        fun complete(
            githubLogin: String,
            email: Email?,
            name: String?,
            bio: String?,
            avatarUrl: String?
        ): UserProfile {
            return UserProfile(
                githubLogin = githubLogin,
                email = email,
                name = name,
                bio = bio,
                avatarUrl = avatarUrl
            )
        }
        
        /**
         * 常量定义
         */
        const val MAX_GITHUB_LOGIN_LENGTH = 100
        const val MAX_NAME_LENGTH = 100
        const val MAX_BIO_LENGTH = 500
        const val MAX_AVATAR_URL_LENGTH = 500
        
        /**
         * 简单的URL格式验证
         */
        private fun isValidUrl(url: String): Boolean {
            return try {
                url.startsWith("http://") || url.startsWith("https://")
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * 更新邮箱
     */
    fun withEmail(newEmail: Email?): UserProfile {
        return copy(email = newEmail)
    }
    
    /**
     * 更新姓名
     */
    fun withName(newName: String?): UserProfile {
        val trimmedName = newName?.trim()?.takeIf { it.isNotBlank() }
        return copy(name = trimmedName)
    }
    
    /**
     * 更新个人简介
     */
    fun withBio(newBio: String?): UserProfile {
        val trimmedBio = newBio?.trim()?.takeIf { it.isNotBlank() }
        return copy(bio = trimmedBio)
    }
    
    /**
     * 更新头像URL
     */
    fun withAvatarUrl(newAvatarUrl: String?): UserProfile {
        val trimmedUrl = newAvatarUrl?.trim()?.takeIf { it.isNotBlank() }
        return copy(avatarUrl = trimmedUrl)
    }
    
    /**
     * 检查档案是否完整
     */
    fun isComplete(): Boolean {
        return email != null && name != null && bio != null && avatarUrl != null
    }
    
    /**
     * 获取显示名称（优先使用name，否则使用githubLogin）
     */
    fun getDisplayName(): String {
        return name?.takeIf { it.isNotBlank() } ?: githubLogin
    }
    
    /**
     * 检查是否有邮箱
     */
    fun hasEmail(): Boolean = email != null
    
    /**
     * 检查是否有头像
     */
    fun hasAvatar(): Boolean = avatarUrl != null
    
    /**
     * 检查是否有个人简介
     */
    fun hasBio(): Boolean = bio != null
}