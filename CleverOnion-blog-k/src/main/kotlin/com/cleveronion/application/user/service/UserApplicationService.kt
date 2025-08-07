package com.cleveronion.application.user.service

import com.cleveronion.application.user.command.AuthenticateUserCommand
import com.cleveronion.application.user.command.RegisterUserCommand
import com.cleveronion.application.user.command.UpdateUserProfileCommand
import com.cleveronion.application.user.dto.UserDto
import com.cleveronion.application.user.dto.AuthenticationResultDto
import com.cleveronion.application.user.usecase.AuthenticateUserUseCase
import com.cleveronion.application.user.usecase.RegisterUserUseCase
import com.cleveronion.application.user.usecase.UpdateUserProfileUseCase
import com.cleveronion.common.annotation.UseCase

/**
 * 用户应用服务
 * 
 * 协调用户相关的用例，提供统一的用户管理接口
 * 处理用户认证、注册、档案管理等功能
 */
@UseCase
class UserApplicationService(
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) {
    
    /**
     * 用户认证
     * 
     * 处理用户登录认证，支持GitHub OAuth认证
     * 
     * @param command 认证命令
     * @return 认证结果DTO
     */
    suspend fun authenticateUser(command: AuthenticateUserCommand): AuthenticationResultDto {
        return authenticateUserUseCase.execute(command)
    }
    
    /**
     * 用户注册
     * 
     * 处理新用户注册流程
     * 
     * @param command 注册命令
     * @return 注册的用户DTO
     */
    suspend fun registerUser(command: RegisterUserCommand): UserDto {
        return registerUserUseCase.execute(command)
    }
    
    /**
     * 更新用户档案
     * 
     * 处理用户信息更新
     * 
     * @param command 更新档案命令
     * @return 更新后的用户DTO
     */
    suspend fun updateUserProfile(command: UpdateUserProfileCommand): UserDto {
        val result = updateUserProfileUseCase.execute(command)
        return result.user
    }
    
    /**
     * GitHub OAuth 登录
     * 
     * 便捷方法，处理GitHub OAuth登录流程
     * 
     * @param githubId GitHub用户ID
     * @param githubLogin GitHub登录名
     * @param email 邮箱地址（可选）
     * @param name 显示名称（可选）
     * @param bio 个人简介（可选）
     * @param avatarUrl 头像URL（可选）
     * @return 认证结果DTO
     */
    suspend fun loginWithGitHub(
        githubId: Long,
        githubLogin: String,
        email: String? = null,
        name: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ): AuthenticationResultDto {
        val command = AuthenticateUserCommand(
            githubId = githubId,
            githubLogin = githubLogin,
            email = email,
            name = name,
            bio = bio,
            avatarUrl = avatarUrl
        )
        return authenticateUserUseCase.execute(command)
    }
    
    /**
     * 更新用户基本信息
     * 
     * 便捷方法，用于更新用户的基本信息
     * 
     * @param userId 用户ID
     * @param name 显示名称
     * @param bio 个人简介
     * @param email 邮箱地址
     * @return 更新后的用户DTO
     */
    suspend fun updateBasicInfo(
        userId: Long,
        name: String? = null,
        bio: String? = null,
        email: String? = null
    ): UserDto {
        val command = UpdateUserProfileCommand(
            userId = userId,
            name = name,
            bio = bio,
            email = email
        )
        val result = updateUserProfileUseCase.execute(command)
        return result.user
    }
    
    /**
     * 更新用户头像
     * 
     * 便捷方法，用于更新用户头像
     * 
     * @param userId 用户ID
     * @param avatarUrl 新的头像URL
     * @return 更新后的用户DTO
     */
    suspend fun updateAvatar(userId: Long, avatarUrl: String): UserDto {
        val command = UpdateUserProfileCommand(
            userId = userId,
            avatarUrl = avatarUrl
        )
        val result = updateUserProfileUseCase.execute(command)
        return result.user
    }
}