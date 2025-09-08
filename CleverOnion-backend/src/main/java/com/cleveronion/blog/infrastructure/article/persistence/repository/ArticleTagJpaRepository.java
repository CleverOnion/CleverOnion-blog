package com.cleveronion.blog.infrastructure.article.persistence.repository;

import com.cleveronion.blog.infrastructure.article.persistence.po.ArticleTagPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文章标签关联JPA仓储接口
 * 继承JpaRepository，提供基础的CRUD操作
 * 
 * @author CleverOnion
 */
@Repository
public interface ArticleTagJpaRepository extends JpaRepository<ArticleTagPO, Long> {
    
    /**
     * 根据文章ID查找所有关联的标签ID
     * 
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    @Query("SELECT at.tagId FROM ArticleTagPO at WHERE at.articleId = :articleId")
    List<Long> findTagIdsByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 根据标签ID查找所有关联的文章ID
     * 
     * @param tagId 标签ID
     * @return 文章ID列表
     */
    @Query("SELECT at.articleId FROM ArticleTagPO at WHERE at.tagId = :tagId")
    List<Long> findArticleIdsByTagId(@Param("tagId") Long tagId);
    
    /**
     * 根据文章ID查找所有关联记录
     * 
     * @param articleId 文章ID
     * @return 关联记录列表
     */
    List<ArticleTagPO> findByArticleId(Long articleId);
    
    /**
     * 根据标签ID查找所有关联记录
     * 
     * @param tagId 标签ID
     * @return 关联记录列表
     */
    List<ArticleTagPO> findByTagId(Long tagId);
    
    /**
     * 检查文章和标签的关联是否存在
     * 
     * @param articleId 文章ID
     * @param tagId 标签ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByArticleIdAndTagId(Long articleId, Long tagId);
    
    /**
     * 根据文章ID删除所有关联记录
     * 
     * @param articleId 文章ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ArticleTagPO at WHERE at.articleId = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 根据标签ID删除所有关联记录
     * 
     * @param tagId 标签ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ArticleTagPO at WHERE at.tagId = :tagId")
    void deleteByTagId(@Param("tagId") Long tagId);
    
    /**
     * 删除特定的文章标签关联
     * 
     * @param articleId 文章ID
     * @param tagId 标签ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ArticleTagPO at WHERE at.articleId = :articleId AND at.tagId = :tagId")
    void deleteByArticleIdAndTagId(@Param("articleId") Long articleId, @Param("tagId") Long tagId);
    
    /**
     * 统计文章的标签数量
     * 
     * @param articleId 文章ID
     * @return 标签数量
     */
    long countByArticleId(Long articleId);
    
    /**
     * 统计标签关联的文章数量
     * 
     * @param tagId 标签ID
     * @return 文章数量
     */
    long countByTagId(Long tagId);
    
    /**
     * 批量查询多个文章的标签关联信息
     * 
     * @param articleIds 文章ID列表
     * @return 文章标签关联记录列表
     */
    @Query("SELECT at FROM ArticleTagPO at WHERE at.articleId IN :articleIds")
    List<ArticleTagPO> findByArticleIdIn(@Param("articleIds") List<Long> articleIds);
}