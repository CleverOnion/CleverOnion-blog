package com.cleveronion.application.user.usecase

import com.cleveronion.application.user.command.AuthenticateUserCommand
import com.cleveronion.application.user.dto.UserDto
import com.cleveronion.application.user.dto.AuthenticationResultDto
import com.cleveronion.domain.user.aggregate.User
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.GitHubId
import com.cleveronion.domain.user.valueobject.Email
import com.cleveronion.domain.user.valueobject.UserProfile
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.annotation.UseCase

/**
 * 用户认证用例
 * 
 * 实现用户认证的业务逻辑，包括用户查找、创建和登录记录
 */
@UseCase
class AuthenticateUserUseCase(
    private val userRepository: UserRepositoryPort
) {
    
    /**
     * 执行用户认证用例
     * 
     * @param command 用户认证命令
     * @return 认证结果DTO
     * @throws ValidationException 当输入验证失败时
     */
    suspend fun execute(command: AuthenticateUserCommand): AuthenticationResultDto {
        val githubId = GitHubId.of(command.githubId)
        
        // 查找现有用户
        val existingUser = userRepository.findByGitHubId(githubId)
        
        return if (existingUser != null) {
            // 用户已存在，更新登录信息
            authenticateExistingUser(existingUser, command)
        } else {
            // 新用户，创建账户
            createNewUser(command)
        }
    }
    
    /**
     * 认证现有用户
     */
    private suspend fun authenticateExistingUser(
        user: User,
        command: AuthenticateUserCommand
    ): AuthenticationResultDto {
        // 检查用户是否被停用
        if (!user.isActive()) {
            throw ValidationException("User account is deactivated")
        }
        
        // 更新用户信息（GitHub信息可能会变化）
        val updatedProfile = createUserProfile(command)
        val updatedUser = user.updateProfile(updatedProfile)
            .recordLogin()
        
        // 保存更新后的用户
        val savedUser = userRepository.save(updatedUser)
        
        return AuthenticationResultDto(
            user = UserDto.fromDomain(savedUser),
            isNewUser = false
        )
    }
    
    /**
     * 创建新用户
     */
    private suspend fun createNewUser(command: AuthenticateUserCommand): AuthenticationResultDto {
        // 检查GitHub登录名是否已被使用
        val existingUserByLogin = userRepository.findByGitHubLogin(command.githubLogin)
        if (existingUserByLogin != null) {
            throw ValidationException("GitHub login '${command.githubLogin}' is already in use")
        }
        
        // 检查邮箱是否已被使用（如果提供）
        command.email?.let { emailString ->
            val email = Email.of(emailString)
            val existingUserByEmail = userRepository.findByEmail(email)
            if (existingUserByEmail != null) {
                throw ValidationException("Email '${emailString}' is already in use")
            }
        }
        
        // 创建用户档案
        val profile = createUserProfile(command)
        val githubId = GitHubId.of(command.githubId)
        
        // 创建新用户
        val newUser = User.create(githubId, profile)
            .recordLogin()
        
        // 保存用户
        val savedUser = userRepository.save(newUser)
        
        return AuthenticationResultDto(
            user = UserDto.fromDomain(savedUser),
            isNewUser = true
        )
    }
    
    /**
     * 创建用户档案
     */
    private fun createUserProfile(command: AuthenticateUserCommand): UserProfile {
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