package com.cleveronion.application.user.command

/**
 * 更新用户档案命令
 * 
 * 包含更新用户档案所需的信息
 */
data class UpdateUserProfileCommand(
    val userId: Long,
    val email: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
) {
    init {
        require(userId > 0) { "User ID must be positive" }
        email?.let { require(it.isNotBlank()) { "Email cannot be blank" } }
        name?.let { require(it.isNotBlank()) { "Name cannot be blank" } }
        bio?.let { require(it.length <= 500) { "Bio cannot exceed 500 characters" } }
        avatarUrl?.let { require(it.isNotBlank()) { "Avatar URL cannot be blank" } }
    }
}