package com.cleveronion.blog.infrastructure.article.persistence.repository;

import com.cleveronion.blog.domain.article.valueobject.ArticleStatus;
import com.cleveronion.blog.infrastructure.article.persistence.po.ArticlePO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文章JPA仓储接口
 * 继承JpaRepository，提供基础的CRUD操作
 * 
 * @author CleverOnion
 */
@Repository
public interface ArticleJpaRepository extends JpaRepository<ArticlePO, Long> {
    
    /**
     * 根据作者ID查找文章列表
     * 
     * @param authorId 作者ID
     * @return 文章列表
     */
    List<ArticlePO> findByAuthorId(Long authorId);
    
    /**
     * 根据分类ID查找文章列表
     * 
     * @param categoryId 分类ID
     * @return 文章列表
     */
    List<ArticlePO> findByCategoryId(Long categoryId);
    
    /**
     * 根据状态查找文章列表
     * 
     * @param status 文章状态
     * @return 文章列表
     */
    List<ArticlePO> findByStatus(String status);
    
    /**
     * 根据作者ID和状态查找文章列表
     * 
     * @param authorId 作者ID
     * @param status 文章状态
     * @return 文章列表
     */
    List<ArticlePO> findByAuthorIdAndStatus(Long authorId, String status);
    
    /**
     * 根据分类ID和状态查找文章列表
     * 
     * @param categoryId 分类ID
     * @param status 文章状态
     * @return 文章列表
     */
    List<ArticlePO> findByCategoryIdAndStatus(Long categoryId, String status);
    
    /**
     * 根据标题关键词查找文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    List<ArticlePO> findByTitleContainingIgnoreCase(String keyword);
    
    /**
     * 根据内容关键词查找文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    List<ArticlePO> findByContentContainingIgnoreCase(String keyword);
    
    /**
     * 根据状态查找文章，按创建时间倒序排列（分页）
     * 
     * @param status 文章状态
     * @param pageable 分页参数
     * @return 文章列表
     */
    List<ArticlePO> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * 根据状态查找文章，按创建时间倒序排列（分页，优化版本：不查询content字段）
     * 
     * @param status 文章状态
     * @param pageable 分页参数
     * @return 文章列表（不包含content字段）
     */
    @Query("SELECT new com.cleveronion.blog.infrastructure.article.persistence.po.ArticlePO(a.id, a.title, a.summary, a.status, a.categoryId, a.authorId, a.createdAt, a.updatedAt) FROM ArticlePO a WHERE a.status = :status ORDER BY a.createdAt DESC")
    List<ArticlePO> findByStatusOrderByCreatedAtDescOptimized(@Param("status") String status, Pageable pageable);
    
