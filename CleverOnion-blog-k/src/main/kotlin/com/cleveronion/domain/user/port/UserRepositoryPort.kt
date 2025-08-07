package com.cleveronion.domain.user.port

import com.cleveronion.domain.user.aggregate.User
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.user.valueobject.GitHubId
import com.cleveronion.domain.user.valueobject.Email
import com.cleveronion.domain.shared.valueobject.Pagination

/**
 * 用户仓储端口
 * 
 * 定义用户聚合的持久化操作接口，遵循六边形架构的端口模式。
 * 具体实现由基础设施层的适配器提供。
 */
interface UserRepositoryPort {
    
    /**
     * 保存用户
     * 
     * @param user 要保存的用户聚合
     * @return 保存后的用户聚合
     */
    suspend fun save(user: User): User
    
    /**
     * 根据ID查找用户
     * 
     * @param id 用户ID
     * @return 找到的用户聚合，如果不存在则返回null
     */
    suspend fun findById(id: UserId): User?
    
    /**
     * 根据GitHub ID查找用户
     * 
     * @param githubId GitHub ID
     * @return 找到的用户聚合，如果不存在则返回null
     */
    suspend fun findByGitHubId(githubId: GitHubId): User?
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱地址
     * @return 找到的用户聚合，如果不存在则返回null
     */
    suspend fun findByEmail(email: Email): User?
    
    /**
     * 根据GitHub登录名查找用户
     * 
     * @param githubLogin GitHub登录名
     * @return 找到的用户聚合，如果不存在则返回null
     */
    suspend fun findByGitHubLogin(githubLogin: String): User?
    
    /**
     * 根据ID删除用户
     * 
     * @param id 用户ID
     * @return 如果删除成功返回true，如果用户不存在返回false
     */
    suspend fun deleteById(id: UserId): Boolean
    
    /**
     * 检查用户是否存在
     * 
     * @param id 用户ID
     * @return 如果用户存在返回true，否则返回false
     */
    suspend fun existsById(id: UserId): Boolean
    
    /**
     * 检查GitHub ID是否已存在
     * 
     * @param githubId GitHub ID
     * @return 如果GitHub ID已存在返回true，否则返回false
     */
    suspend fun existsByGitHubId(githubId: GitHubId): Boolean
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱地址
     * @return 如果邮箱已存在返回true，否则返回false
     */
    suspend fun existsByEmail(email: Email): Boolean
    
    /**
     * 检查GitHub登录名是否已存在
     * 
     * @param githubLogin GitHub登录名
     * @return 如果GitHub登录名已存在返回true，否则返回false
     */
    suspend fun existsByGitHubLogin(githubLogin: String): Boolean
    
    /**
     * 获取所有活跃用户
     * 
     * @param pagination 分页参数
     * @return 活跃用户列表
     */
    suspend fun findActiveUsers(pagination: Pagination): List<User>
    
    /**
     * 获取所有非活跃用户
     * 
     * @param pagination 分页参数
     * @return 非活跃用户列表
     */
    suspend fun findInactiveUsers(pagination: Pagination): List<User>
    
    /**
     * 根据姓名搜索用户
     * 
     * @param nameKeyword 姓名关键词
     * @param pagination 分页参数
     * @param activeOnly 是否只搜索活跃用户，默认为true
     * @return 用户列表
     */
    suspend fun searchByName(
        nameKeyword: String,
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<User>
    
    /**
     * 根据GitHub登录名搜索用户
     * 
     * @param loginKeyword GitHub登录名关键词
     * @param pagination 分页参数
     * @param activeOnly 是否只搜索活跃用户，默认为true
     * @return 用户列表
     */
    suspend fun searchByGitHubLogin(
        loginKeyword: String,
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<User>
    
    /**
     * 获取最近注册的用户
     * 
     * @param pagination 分页参数
     * @param activeOnly 是否只包含活跃用户，默认为true
     * @return 用户列表，按注册时间降序排列
     */
    suspend fun findRecentlyRegistered(
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<User>
    
    /**
     * 获取最近登录的用户
     * 
     * @param pagination 分页参数
     * @param activeOnly 是否只包含活跃用户，默认为true
     * @return 用户列表，按最后登录时间降序排列
     */
    suspend fun findRecentlyLoggedIn(
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<User>
    
    /**
     * 获取长时间未登录的用户
     * 
     * @param daysThreshold 天数阈值
     * @param pagination 分页参数
     * @return 用户列表
     */
    suspend fun findInactiveUsers(
        daysThreshold: Long,
        pagination: Pagination
    ): List<User>
    
    /**
     * 获取用户总数
     * 
     * @param activeOnly 是否只统计活跃用户，默认为false
     * @return 用户总数
     */
    suspend fun count(activeOnly: Boolean = false): Long
    
    /**
     * 获取指定时间段内注册的用户数量
     * 
     * @param startTime 开始时间（ISO字符串格式）
     * @param endTime 结束时间（ISO字符串格式）
     * @return 用户数量
     */
    suspend fun countRegisteredBetween(startTime: String, endTime: String): Long
    
    /**
     * 获取有邮箱的用户数量
     * 
     * @param activeOnly 是否只统计活跃用户，默认为false
     * @return 用户数量
     */
    suspend fun countUsersWithEmail(activeOnly: Boolean = false): Long
    
    /**
     * 获取档案完整的用户数量
     * 
     * @param activeOnly 是否只统计活跃用户，默认为false
     * @return 用户数量
     */
    suspend fun countUsersWithCompleteProfile(activeOnly: Boolean = false): Long
    
    /**
     * 批量保存用户
     * 
     * @param users 要保存的用户列表
     * @return 保存后的用户列表
     */
    suspend fun saveAll(users: List<User>): List<User>
    
    /**
     * 批量删除用户
     * 
     * @param ids 要删除的用户ID列表
     * @return 实际删除的用户数量
     */
    suspend fun deleteByIds(ids: List<UserId>): Int
    
    /**
     * 批量激活用户
     * 
     * @param ids 要激活的用户ID列表
     * @return 实际激活的用户数量
     */
    suspend fun activateUsers(ids: List<UserId>): Int
    
    /**
     * 批量停用用户
     * 
     * @param ids 要停用的用户ID列表
     * @return 实际停用的用户数量
     */
    suspend fun deactivateUsers(ids: List<UserId>): Int
}