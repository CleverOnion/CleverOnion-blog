package com.cleveronion.domain.user.service

import com.cleveronion.domain.user.aggregate.User
import com.cleveronion.domain.user.valueobject.*
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException

/**
 * 用户领域服务
 * 
 * 封装跨用户聚合的业务逻辑，处理复杂的业务规则和操作。
 * 领域服务用于处理不属于单个聚合的业务逻辑。
 */
class UserDomainService(
    private val userRepository: UserRepositoryPort
) {
    
    /**
     * 验证用户注册的业务规则
     * 
     * @param githubId GitHub ID
     * @param profile 用户档案
     * @throws BusinessRuleViolationException 如果不满足注册条件
     */
    suspend fun validateUserRegistration(githubId: GitHubId, profile: UserProfile) {
        // 检查GitHub ID是否已存在
        if (userRepository.existsByGitHubId(githubId)) {
            throw BusinessRuleViolationException("GitHub ID already exists: ${githubId.value}")
        }
        
        // 检查GitHub登录名是否已存在
        if (userRepository.existsByGitHubLogin(profile.githubLogin)) {
            throw BusinessRuleViolationException("GitHub login already exists: ${profile.githubLogin}")
        }
        
        // 检查邮箱是否已存在（如果提供）
        profile.email?.let { email ->
            if (userRepository.existsByEmail(email)) {
                throw BusinessRuleViolationException("Email already exists: ${email.value}")
            }
        }
    }
    
    /**
     * 验证邮箱更新的业务规则
     * 
     * @param user 要更新邮箱的用户
     * @param newEmail 新邮箱，可以为null表示移除邮箱
     * @throws BusinessRuleViolationException 如果邮箱已被其他用户使用
     */
    suspend fun validateEmailUpdate(user: User, newEmail: Email?) {
        newEmail?.let { email ->
            // 检查邮箱是否已被其他用户使用
            val existingUser = userRepository.findByEmail(email)
            if (existingUser != null && existingUser.getId() != user.getId()) {
                throw BusinessRuleViolationException("Email already exists: ${email.value}")
            }
        }
    }
    
    /**
     * 检查用户是否有权限执行管理操作
     * 
     * @param userId 用户ID
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @return 用户实例
     * @throws EntityNotFoundException 如果用户不存在
     * @throws UnauthorizedOperationException 如果用户没有管理权限
     */
    suspend fun checkAdminPermission(userId: UserId, adminGitHubIds: Set<GitHubId>): User {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User not found: ${userId.value}")
        
        if (!user.canPerformAdminActions(adminGitHubIds)) {
            throw UnauthorizedOperationException("User does not have admin permissions")
        }
        
        return user
    }
    
    /**
     * 检查用户是否可以编辑指定内容
     * 
     * @param userId 用户ID
     * @param contentAuthorId 内容作者ID
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @return 用户实例
     * @throws EntityNotFoundException 如果用户不存在
     * @throws UnauthorizedOperationException 如果用户没有编辑权限
     */
    suspend fun checkContentEditPermission(
        userId: UserId,
        contentAuthorId: UserId,
        adminGitHubIds: Set<GitHubId>
    ): User {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User not found: ${userId.value}")
        
        if (!user.canEditContent(contentAuthorId, adminGitHubIds)) {
            throw UnauthorizedOperationException("User does not have permission to edit this content")
        }
        
        return user
    }
    
    /**
     * 检查用户是否可以删除指定内容
     * 
     * @param userId 用户ID
     * @param contentAuthorId 内容作者ID
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @return 用户实例
     * @throws EntityNotFoundException 如果用户不存在
     * @throws UnauthorizedOperationException 如果用户没有删除权限
     */
    suspend fun checkContentDeletePermission(
        userId: UserId,
        contentAuthorId: UserId,
        adminGitHubIds: Set<GitHubId>
    ): User {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User not found: ${userId.value}")
        
        if (!user.canDeleteContent(contentAuthorId, adminGitHubIds)) {
            throw UnauthorizedOperationException("User does not have permission to delete this content")
        }
        
        return user
    }
    
    /**
     * 验证用户停用的业务规则
     * 
     * @param user 要停用的用户
     * @param operatorId 执行操作的用户ID
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @throws BusinessRuleViolationException 如果不能停用用户
     * @throws UnauthorizedOperationException 如果操作者没有权限
     */
    suspend fun validateUserDeactivation(
        user: User,
        operatorId: UserId,
        adminGitHubIds: Set<GitHubId>
    ) {
        // 检查操作者权限
        val operator = checkAdminPermission(operatorId, adminGitHubIds)
        
        // 用户不能停用自己
        if (user.getId() == operatorId) {
            throw BusinessRuleViolationException("User cannot deactivate themselves")
        }
        
        // 检查用户是否已经停用
        if (!user.isActive()) {
            throw BusinessRuleViolationException("User is already deactivated")
        }
        
        // 管理员不能停用其他管理员（可选规则）
        if (user.isAdmin(adminGitHubIds)) {
            throw BusinessRuleViolationException("Cannot deactivate admin users")
        }
    }
    
    /**
     * 验证用户激活的业务规则
     * 
     * @param user 要激活的用户
     * @param operatorId 执行操作的用户ID
     * @param adminGitHubIds 管理员GitHub ID白名单
     * @throws BusinessRuleViolationException 如果不能激活用户
     * @throws UnauthorizedOperationException 如果操作者没有权限
     */
    suspend fun validateUserActivation(
        user: User,
        operatorId: UserId,
        adminGitHubIds: Set<GitHubId>
    ) {
        // 检查操作者权限
        checkAdminPermission(operatorId, adminGitHubIds)
        
        // 检查用户是否已经激活
        if (user.isActive()) {
            throw BusinessRuleViolationException("User is already active")
        }
    }
    
    /**
     * 计算用户活跃度评分
     * 
     * 基于最后登录时间、档案完整度等因素计算用户活跃度
     * 
     * @param user 用户实例
     * @return 活跃度评分（0-100）
     */
    fun calculateUserActivityScore(user: User): Double {
        if (!user.isActive()) {
            return 0.0
        }
        
        var score = 0.0
        
        // 基础分数（激活用户）
        score += 20.0
        
        // 档案完整度评分（30分）
        if (user.hasCompleteProfile()) {
            score += 30.0
        } else {
            val profile = user.getProfile()
            if (profile.hasEmail()) score += 10.0
            if (profile.name != null) score += 10.0
            if (profile.hasBio()) score += 5.0
            if (profile.hasAvatar()) score += 5.0
        }
        
        // 登录活跃度评分（50分）
        val lastLoginAt = user.getLastLoginAt()
        if (lastLoginAt != null) {
            val daysSinceLastLogin = java.time.Duration.between(
                lastLoginAt.value,
                java.time.Instant.now()
            ).toDays()
            
            val loginScore = when {
                daysSinceLastLogin <= 1 -> 50.0
                daysSinceLastLogin <= 7 -> 40.0
                daysSinceLastLogin <= 30 -> 30.0
                daysSinceLastLogin <= 90 -> 20.0
                daysSinceLastLogin <= 180 -> 10.0
                else -> 0.0
            }
            score += loginScore
        } else {
            // 从未登录的用户
            val daysSinceRegistration = java.time.Duration.between(
                user.getCreatedAt().value,
                java.time.Instant.now()
            ).toDays()
            
            // 新注册用户给予一定分数
            if (daysSinceRegistration <= 7) {
                score += 25.0
            } else if (daysSinceRegistration <= 30) {
                score += 10.0
            }
        }
        
        return minOf(score, 100.0)
    }
    
    /**
     * 检查用户是否需要档案完善提醒
     * 
     * @param user 用户实例
     * @return 如果需要提醒返回true，否则返回false
     */
    fun needsProfileCompletionReminder(user: User): Boolean {
        if (!user.isActive()) {
            return false
        }
        
        val profile = user.getProfile()
        
        // 如果档案已完整，不需要提醒
        if (profile.isComplete()) {
            return false
        }
        
        // 注册超过7天但档案不完整的用户需要提醒
        val daysSinceRegistration = java.time.Duration.between(
            user.getCreatedAt().value,
            java.time.Instant.now()
        ).toDays()
        
        return daysSinceRegistration >= 7
    }
    
    /**
     * 获取用户档案完善建议
     * 
     * @param user 用户实例
     * @return 建议列表
     */
    fun getProfileCompletionSuggestions(user: User): List<String> {
        val suggestions = mutableListOf<String>()
        val profile = user.getProfile()
        
        if (!profile.hasEmail()) {
            suggestions.add("添加邮箱地址以接收重要通知")
        }
        
        if (profile.name.isNullOrBlank()) {
            suggestions.add("设置显示名称让其他用户更容易识别您")
        }
        
        if (!profile.hasBio()) {
            suggestions.add("添加个人简介介绍您的背景和兴趣")
        }
        
        if (!profile.hasAvatar()) {
            suggestions.add("上传头像让您的档案更加个性化")
        }
        
        return suggestions
    }
    
    /**
     * 检查用户是否为可疑账户
     * 
     * 基于注册时间、活跃度等因素判断是否为可疑账户
     * 
     * @param user 用户实例
     * @return 如果是可疑账户返回true，否则返回false
     */
    fun isSuspiciousAccount(user: User): Boolean {
        // 账户已停用
        if (!user.isActive()) {
            return false
        }
        
        val daysSinceRegistration = java.time.Duration.between(
            user.getCreatedAt().value,
            java.time.Instant.now()
        ).toDays()
        
        // 注册超过30天但从未登录
        if (user.getLastLoginAt() == null && daysSinceRegistration > 30) {
            return true
        }
        
        // 注册超过90天但档案完全空白
        if (daysSinceRegistration > 90) {
            val profile = user.getProfile()
            if (!profile.hasEmail() && profile.name.isNullOrBlank() && !profile.hasBio()) {
                return true
            }
        }
        
        return false
    }
}