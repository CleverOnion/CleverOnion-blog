package com.cleveronion.domain.user.aggregate

import com.cleveronion.domain.user.valueobject.*
import com.cleveronion.domain.shared.aggregate.AggregateRoot
import com.cleveronion.domain.shared.valueobject.CreatedAt
import com.cleveronion.domain.shared.valueobject.UpdatedAt
import com.cleveronion.domain.shared.event.UserEmailChangedEvent
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException

/**
 * 用户聚合根
 * 
 * 用户聚合的核心实体，包含用户的所有业务逻辑和状态管理。
 * 负责维护用户信息的一致性和业务规则。
 */
class User private constructor(
    private val id: UserId,
    private val githubId: GitHubId,
    private var profile: UserProfile,
    private var isActive: Boolean,
    private var lastLoginAt: UpdatedAt?,
    private val createdAt: CreatedAt,
    private var updatedAt: UpdatedAt
) : AggregateRoot<UserId>() {
    
    companion object {
        /**
         * 创建新用户
         * 
         * @param githubId GitHub ID
         * @param profile 用户档案
         * @return 新创建的用户实例
         */
        fun create(
            githubId: GitHubId,
            profile: UserProfile
        ): User {
            val now = CreatedAt.now()
            return User(
                id = UserId.generate(),
                githubId = githubId,
                profile = profile,
                isActive = true,
                lastLoginAt = null,
                createdAt = now,
                updatedAt = UpdatedAt.now()
            ).also { user ->
                user.ensureBusinessRules()
            }
        }
        
        /**
         * 从现有数据重建用户聚合
         * 
         * 用于从数据库或其他持久化存储中重建聚合实例
         */
        fun reconstruct(
            id: UserId,
            githubId: GitHubId,
            profile: UserProfile,
            isActive: Boolean,
            lastLoginAt: UpdatedAt?,
            createdAt: CreatedAt,
            updatedAt: UpdatedAt
        ): User {
            return User(
                id = id,
                githubId = githubId,
                profile = profile,
                isActive = isActive,
                lastLoginAt = lastLoginAt,
                createdAt = createdAt,
                updatedAt = updatedAt
            ).also { user ->
                user.ensureBusinessRules()
            }
        }
    }
    
    /**
     * 更新用户档案
     * 
     * @param newProfile 新的用户档案
     * @return 当前用户实例（支持链式调用）
     */
    fun updateProfile(newProfile: UserProfile): User {
        val oldEmail = profile.email
        val newEmail = newProfile.email
        
        profile = newProfile
        updatedAt = UpdatedAt.now()
        
        // 如果邮箱发生变化，发布事件
        if (oldEmail != newEmail) {
            addDomainEvent(
                UserEmailChangedEvent(
                    userId = id.value,
                    oldEmail = oldEmail?.value,
                    newEmail = newEmail?.value
                )
            )
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 更新邮箱
     * 
     * @param newEmail 新邮箱，可以为null表示移除邮箱
     * @return 当前用户实例（支持链式调用）
     */
    fun updateEmail(newEmail: Email?): User {
        val oldEmail = profile.email
        
        if (oldEmail != newEmail) {
            profile = profile.withEmail(newEmail)
            updatedAt = UpdatedAt.now()
            
            // 发布邮箱变更事件
            addDomainEvent(
                UserEmailChangedEvent(
                    userId = id.value,
                    oldEmail = oldEmail?.value,
                    newEmail = newEmail?.value
                )
            )
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 更新姓名
     * 
     * @param newName 新姓名，可以为null表示移除姓名
     * @return 当前用户实例（支持链式调用）
     */
    fun updateName(newName: String?): User {
        val updatedProfile = profile.withName(newName)
        if (profile != updatedProfile) {
            profile = updatedProfile
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 更新个人简介
     * 
     * @param newBio 新个人简介，可以为null表示移除简介
     * @return 当前用户实例（支持链式调用）
     */
    fun updateBio(newBio: String?): User {
        val updatedProfile = profile.withBio(newBio)
        if (profile != updatedProfile) {
            profile = updatedProfile
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 更新头像URL
     * 
     * @param newAvatarUrl 新头像URL，可以为null表示移除头像
     * @return 当前用户实例（支持链式调用）
     */
    fun updateAvatarUrl(newAvatarUrl: String?): User {
        val updatedProfile = profile.withAvatarUrl(newAvatarUrl)
        if (profile != updatedProfile) {
            profile = updatedProfile
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 激活用户
     * 
     * @return 当前用户实例（支持链式调用）
     */
    fun activate(): User {
        if (!isActive) {
            isActive = true
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 停用用户
     * 
     * @return 当前用户实例（支持链式调用）
     */
    fun deactivate(): User {
        if (isActive) {
            isActive = false
            updatedAt = UpdatedAt.now()
        }
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 记录用户登录
     * 
     * @return 当前用户实例（支持链式调用）
     * @throws BusinessRuleViolationException 如果用户未激活
     */
    fun recordLogin(): User {
        if (!isActive) {
            throw BusinessRuleViolationException("Cannot login with deactivated user account")
        }
        
        lastLoginAt = UpdatedAt.now()
        
        ensureBusinessRules()
        return this
    }
    
    /**
     * 检查用户是否为管理员
     * 
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @return 如果用户是管理员返回true，否则返回false
     */
    fun isAdmin(adminGitHubIds: Set<GitHubId>): Boolean {
        return githubId in adminGitHubIds && isActive
    }
    
    /**
     * 检查用户是否可以执行管理操作
     * 
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @return 如果用户可以执行管理操作返回true，否则返回false
     */
    fun canPerformAdminActions(adminGitHubIds: Set<GitHubId>): Boolean {
        return isAdmin(adminGitHubIds) && isActive
    }
    
    /**
     * 检查用户是否可以创建内容
     * 
     * @return 如果用户可以创建内容返回true，否则返回false
     */
    fun canCreateContent(): Boolean {
        return isActive
    }
    
    /**
     * 检查用户是否可以编辑指定用户的内容
     * 
     * @param contentAuthorId 内容作者的用户ID
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @return 如果用户可以编辑内容返回true，否则返回false
     */
    fun canEditContent(contentAuthorId: UserId, adminGitHubIds: Set<GitHubId>): Boolean {
        return isActive && (id == contentAuthorId || isAdmin(adminGitHubIds))
    }
    
    /**
     * 检查用户是否可以删除指定用户的内容
     * 
     * @param contentAuthorId 内容作者的用户ID
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @return 如果用户可以删除内容返回true，否则返回false
     */
    fun canDeleteContent(contentAuthorId: UserId, adminGitHubIds: Set<GitHubId>): Boolean {
        return isActive && (id == contentAuthorId || isAdmin(adminGitHubIds))
    }
    
    /**
     * 检查用户档案是否完整
     * 
     * @return 如果档案完整返回true，否则返回false
     */
    fun hasCompleteProfile(): Boolean {
        return profile.isComplete()
    }
    
    /**
     * 检查用户是否有邮箱
     * 
     * @return 如果用户有邮箱返回true，否则返回false
     */
    fun hasEmail(): Boolean {
        return profile.hasEmail()
    }
    
    /**
     * 检查用户是否长时间未登录
     * 
     * @param daysThreshold 天数阈值，默认为90天
     * @return 如果用户长时间未登录返回true，否则返回false
     */
    fun isInactive(daysThreshold: Long = 90): Boolean {
        val lastLogin = lastLoginAt?.value ?: createdAt.value
        val daysSinceLastLogin = java.time.Duration.between(
            lastLogin,
            java.time.Instant.now()
        ).toDays()
        
        return daysSinceLastLogin > daysThreshold
    }
    
    // 实现聚合根基类的抽象方法
    override fun ensureBusinessRules() {
        // 验证GitHub登录名不能为空
        if (profile.githubLogin.isBlank()) {
            throw BusinessRuleViolationException("User must have GitHub login")
        }
        
        // 验证邮箱格式（如果提供）
        profile.email?.let { email ->
            if (!email.isValid()) {
                throw BusinessRuleViolationException("Invalid email format")
            }
        }
        
        // 验证更新时间不能早于创建时间
        if (!updatedAt.isAfterCreation(createdAt) && updatedAt.value != createdAt.value) {
            throw BusinessRuleViolationException("Updated time cannot be before created time")
        }
        
        // 验证最后登录时间不能早于创建时间
        lastLoginAt?.let { loginTime ->
            if (loginTime.value.isBefore(createdAt.value)) {
                throw BusinessRuleViolationException("Last login time cannot be before created time")
            }
        }
    }
    
    override fun getId(): UserId = id
    
    // Getters - 提供对内部状态的只读访问
    fun getGitHubId(): GitHubId = githubId
    fun getProfile(): UserProfile = profile
    fun isActive(): Boolean = isActive
    fun getLastLoginAt(): UpdatedAt? = lastLoginAt
    fun getCreatedAt(): CreatedAt = createdAt
    fun getUpdatedAt(): UpdatedAt = updatedAt
}