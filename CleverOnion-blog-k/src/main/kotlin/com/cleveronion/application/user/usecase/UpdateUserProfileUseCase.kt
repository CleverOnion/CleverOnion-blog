package com.cleveronion.application.user.usecase

import com.cleveronion.application.user.command.UpdateUserProfileCommand
import com.cleveronion.application.user.dto.UserDto
import com.cleveronion.application.user.dto.UserProfileUpdateResultDto
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.user.valueobject.Email
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.application.shared.port.EventPublisherPort
import com.cleveronion.common.annotation.UseCase

/**
 * 更新用户档案用例
 * 
 * 实现用户档案更新的业务逻辑，包括邮箱唯一性验证和事件发布
 */
@UseCase
class UpdateUserProfileUseCase(
    private val userRepository: UserRepositoryPort,
    private val eventPublisher: EventPublisherPort
) {
    
    /**
     * 执行更新用户档案用例
     * 
     * @param command 更新用户档案命令
     * @return 更新结果DTO
     * @throws EntityNotFoundException 当用户不存在时
     * @throws ValidationException 当输入验证失败时
     */
    suspend fun execute(command: UpdateUserProfileCommand): UserProfileUpdateResultDto {
        val userId = UserId.of(command.userId)
        
        // 查找用户
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException.of("User", command.userId)
        
        // 验证用户是否活跃
        if (!user.isActive()) {
            throw ValidationException("Cannot update profile of inactive user")
        }
        
        // 验证邮箱唯一性（如果要更新邮箱）
        command.email?.let { newEmailString ->
            val newEmail = Email.of(newEmailString)
            val currentEmail = user.getProfile().email
            
            // 只有当邮箱真的发生变化时才需要验证唯一性
            if (currentEmail != newEmail) {
                val existingUserWithEmail = userRepository.findByEmail(newEmail)
                if (existingUserWithEmail != null && existingUserWithEmail.getId() != userId) {
                    throw ValidationException("Email '$newEmailString' is already in use by another user")
                }
            }
        }
        
        // 记录更新的字段
        val updatedFields = mutableListOf<String>()
        var updatedUser = user
        
        // 更新邮箱
        command.email?.let { newEmailString ->
            val newEmail = Email.of(newEmailString)
            val currentEmail = user.getProfile().email
            if (currentEmail != newEmail) {
                updatedUser = updatedUser.updateEmail(newEmail)
                updatedFields.add("email")
            }
        }
        
        // 更新姓名
        command.name?.let { newName ->
            val currentName = user.getProfile().name
            if (currentName != newName) {
                updatedUser = updatedUser.updateName(newName)
                updatedFields.add("name")
            }
        }
        
        // 更新个人简介
        command.bio?.let { newBio ->
            val currentBio = user.getProfile().bio
            if (currentBio != newBio) {
                updatedUser = updatedUser.updateBio(newBio)
                updatedFields.add("bio")
            }
        }
        
        // 更新头像URL
        command.avatarUrl?.let { newAvatarUrl ->
            val currentAvatarUrl = user.getProfile().avatarUrl
            if (currentAvatarUrl != newAvatarUrl) {
                updatedUser = updatedUser.updateAvatarUrl(newAvatarUrl)
                updatedFields.add("avatarUrl")
            }
        }
        
        // 如果没有任何字段更新，直接返回
        if (updatedFields.isEmpty()) {
            return UserProfileUpdateResultDto(
                user = UserDto.fromDomain(user),
                updatedFields = emptyList()
            )
        }
        
        // 保存更新后的用户
        val savedUser = userRepository.save(updatedUser)
        
        // 发布领域事件
        val domainEvents = savedUser.getDomainEvents()
        if (domainEvents.isNotEmpty()) {
            eventPublisher.publishAll(domainEvents)
            savedUser.clearDomainEvents()
        }
        
        return UserProfileUpdateResultDto(
            user = UserDto.fromDomain(savedUser),
            updatedFields = updatedFields
        )
    }
}