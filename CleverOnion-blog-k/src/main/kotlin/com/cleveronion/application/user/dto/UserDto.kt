package com.cleveronion.application.user.dto

import com.cleveronion.domain.user.aggregate.User

/**
 * 用户数据传输对象
 * 
 * 用于在应用层和外部层之间传输用户数据
 */
data class UserDto(
    val id: Long,
    val githubId: Long,
    val githubLogin: String,
    val email: String?,
    val name: String?,
    val bio: String?,
    val avatarUrl: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String?
) {
    companion object {
        /**
         * 从领域模型创建DTO
         */
        fun fromDomain(user: User): UserDto {
            val profile = user.getProfile()
            return UserDto(
                id = user.getId().value,
                githubId = user.getGitHubId().value,
                githubLogin = profile.githubLogin,
                email = profile.email?.value,
                name = profile.name,
                bio = profile.bio,
                avatarUrl = profile.avatarUrl,
                isActive = user.isActive(),
                createdAt = user.getCreatedAt().value.toString(),
                updatedAt = user.getUpdatedAt().value.toString(),
                lastLoginAt = user.getLastLoginAt()?.value?.toString()
            )
        }
    }
}

/**
 * 用户认证结果数据传输对象
 * 
 * 用于传输用户认证结果
 */
data class AuthenticationResultDto(
    val user: UserDto,
    val isNewUser: Boolean,
    val token: String? = null
)

/**
 * 用户档案更新结果数据传输对象
 * 
 * 用于传输用户档案更新结果
 */
data class UserProfileUpdateResultDto(
    val user: UserDto,
    val updatedFields: List<String>
)