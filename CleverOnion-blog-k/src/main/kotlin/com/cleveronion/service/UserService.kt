package com.cleveronion.service

import com.cleveronion.config.DatabaseConfig
import com.cleveronion.domain.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import org.slf4j.LoggerFactory

class UserService {
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    /**
     * 根据GitHub ID查找用户
     */
    fun findByGithubId(githubId: Long): User? {
        return transaction(DatabaseConfig.database) {
            Users.select { Users.githubId eq githubId }
                .map { rowToUser(it) }
                .singleOrNull()
        }
    }
    
    /**
     * 根据用户ID查找用户
     */
    fun findById(id: Long): User? {
        return transaction(DatabaseConfig.database) {
            Users.select { Users.id eq id }
                .map { rowToUser(it) }
                .singleOrNull()
        }
    }
    
    /**
     * 根据邮箱查找用户
     */
    fun findByEmail(email: String): User? {
        return transaction(DatabaseConfig.database) {
            Users.select { Users.email eq email }
                .map { rowToUser(it) }
                .singleOrNull()
        }
    }
    
    /**
     * 创建新用户
     */
    fun createUser(githubUser: GitHubUser, primaryEmail: String?): User {
        logger.info("正在数据库中创建新用户: ${githubUser.login}")
        return transaction(DatabaseConfig.database) {
            val userId = Users.insertAndGetId {
                it[githubId] = githubUser.id
                it[githubLogin] = githubUser.login
                it[avatarUrl] = githubUser.avatar_url
                it[email] = primaryEmail ?: githubUser.email
                it[name] = githubUser.name
                it[bio] = githubUser.bio
                it[createdAt] = Instant.now()
                it[updatedAt] = Instant.now()
            }
            
            logger.info("用户插入成功，分配的数据库ID: ${userId.value}")
            findById(userId.value)!!
        }
    }
    
    /**
     * 更新用户信息
     */
    fun updateUser(userId: Long, githubUser: GitHubUser, primaryEmail: String?): User? {
        logger.info("正在更新用户信息，数据库ID: $userId")
        return transaction(DatabaseConfig.database) {
            val updated = Users.update({ Users.id eq userId }) {
                it[githubLogin] = githubUser.login
                it[avatarUrl] = githubUser.avatar_url
                it[email] = primaryEmail ?: githubUser.email
                it[name] = githubUser.name
                it[bio] = githubUser.bio
                it[updatedAt] = Instant.now()
            }
            
            logger.info("用户更新操作影响行数: $updated")
            if (updated > 0) findById(userId) else null
        }
    }
    
    /**
     * 创建或更新用户（OAuth登录时使用）
     */
    fun createOrUpdateUser(githubUser: GitHubUser, primaryEmail: String?): User {
        logger.info("开始创建或更新用户: ${githubUser.login} (GitHub ID: ${githubUser.id})")
        val existingUser = findByGithubId(githubUser.id)
        
        return if (existingUser != null) {
            logger.info("找到现有用户，正在更新: ${existingUser.githubLogin} (数据库ID: ${existingUser.id})")
            val updatedUser = updateUser(existingUser.id, githubUser, primaryEmail) ?: existingUser
            logger.info("用户更新完成: ${updatedUser.githubLogin}")
            updatedUser
        } else {
            logger.info("未找到现有用户，正在创建新用户")
            val newUser = createUser(githubUser, primaryEmail)
            logger.info("新用户创建完成: ${newUser.githubLogin} (数据库ID: ${newUser.id})")
            newUser
        }
    }
    
    /**
     * 获取所有用户（分页）
     */
    fun getAllUsers(page: Int = 1, pageSize: Int = 20): List<User> {
        return transaction(DatabaseConfig.database) {
            Users.selectAll()
                .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
                .orderBy(Users.createdAt, SortOrder.DESC)
                .map { rowToUser(it) }
        }
    }
    
    /**
     * 获取用户总数
     */
    fun getUserCount(): Long {
        return transaction(DatabaseConfig.database) {
            Users.selectAll().count()
        }
    }
    
    /**
     * 删除用户
     */
    fun deleteUser(userId: Long): Boolean {
        return transaction(DatabaseConfig.database) {
            Users.deleteWhere { Users.id eq userId } > 0
        }
    }
    
    /**
     * 将数据库行转换为User对象
     */
    private fun rowToUser(row: ResultRow): User {
        return User(
            id = row[Users.id].value,
            githubId = row[Users.githubId],
            githubLogin = row[Users.githubLogin],
            avatarUrl = row[Users.avatarUrl],
            email = row[Users.email],
            name = row[Users.name],
            bio = row[Users.bio],
            createdAt = row[Users.createdAt].toString(),
            updatedAt = row[Users.updatedAt].toString()
        )
    }
}