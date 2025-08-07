package com.cleveronion.domain.comment.port

import com.cleveronion.domain.comment.aggregate.Comment
import com.cleveronion.domain.comment.valueobject.CommentId
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.shared.valueobject.Pagination

/**
 * 评论仓储端口
 * 
 * 定义评论聚合的持久化操作接口，遵循六边形架构的端口模式。
 * 具体实现由基础设施层的适配器提供。
 */
interface CommentRepositoryPort {
    
    /**
     * 保存评论
     * 
     * @param comment 要保存的评论聚合
     * @return 保存后的评论聚合
     */
    suspend fun save(comment: Comment): Comment
    
    /**
     * 根据ID查找评论
     * 
     * @param id 评论ID
     * @return 找到的评论聚合，如果不存在则返回null
     */
    suspend fun findById(id: CommentId): Comment?
    
    /**
     * 根据ID删除评论
     * 
     * @param id 评论ID
     * @return 如果删除成功返回true，如果评论不存在返回false
     */
    suspend fun deleteById(id: CommentId): Boolean
    
    /**
     * 检查评论是否存在
     * 
     * @param id 评论ID
     * @return 如果评论存在返回true，否则返回false
     */
    suspend fun existsById(id: CommentId): Boolean
    
    /**
     * 根据文章ID查找评论列表
     * 
     * @param articleId 文章ID
     * @param pagination 分页参数
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 评论列表
     */
    suspend fun findByArticleId(
        articleId: ArticleId,
        pagination: Pagination,
        includeDeleted: Boolean = false
    ): List<Comment>
    
    /**
     * 根据用户ID查找评论列表
     * 
     * @param userId 用户ID
     * @param pagination 分页参数
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 评论列表
     */
    suspend fun findByUserId(
        userId: UserId,
        pagination: Pagination,
        includeDeleted: Boolean = false
    ): List<Comment>
    
    /**
     * 根据父评论ID查找回复列表
     * 
     * @param parentId 父评论ID
     * @param pagination 分页参数
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 回复列表
     */
    suspend fun findRepliesByParentId(
        parentId: CommentId,
        pagination: Pagination,
        includeDeleted: Boolean = false
    ): List<Comment>
    
    /**
     * 获取文章的顶级评论列表
     * 
     * @param articleId 文章ID
     * @param pagination 分页参数
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 顶级评论列表
     */
    suspend fun findTopLevelCommentsByArticleId(
        articleId: ArticleId,
        pagination: Pagination,
        includeDeleted: Boolean = false
    ): List<Comment>
    
    /**
     * 搜索评论
     * 
     * @param keyword 搜索关键词，在评论内容中搜索
     * @param pagination 分页参数
     * @param articleId 文章ID过滤，如果为null则不过滤
     * @param userId 用户ID过滤，如果为null则不过滤
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 评论列表
     */
    suspend fun search(
        keyword: String,
        pagination: Pagination,
        articleId: ArticleId? = null,
        userId: UserId? = null,
        includeDeleted: Boolean = false
    ): List<Comment>
    
    /**
     * 获取文章的评论总数
     * 
     * @param articleId 文章ID
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 评论总数
     */
    suspend fun countByArticleId(articleId: ArticleId, includeDeleted: Boolean = false): Long
    
    /**
     * 获取指定用户的评论总数
     * 
     * @param userId 用户ID
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 评论总数
     */
    suspend fun countByUserId(userId: UserId, includeDeleted: Boolean = false): Long
    
    /**
     * 获取指定评论的回复总数
     * 
     * @param parentId 父评论ID
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 回复总数
     */
    suspend fun countRepliesByParentId(parentId: CommentId, includeDeleted: Boolean = false): Long
    
    /**
     * 获取最新评论列表
     * 
     * @param pagination 分页参数
     * @param articleId 文章ID过滤，如果为null则不过滤
     * @param includeDeleted 是否包含已删除的评论，默认为false
     * @return 评论列表，按创建时间降序排列
     */
    suspend fun findLatest(
        pagination: Pagination,
        articleId: ArticleId? = null,
        includeDeleted: Boolean = false
    ): List<Comment>
    
    /**
     * 批量保存评论
     * 
     * @param comments 要保存的评论列表
     * @return 保存后的评论列表
     */
    suspend fun saveAll(comments: List<Comment>): List<Comment>
    
    /**
     * 批量删除评论
     * 
     * @param ids 要删除的评论ID列表
     * @return 实际删除的评论数量
     */
    suspend fun deleteByIds(ids: List<CommentId>): Int
    
    /**
     * 批量软删除评论
     * 
     * @param ids 要软删除的评论ID列表
     * @return 实际软删除的评论数量
     */
    suspend fun softDeleteByIds(ids: List<CommentId>): Int
    
    /**
     * 根据文章ID删除所有评论
     * 
     * @param articleId 文章ID
     * @return 删除的评论数量
     */
    suspend fun deleteByArticleId(articleId: ArticleId): Int
    
    /**
     * 根据用户ID删除所有评论
     * 
     * @param userId 用户ID
     * @return 删除的评论数量
     */
    suspend fun deleteByUserId(userId: UserId): Int
}