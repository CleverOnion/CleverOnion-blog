package com.cleveronion.blog.domain.comment.repository;

import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.user.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 评论仓储接口
 * 定义评论聚合的持久化操作
 * 使用领域模型作为参数和返回值，保持领域层的纯净性
 * 
 * @author CleverOnion
 */
public interface CommentRepository {
    
    /**
     * 保存评论聚合
     * 
     * @param comment 评论聚合根
     * @return 保存后的评论聚合根（包含生成的ID）
     */
    CommentAggregate save(CommentAggregate comment);
    
    /**
     * 根据ID查找评论
     * 
     * @param id 评论ID
     * @return 评论聚合根（如果存在）
     */
    Optional<CommentAggregate> findById(CommentId id);
    
    /**
     * 根据ID删除评论
     * 
     * @param id 评论ID
     */
    void deleteById(CommentId id);
    
    /**
     * 检查评论是否存在
     * 
     * @param id 评论ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(CommentId id);
    
    /**
     * 根据文章ID查找所有评论
     * 
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<CommentAggregate> findByArticleId(ArticleId articleId);
    
    /**
     * 分页查找文章的所有评论
     * 
     * @param articleId 文章ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 评论列表
     */
    List<CommentAggregate> findByArticleId(ArticleId articleId, int page, int size);
    
    /**
     * 根据用户ID查找评论列表
     * 
     * @param userId 用户ID
     * @return 评论列表
     */
    List<CommentAggregate> findByUserId(UserId userId);
    
    /**
     * 根据父评论ID查找子评论列表
     * 
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    List<CommentAggregate> findByParentId(CommentId parentId);
    
    /**
     * 分页查找父评论的回复
     * 
     * @param parentId 父评论ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 回复列表
     */
    List<CommentAggregate> findByParentId(CommentId parentId, int page, int size);
    
    /**
     * 查找文章的顶级评论（没有父评论的评论）
     * 
     * @param articleId 文章ID
     * @return 顶级评论列表
     */
    List<CommentAggregate> findTopLevelCommentsByArticleId(ArticleId articleId);
    
    /**
     * 分页查找文章的顶级评论
     * 
     * @param articleId 文章ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 顶级评论列表
     */
    List<CommentAggregate> findTopLevelCommentsByArticleId(ArticleId articleId, int page, int size);
    
    /**
     * 统计文章的评论总数（包括回复）
     * 
     * @param articleId 文章ID
     * @return 评论总数
     */
    long countByArticleId(ArticleId articleId);
    
    /**
     * 统计文章的顶级评论数量
     * 
     * @param articleId 文章ID
     * @return 顶级评论数量
     */
    long countTopLevelCommentsByArticleId(ArticleId articleId);
    
    /**
     * 统计某个评论的回复数量
     * 
     * @param parentId 父评论ID
     * @return 回复数量
     */
    long countRepliesByParentId(CommentId parentId);
    
    /**
     * 统计用户的评论总数
     * 
     * @param userId 用户ID
     * @return 评论总数
     */
    long countByUserId(UserId userId);
    
    /**
     * 查找用户最近的评论
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近评论列表
     */
    List<CommentAggregate> findRecentCommentsByUserId(UserId userId, int limit);
    
    /**
     * 查找文章最近的评论
     * 
     * @param articleId 文章ID
     * @param limit 限制数量
     * @return 最近评论列表
     */
    List<CommentAggregate> findRecentCommentsByArticleId(ArticleId articleId, int limit);
    
    /**
     * 根据内容关键词搜索评论
     * 
     * @param keyword 关键词
     * @return 评论列表
     */
    List<CommentAggregate> findByContentContaining(String keyword);
    
    /**
     * 分页查找用户的评论
     * 
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 评论列表
     */
    List<CommentAggregate> findByUserId(UserId userId, int page, int size);
    
    /**
     * 检查评论是否属于指定用户
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 如果评论属于该用户返回true，否则返回false
     */
    boolean isCommentOwnedByUser(CommentId commentId, UserId userId);
}