package com.cleveronion.blog.infrastructure.comment.persistence.repository;

import com.cleveronion.blog.infrastructure.comment.persistence.po.CommentPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论JPA仓储接口
 * 继承JpaRepository，提供基础的CRUD操作
 * 
 * @author CleverOnion
 */
@Repository
public interface CommentJpaRepository extends JpaRepository<CommentPO, Long> {
    
    /**
     * 根据文章ID查找评论列表
     * 
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<CommentPO> findByArticleId(Long articleId);
    
    /**
     * 分页根据文章ID查找评论列表
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 评论列表
     */
    List<CommentPO> findByArticleId(Long articleId, Pageable pageable);
    
    /**
     * 根据用户ID查找评论列表
     * 
     * @param userId 用户ID
     * @return 评论列表
     */
    List<CommentPO> findByUserId(Long userId);
    
    /**
     * 根据父评论ID查找回复列表
     * 
     * @param parentId 父评论ID
     * @return 回复列表
     */
    List<CommentPO> findByParentId(Long parentId);
    
    /**
     * 分页根据父评论ID查找回复列表
     * 
     * @param parentId 父评论ID
     * @param pageable 分页参数
     * @return 回复列表
     */
    List<CommentPO> findByParentId(Long parentId, Pageable pageable);
    
    /**
     * 查找顶级评论（parent_id为null）
     * 
     * @param articleId 文章ID
     * @return 顶级评论列表
     */
    List<CommentPO> findByArticleIdAndParentIdIsNull(Long articleId);
    
    /**
     * 根据文章ID和父评论ID查找回复
     * 
     * @param articleId 文章ID
     * @param parentId 父评论ID
     * @return 回复列表
     */
    List<CommentPO> findByArticleIdAndParentId(Long articleId, Long parentId);
    
    /**
     * 根据文章ID查找评论，按创建时间排序
     * 
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<CommentPO> findByArticleIdOrderByCreatedAtAsc(Long articleId);
    
    /**
     * 根据文章ID查找顶级评论，按创建时间排序
     * 
     * @param articleId 文章ID
     * @return 顶级评论列表
     */
    List<CommentPO> findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(Long articleId);
    
    /**
     * 根据用户ID查找评论，按创建时间倒序排列
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评论列表
     */
    List<CommentPO> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 统计文章的评论总数
     * 
     * @param articleId 文章ID
     * @return 评论总数
     */
    long countByArticleId(Long articleId);
    
    /**
     * 统计文章的顶级评论数
     * 
     * @param articleId 文章ID
     * @return 顶级评论数
     */
    long countByArticleIdAndParentIdIsNull(Long articleId);
    
    /**
     * 统计某个评论的回复数
     * 
     * @param parentId 父评论ID
     * @return 回复数
     */
    long countByParentId(Long parentId);
    
    /**
     * 统计用户的评论总数
     * 
     * @param userId 用户ID
     * @return 评论总数
     */
    long countByUserId(Long userId);
    
    /**
     * 查找最近的评论
     * 
     * @param pageable 分页参数
     * @return 最近评论列表
     */
    List<CommentPO> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据内容关键词搜索评论
     * 
     * @param keyword 关键词
     * @return 评论列表
     */
    List<CommentPO> findByContentContainingIgnoreCase(String keyword);
    
    /**
     * 检查评论是否存在
     * 
     * @param id 评论ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);
    
    /**
     * 删除文章的所有评论
     * 
     * @param articleId 文章ID
     */
    void deleteByArticleId(Long articleId);
    
    /**
     * 删除某个评论及其所有回复
     * 使用自定义查询来实现级联删除
     * 
     * @param parentId 父评论ID
     */
    @Query("DELETE FROM CommentPO c WHERE c.id = :parentId OR c.parentId = :parentId")
    void deleteByIdOrParentId(@Param("parentId") Long parentId);
}
