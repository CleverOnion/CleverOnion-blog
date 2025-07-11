package com.cleveronion.service

import com.cleveronion.config.DatabaseConfig
import com.cleveronion.domain.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import kotlinx.serialization.Serializable

/**
 * 评论服务类
 * 负责处理评论相关的业务逻辑
 * 遵循单一职责原则和依赖反转原则
 */
class CommentService {
    
    /**
     * 创建评论
     * @param request 创建评论请求
     * @param userId 评论者用户ID
     * @return 创建的评论
     * @throws IllegalArgumentException 当参数无效时
     */
    fun createComment(request: CreateCommentRequest, userId: Long): Comment {
        validateCreateCommentRequest(request)
        
        return transaction(DatabaseConfig.database) {
            // 验证文章是否存在且已发布
            val article = Articles.select { Articles.id eq request.articleId }
                .singleOrNull()
                ?: throw IllegalArgumentException("文章不存在")
            
            if (article[Articles.status] != ArticleStatus.PUBLISHED) {
                throw IllegalArgumentException("只能对已发布的文章进行评论")
            }
            
            // 如果是回复评论，验证父评论是否存在且属于同一文章
            request.parentId?.let { parentId ->
                val parentComment = Comments.select { 
                    (Comments.id eq parentId) and (Comments.articleId eq request.articleId)
                }.singleOrNull()
                    ?: throw IllegalArgumentException("父评论不存在或不属于该文章")
            }
            
            val now = Instant.now()
            
            val commentId = Comments.insertAndGetId {
                it[Comments.articleId] = request.articleId
                it[Comments.userId] = userId
                it[Comments.content] = request.content.trim()
                it[Comments.parentId] = request.parentId
                it[Comments.createdAt] = now
            }
            
            // 返回创建的评论
            getCommentById(commentId.value)!!
        }
    }
    
    /**
     * 根据ID获取评论详情
     * @param commentId 评论ID
     * @return 评论详情，如果不存在则返回null
     */
    fun getCommentById(commentId: Long): Comment? {
        return transaction(DatabaseConfig.database) {
            Comments.select { Comments.id eq commentId }
                .singleOrNull()
                ?.let { rowToComment(it) }
        }
    }
    
    /**
     * 获取文章的评论列表（分页）
     * @param articleId 文章ID
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 评论列表
     */
    fun getCommentsByArticleId(articleId: Long, page: Int = 1, pageSize: Int = 20): List<CommentWithReplies> {
        return transaction(DatabaseConfig.database) {
            // 验证文章是否存在且已发布
            val article = Articles.select { Articles.id eq articleId }
                .singleOrNull()
                ?: throw IllegalArgumentException("文章不存在")
            
            if (article[Articles.status] != ArticleStatus.PUBLISHED) {
                throw IllegalArgumentException("只能查看已发布文章的评论")
            }
            
            val offset = (page - 1) * pageSize
            
            // 获取顶级评论（没有父评论的评论）
            val topLevelComments = Comments
                .select { 
                    (Comments.articleId eq articleId) and Comments.parentId.isNull()
                }
                .orderBy(Comments.createdAt, SortOrder.DESC)
                .limit(pageSize, offset.toLong())
                .map { rowToComment(it) }
            
            // 为每个顶级评论获取回复
            topLevelComments.map { comment ->
                val replies = Comments
                    .select { Comments.parentId eq comment.id }
                    .orderBy(Comments.createdAt, SortOrder.ASC)
                    .map { rowToComment(it) }
                
                CommentWithReplies(
                    comment = comment,
                    replies = replies
                )
            }
        }
    }
    
    /**
     * 获取文章的评论总数
     * @param articleId 文章ID
     * @return 评论总数
     */
    fun getCommentCountByArticleId(articleId: Long): Long {
        return transaction(DatabaseConfig.database) {
            Comments.select { Comments.articleId eq articleId }
                .count()
        }
    }
    
    /**
     * 获取用户的评论列表（分页）
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 评论列表
     */
    fun getCommentsByUserId(userId: Long, page: Int = 1, pageSize: Int = 20): List<Comment> {
        return transaction(DatabaseConfig.database) {
            val offset = (page - 1) * pageSize
            
            Comments
                .select { Comments.userId eq userId }
                .orderBy(Comments.createdAt, SortOrder.DESC)
                .limit(pageSize, offset.toLong())
                .map { rowToComment(it) }
        }
    }
    
    /**
     * 获取用户的评论总数
     * @param userId 用户ID
     * @return 评论总数
     */
    fun getCommentCountByUserId(userId: Long): Long {
        return transaction(DatabaseConfig.database) {
            Comments.select { Comments.userId eq userId }
                .count()
        }
    }
    
