package com.cleveronion.application.user.usecase

import com.cleveronion.application.user.command.RegisterUserCommand
import com.cleveronion.application.user.dto.UserDto
import com.cleveronion.domain.user.aggregate.User
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.GitHubId
import com.cleveronion.domain.user.valueobject.Email
import com.cleveronion.domain.user.valueobject.UserProfile
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.annotation.UseCase

/**
 * 用户注册用例
 * 
 * 实现用户注册的业务逻辑，包括唯一性验证和用户创建
 */
@UseCase
class RegisterUserUseCase(
    private val userRepository: UserRepositoryPort
) {
    
    /**
     * 执行用户注册用例
     * 
     * @param command 用户注册命令
     * @return 注册的用户DTO
     * @throws ValidationException 当输入验证失败或用户已存在时
     */
    suspend fun execute(command: RegisterUserCommand): UserDto {
        val githubId = GitHubId.of(command.githubId)
        
        // 验证用户唯一性
        validateUserUniqueness(githubId, command.githubLogin, command.email)
        
        // 创建用户档案
        val profile = createUserProfile(command)
        
        // 创建新用户
        val newUser = User.create(githubId, profile)
        
        // 保存用户
        val savedUser = userRepository.save(newUser)
        
        return UserDto.fromDomain(savedUser)
    }
    
    /**
     * 验证用户唯一性
     */
    private suspend fun validateUserUniqueness(
        githubId: GitHubId,
        githubLogin: String,
        email: String?
    ) {
        // 检查GitHub ID是否已存在
        if (userRepository.existsByGitHubId(githubId)) {
            throw ValidationException("User with GitHub ID '${githubId.value}' already exists")
        }
        
        // 检查GitHub登录名是否已存在
        if (userRepository.existsByGitHubLogin(githubLogin)) {
            throw ValidationException("User with GitHub login '$githubLogin' already exists")
        }
        
        // 检查邮箱是否已存在（如果提供）
        email?.let { emailString ->
            val emailVO = Email.of(emailString)
            if (userRepository.existsByEmail(emailVO)) {
                throw ValidationException("User with email '$emailString' already exists")
            }
        }
    }
    
    /**
     * 创建用户档案
     */
    private fun createUserProfile(command: RegisterUserCommand): UserProfile {
        val email = command.email?.let { Email.of(it) }
        
        return UserProfile.complete(
            githubLogin = command.githubLogin,
            email = email,
            name = command.name,
            bio = command.bio,
            avatarUrl = command.avatarUrl
        )
    }
}