    /**
     * 查询所有文章，按创建时间倒序排列（分页）
     * 
     * @param pageable 分页参数
     * @return 文章列表
     */
    List<ArticlePO> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 查询所有文章，按创建时间倒序排列（分页，优化版本：不查询content字段）
     * 
     * @param pageable 分页参数
     * @return 文章列表（不包含content字段）
     */
    @Query("SELECT new com.cleveronion.blog.infrastructure.article.persistence.po.ArticlePO(a.id, a.title, a.summary, a.status, a.categoryId, a.authorId, a.createdAt, a.updatedAt) FROM ArticlePO a ORDER BY a.createdAt DESC")
    List<ArticlePO> findAllByOrderByCreatedAtDescOptimized(Pageable pageable);
    

    
    /**
     * 根据分类ID查找文章，按创建时间倒序排列（分页）
     * 
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 文章列表
     */
    List<ArticlePO> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);
    
    /**
     * 根据分类ID查找文章，按创建时间倒序排列（分页，优化版本：不查询content字段）
     * 
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 文章列表（不包含content字段）
     */
    @Query("SELECT new com.cleveronion.blog.infrastructure.article.persistence.po.ArticlePO(a.id, a.title, a.summary, a.status, a.categoryId, a.authorId, a.createdAt, a.updatedAt) FROM ArticlePO a WHERE a.categoryId = :categoryId ORDER BY a.createdAt DESC")
    List<ArticlePO> findByCategoryIdOrderByCreatedAtDescOptimized(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * 分页查找指定标签的文章（支持状态筛选）
     * 
     * @param tagId 标签ID
     * @param status 文章状态
     * @param pageable 分页参数
     * @return 文章列表
     */
    @Query("SELECT a FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE at.tagId = :tagId AND a.status = :status ORDER BY a.createdAt DESC")
    List<ArticlePO> findByTagIdAndStatusOrderByCreatedAtDesc(@Param("tagId") Long tagId, @Param("status") String status, Pageable pageable);
    
    /**
     * 分页查找指定标签的所有文章
     * 
     * @param tagId 标签ID
     * @param pageable 分页参数
     * @return 文章列表
     */
    @Query("SELECT a FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE at.tagId = :tagId ORDER BY a.createdAt DESC")
    List<ArticlePO> findByTagIdOrderByCreatedAtDesc(@Param("tagId") Long tagId, Pageable pageable);
    
    /**
     * 分页查找指定作者的文章
     * 
     * @param authorId 作者ID
     * @param pageable 分页参数
     * @return 文章列表
     */
    List<ArticlePO> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    /**
     * 分页查找指定分类的已发布文章
     * 
     * @param categoryId 分类ID
     * @param status 文章状态
     * @param pageable 分页参数
     * @return 文章列表
     */
    List<ArticlePO> findByCategoryIdAndStatusOrderByCreatedAtDesc(Long categoryId, String status, Pageable pageable);
    
    /**
     * 统计指定作者的文章数量
     * 
     * @param authorId 作者ID
     * @return 文章数量
     */
    long countByAuthorId(Long authorId);
    
    /**
     * 统计指定分类的文章数量
     * 
     * @param categoryId 分类ID
     * @return 文章数量
     */
    long countByCategoryId(Long categoryId);
    
    /**
     * 统计指定分类和状态的文章数量
     * 
     * @param categoryId 分类ID
     * @param status 文章状态
     * @return 文章数量
     */
    long countByCategoryIdAndStatus(Long categoryId, String status);
    
    /**
     * 统计指定状态的文章数量
     * 
     * @param status 文章状态
     * @return 文章数量
     */
    long countByStatus(String status);
    
    /**
     * 查找最近发布的文章
     * 
     * @param status 文章状态
     * @param pageable 分页参数
     * @return 文章列表
     */
    @Query("SELECT a FROM ArticlePO a WHERE a.status = :status ORDER BY a.createdAt DESC")
    List<ArticlePO> findRecentlyPublished(@Param("status") String status, Pageable pageable);
    
    /**
     * 根据标签ID查找文章列表
     * 
     * @param tagId 标签ID
     * @return 文章列表
     */
    @Query("SELECT a FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE at.tagId = :tagId")
    List<ArticlePO> findByTagId(@Param("tagId") Long tagId);
    

    
    /**
     * 统计指定标签的文章数量
     * 
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Query("SELECT COUNT(a) FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE at.tagId = :tagId")
    long countByTagId(@Param("tagId") Long tagId);
    
    /**
     * 统计指定标签和状态的文章数量
     * 
     * @param tagId 标签ID
     * @param status 文章状态
     * @return 文章数量
     */
    @Query("SELECT COUNT(a) FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE at.tagId = :tagId AND a.status = :status")
    long countByTagIdAndStatus(@Param("tagId") Long tagId, @Param("status") String status);
    
    /**
     * 同时按分类和标签查找已发布文章
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param status 文章状态
     * @param pageable 分页参数
     * @return 文章列表
     */
    @Query("SELECT a FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE a.categoryId = :categoryId AND at.tagId = :tagId AND a.status = :status ORDER BY a.createdAt DESC")
    List<ArticlePO> findByCategoryIdAndTagIdAndStatusOrderByCreatedAtDesc(@Param("categoryId") Long categoryId, @Param("tagId") Long tagId, @Param("status") String status, Pageable pageable);
    
    /**
     * 统计同时属于指定分类和标签的已发布文章数量
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Query("SELECT COUNT(a) FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE a.categoryId = :categoryId AND at.tagId = :tagId AND a.status = 'PUBLISHED'")
    long countByCategoryIdAndTagId(@Param("categoryId") Long categoryId, @Param("tagId") Long tagId);
    
    /**
     * 统计指定分类、标签和状态的文章数量
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param status 文章状态
     * @return 文章数量
     */
    @Query("SELECT COUNT(a) FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE a.categoryId = :categoryId AND at.tagId = :tagId AND a.status = :status")
    long countByCategoryIdAndTagIdAndStatus(@Param("categoryId") Long categoryId, @Param("tagId") Long tagId, @Param("status") ArticleStatus status);
    
    /**
     * 根据分类ID和标签ID查找文章（所有状态）
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param pageable 分页参数
     * @return 文章列表
     */
    @Query("SELECT a FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE a.categoryId = :categoryId AND at.tagId = :tagId ORDER BY a.createdAt DESC")
    List<ArticlePO> findByCategoryIdAndTagIdOrderByCreatedAtDesc(@Param("categoryId") Long categoryId, @Param("tagId") Long tagId, Pageable pageable);

    /**
     * 统计指定分类和标签的文章数量（所有状态）
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Query("SELECT COUNT(a) FROM ArticlePO a JOIN ArticleTagPO at ON a.id = at.articleId WHERE a.categoryId = :categoryId AND at.tagId = :tagId")
    long countByCategoryIdAndTagIdAllStatuses(@Param("categoryId") Long categoryId, @Param("tagId") Long tagId);
}