package com.cleveronion.domain.comment.service

import com.cleveronion.domain.comment.aggregate.Comment
import com.cleveronion.domain.comment.valueobject.CommentId
import com.cleveronion.domain.comment.valueobject.CommentContent
import com.cleveronion.domain.comment.port.CommentRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.shared.exception.BusinessRuleViolationException
import com.cleveronion.domain.shared.exception.EntityNotFoundException
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException

/**
 * 评论领域服务
 * 
 * 封装跨评论聚合的业务逻辑，处理复杂的业务规则和操作。
 * 领域服务用于处理不属于单个聚合的业务逻辑。
 */
class CommentDomainService(
    private val commentRepository: CommentRepositoryPort
) {
    
    companion object {
        /**
         * 最大回复深度
         */
        const val MAX_REPLY_DEPTH = 1
        
        /**
         * 用户每日最大评论数量
         */
        const val MAX_COMMENTS_PER_DAY = 50
    }
    
    /**
     * 验证评论创建的业务规则
     * 
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param content 评论内容
     * @param parentId 父评论ID，如果为null则为顶级评论
     * @throws BusinessRuleViolationException 如果不满足创建条件
     */
    suspend fun validateCommentCreation(
        articleId: ArticleId,
        userId: UserId,
        content: CommentContent,
        parentId: CommentId? = null
    ) {
        // 验证内容长度
        if (content.isTooShort()) {
            throw BusinessRuleViolationException("Comment content is too short")
        }
        
        if (content.isTooLong()) {
            throw BusinessRuleViolationException("Comment content is too long")
        }
        
        // 如果是回复评论，验证父评论存在且属于同一文章
        parentId?.let { parent ->
            val parentComment = commentRepository.findById(parent)
                ?: throw EntityNotFoundException("Parent comment not found: ${parent.value}")
            
            if (!parentComment.belongsToArticle(articleId)) {
                throw BusinessRuleViolationException("Parent comment does not belong to the same article")
            }
            
            if (parentComment.isDeleted()) {
                throw BusinessRuleViolationException("Cannot reply to deleted comment")
            }
            
            // 验证回复深度（只允许一级回复）
            if (parentComment.isReply()) {
                throw BusinessRuleViolationException("Cannot reply to a reply comment (max depth exceeded)")
            }
        }
        
        // 验证用户每日评论数量限制
        val todayCommentsCount = getUserTodayCommentsCount(userId)
        if (todayCommentsCount >= MAX_COMMENTS_PER_DAY) {
            throw BusinessRuleViolationException("Daily comment limit exceeded")
        }
    }
    
    /**
     * 检查用户是否有权限操作指定评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @param requireEditPermission 是否需要编辑权限，默认为false
     * @return 如果有权限返回评论实例
     * @throws EntityNotFoundException 如果评论不存在
     * @throws UnauthorizedOperationException 如果用户没有权限
     */
    suspend fun checkUserPermission(
        commentId: CommentId,
        userId: UserId,
        requireEditPermission: Boolean = false
    ): Comment {
        val comment = commentRepository.findById(commentId)
            ?: throw EntityNotFoundException("Comment not found: ${commentId.value}")
        
        // 检查是否为评论作者
        if (!comment.isCreatedBy(userId)) {
            throw UnauthorizedOperationException("User does not have permission to access this comment")
        }
        
        // 如果需要编辑权限，检查评论是否可编辑
        if (requireEditPermission && !comment.canBeEditedBy(userId)) {
            throw UnauthorizedOperationException("Comment cannot be edited")
        }
        
        return comment
    }
    
    /**
     * 验证评论更新的业务规则
     * 
     * @param comment 要更新的评论
     * @param newContent 新内容
     * @param userId 执行更新的用户ID
     * @throws BusinessRuleViolationException 如果不满足更新条件
     * @throws UnauthorizedOperationException 如果用户没有权限
     */
    suspend fun validateCommentUpdate(
        comment: Comment,
        newContent: CommentContent,
        userId: UserId
    ) {
        // 检查用户权限
        if (!comment.canBeEditedBy(userId)) {
            throw UnauthorizedOperationException("User cannot edit this comment")
        }
        
        // 验证新内容
        if (newContent.isTooShort()) {
            throw BusinessRuleViolationException("Comment content is too short")
        }
        
        if (newContent.isTooLong()) {
            throw BusinessRuleViolationException("Comment content is too long")
        }
        
        // 检查评论是否已删除
        if (comment.isDeleted()) {
            throw BusinessRuleViolationException("Cannot update deleted comment")
        }
        
        // 检查是否在允许编辑的时间窗口内（例如：创建后24小时内）
        val hoursSinceCreation = java.time.Duration.between(
            comment.getCreatedAt().value,
            java.time.Instant.now()
        ).toHours()
        
        if (hoursSinceCreation > 24) {
            throw BusinessRuleViolationException("Comment can only be edited within 24 hours of creation")
        }
    }
    
    /**
     * 验证评论删除的业务规则
     * 
     * @param comment 要删除的评论
     * @param userId 执行删除的用户ID
     * @param isAdmin 用户是否为管理员
     * @throws BusinessRuleViolationException 如果不能删除
     * @throws UnauthorizedOperationException 如果用户没有权限
     */
    suspend fun validateCommentDeletion(
        comment: Comment,
        userId: UserId,
        isAdmin: Boolean = false
    ) {
        // 检查用户权限（作者或管理员可以删除）
        if (!comment.canBeDeletedBy(userId) && !isAdmin) {
            throw UnauthorizedOperationException("User cannot delete this comment")
        }
        
        // 检查评论是否已删除
        if (comment.isDeleted()) {
            throw BusinessRuleViolationException("Comment is already deleted")
        }
        
        // 如果评论有回复，需要特殊处理
        val repliesCount = commentRepository.countRepliesByParentId(comment.getId())
        if (repliesCount > 0) {
            // 可以选择：1. 禁止删除有回复的评论，2. 级联删除回复，3. 只软删除父评论
            // 这里选择软删除，保留回复的上下文
            // 实际业务中可能需要更复杂的处理逻辑
        }
    }
    
    /**
     * 检查评论内容是否包含敏感词汇
     * 
     * 这是一个简化的实现，实际项目中可能需要更复杂的内容审核系统
     * 
     * @param content 评论内容
     * @return 如果包含敏感词汇返回true，否则返回false
     */
    fun containsSensitiveContent(content: CommentContent): Boolean {
        // 简化的敏感词列表
        val sensitiveWords = listOf(
            "spam", "垃圾", "广告", "推广"
        )
        
        return sensitiveWords.any { word ->
            content.contains(word, ignoreCase = true)
        }
    }
    
    /**
     * 检查评论是否为垃圾评论
     * 
     * @param comment 评论实例
     * @return 如果是垃圾评论返回true，否则返回false
     */
    suspend fun isSpamComment(comment: Comment): Boolean {
        // 检查内容是否包含敏感词汇
        if (containsSensitiveContent(comment.getContent())) {
            return true
        }
        
        // 检查用户是否在短时间内发布了大量相似评论
        val recentComments = commentRepository.findByUserId(
            comment.getUserId(),
            com.cleveronion.domain.shared.valueobject.Pagination(page = 1, pageSize = 10)
        )
        
        val similarCommentsCount = recentComments.count { recentComment ->
            val similarity = calculateContentSimilarity(
                comment.getContent().value,
                recentComment.getContent().value
            )
            similarity > 0.8 // 80%相似度阈值
        }
        
        return similarCommentsCount >= 3
    }
    
    /**
     * 获取用户今日评论数量
     * 
     * @param userId 用户ID
     * @return 今日评论数量
     */
    private suspend fun getUserTodayCommentsCount(userId: UserId): Long {
        val today = java.time.LocalDate.now()
        val startTime = today.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toString()
        val endTime = today.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toString()
        
        val userComments = commentRepository.findByUserId(
            userId = userId,
            pagination = com.cleveronion.domain.shared.valueobject.Pagination(page = 1, pageSize = 100)
        )
        
        // Filter comments created today
        val todayComments = userComments.filter { comment ->
            val commentDate = java.time.LocalDate.ofInstant(
                comment.getCreatedAt().value,
                java.time.ZoneOffset.UTC
            )
            commentDate == today
        }
        
        return todayComments.size.toLong()
    }
    
    /**
     * 计算两个文本的相似度
     * 
     * 简化的相似度计算，实际项目中可能需要更复杂的算法
     * 
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度（0-1之间）
     */
    private fun calculateContentSimilarity(text1: String, text2: String): Double {
        if (text1 == text2) return 1.0
        if (text1.isEmpty() || text2.isEmpty()) return 0.0
        
        val words1 = text1.lowercase().split("\\s+".toRegex()).toSet()
        val words2 = text2.lowercase().split("\\s+".toRegex()).toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return if (union == 0) 0.0 else intersection.toDouble() / union.toDouble()
    }
    
    /**
     * 获取评论的质量评分
     * 
     * @param comment 评论实例
     * @return 质量评分（0-100）和改进建议列表
     */
    suspend fun getCommentQualityScore(comment: Comment): Pair<Double, List<String>> {
        val suggestions = mutableListOf<String>()
        var score = 100.0
        
        val content = comment.getContent()
        
        // 检查内容长度
        if (content.length() < 10) {
            suggestions.add("评论内容过短，建议提供更详细的观点")
            score -= 20
        }
        
        // 检查是否包含敏感内容
        if (containsSensitiveContent(content)) {
            suggestions.add("评论包含不当内容，请修改")
            score -= 30
        }
        
        // 检查是否为垃圾评论
        if (isSpamComment(comment)) {
            suggestions.add("评论疑似垃圾内容")
            score -= 40
        }
        
        // 检查内容的建设性（简化判断）
        val constructiveWords = listOf("建议", "认为", "觉得", "分析", "总结", "学习")
        val hasConstructiveWords = constructiveWords.any { word ->
            content.contains(word, ignoreCase = true)
        }
        
        if (!hasConstructiveWords && content.length() > 20) {
            suggestions.add("建议提供更具建设性的观点")
            score -= 10
        }
        
        return Pair(maxOf(score, 0.0), suggestions)
    }
}