    /**
     * 更新评论内容
     * @param commentId 评论ID
     * @param newContent 新内容
     * @param userId 操作用户ID（只能更新自己的评论）
     * @return 更新后的评论，如果不存在或无权限则返回null
     */
    fun updateComment(commentId: Long, newContent: String, userId: Long): Comment? {
        if (newContent.isBlank()) {
            throw IllegalArgumentException("评论内容不能为空")
        }
        
        if (newContent.length > 1000) {
            throw IllegalArgumentException("评论内容不能超过1000个字符")
        }
        
        return transaction(DatabaseConfig.database) {
            // 验证评论是否存在且属于当前用户
            val existingComment = Comments.select { 
                (Comments.id eq commentId) and (Comments.userId eq userId)
            }.singleOrNull()
                ?: return@transaction null
            
            Comments.update({ Comments.id eq commentId }) {
                it[content] = newContent.trim()
            }
            
            getCommentById(commentId)
        }
    }
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 操作用户ID（只能删除自己的评论）
     * @return 是否删除成功
     */
    fun deleteComment(commentId: Long, userId: Long): Boolean {
        return transaction(DatabaseConfig.database) {
            // 验证评论是否存在且属于当前用户
            val existingComment = Comments.select { 
                (Comments.id eq commentId) and (Comments.userId eq userId)
            }.singleOrNull()
                ?: return@transaction false
            
            // 先删除所有回复
            Comments.deleteWhere { Comments.parentId eq commentId }
            
            // 再删除评论本身
            val deletedCount = Comments.deleteWhere { Comments.id eq commentId }
            
            deletedCount > 0
        }
    }
    
    /**
     * 管理员删除评论（可以删除任何评论）
     * @param commentId 评论ID
     * @return 是否删除成功
     */
    fun adminDeleteComment(commentId: Long): Boolean {
        return transaction(DatabaseConfig.database) {
            // 验证评论是否存在
            val existingComment = Comments.select { Comments.id eq commentId }
                .singleOrNull()
                ?: return@transaction false
            
            // 先删除所有回复
            Comments.deleteWhere { Comments.parentId eq commentId }
            
            // 再删除评论本身
            val deletedCount = Comments.deleteWhere { Comments.id eq commentId }
            
            deletedCount > 0
        }
    }
    
    /**
     * 获取最新评论列表（全站）
     * @param limit 限制数量
     * @return 最新评论列表
     */
    fun getLatestComments(limit: Int = 10): List<Comment> {
        return transaction(DatabaseConfig.database) {
            val validLimit = when {
                limit < 1 -> 10
                limit > 100 -> 100
                else -> limit
            }
            
            Comments
                .innerJoin(Articles) // 只显示已发布文章的评论
                .select { Articles.status eq ArticleStatus.PUBLISHED }
                .orderBy(Comments.createdAt, SortOrder.DESC)
                .limit(validLimit)
                .map { rowToComment(it) }
        }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 验证创建评论请求
     */
    private fun validateCreateCommentRequest(request: CreateCommentRequest) {
        if (request.content.isBlank()) {
            throw IllegalArgumentException("评论内容不能为空")
        }
        
        if (request.content.length > 1000) {
            throw IllegalArgumentException("评论内容不能超过1000个字符")
        }
        
        if (request.articleId <= 0) {
            throw IllegalArgumentException("无效的文章ID")
        }
        
        request.parentId?.let { parentId ->
            if (parentId <= 0) {
                throw IllegalArgumentException("无效的父评论ID")
            }
        }
    }
    
    /**
     * 将数据库行转换为Comment对象
     */
    private fun rowToComment(row: ResultRow): Comment {
        // 获取用户信息
        val user = Users.select { Users.id eq row[Comments.userId] }
            .single()
            .let { userRow ->
                User(
                    id = userRow[Users.id].value,
                    githubId = userRow[Users.githubId],
                    githubLogin = userRow[Users.githubLogin],
                    email = userRow[Users.email],
                    avatarUrl = userRow[Users.avatarUrl],
                    name = userRow[Users.name],
                    bio = userRow[Users.bio],
                    createdAt = userRow[Users.createdAt].toString(),
                    updatedAt = userRow[Users.updatedAt].toString()
                )
            }
        
        return Comment(
            id = row[Comments.id].value,
            articleId = row[Comments.articleId],
            userId = row[Comments.userId],
            content = row[Comments.content],
            parentId = row[Comments.parentId],
            createdAt = row[Comments.createdAt].toString(),
            author = user
        )
    }
}

// ========== 响应数据类 ==========

/**
 * 带回复的评论
 */
@Serializable
data class CommentWithReplies(
    val comment: Comment,
    val replies: List<Comment>
)