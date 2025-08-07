package com.cleveronion.application.user.command

/**
 * 用户注册命令
 * 
 * 包含用户注册所需的信息
 */
data class RegisterUserCommand(
    val githubId: Long,
    val githubLogin: String,
    val email: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
) {
    init {
        require(githubId > 0) { "GitHub ID must be positive" }
        require(githubLogin.isNotBlank()) { "GitHub login cannot be blank" }
        email?.let { require(it.isNotBlank()) { "Email cannot be blank" } }
        name?.let { require(it.isNotBlank()) { "Name cannot be blank" } }
        bio?.let { require(it.length <= 500) { "Bio cannot exceed 500 characters" } }
        avatarUrl?.let { require(it.isNotBlank()) { "Avatar URL cannot be blank" } }
    }
}