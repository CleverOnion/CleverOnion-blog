package com.cleveronion.blog.infrastructure.article.persistence.repository;

import com.cleveronion.blog.infrastructure.article.persistence.po.TagPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 标签JPA仓储接口
 * 继承JpaRepository，提供基础的CRUD操作
 * 
 * @author CleverOnion
 */
@Repository
public interface TagJpaRepository extends JpaRepository<TagPO, Long> {
    
    /**
     * 根据名称查找标签
     * 
     * @param name 标签名称
     * @return 标签持久化对象的Optional包装
     */
    Optional<TagPO> findByName(String name);
    
    /**
     * 根据ID集合查找标签列表
     * 
     * @param ids 标签ID集合
     * @return 标签列表
     */
    List<TagPO> findByIdIn(Set<Long> ids);
    
    /**
     * 根据名称集合查找标签列表
     * 
     * @param names 标签名称集合
     * @return 标签列表
     */
    List<TagPO> findByNameIn(Set<String> names);
    
    /**
     * 检查指定名称的标签是否存在
     * 
     * @param name 标签名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByName(String name);
    
    /**
     * 根据名称关键词搜索标签（忽略大小写）
     * 
     * @param keyword 关键词
     * @return 标签列表
     */
    List<TagPO> findByNameContainingIgnoreCase(String keyword);
    
    /**
     * 根据名称前缀查找标签（忽略大小写）
     * 
     * @param prefix 前缀
     * @return 标签列表
     */
    List<TagPO> findByNameStartingWithIgnoreCase(String prefix);
    
    /**
     * 分页查询标签
     * 
     * @param pageable 分页参数
     * @return 标签列表
     */
    List<TagPO> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 查找最近创建的标签
     * 
     * @param pageable 分页参数
     * @return 标签列表
     */
    @Query("SELECT t FROM TagPO t ORDER BY t.createdAt DESC")
    List<TagPO> findRecentlyCreated(Pageable pageable);
    
    /**
     * 按名称排序查找所有标签
     * 
     * @return 标签列表（按名称升序）
     */
    List<TagPO> findAllByOrderByNameAsc();
    
    /**
     * 按名称排序查找所有标签
     * 
     * @return 标签列表（按名称降序）
     */
    List<TagPO> findAllByOrderByNameDesc();
    
    /**
     * 按创建时间排序查找所有标签
     * 
     * @return 标签列表（按创建时间升序）
     */
    List<TagPO> findAllByOrderByCreatedAtAsc();
    
    /**
     * 按创建时间排序查找所有标签
     * 
     * @return 标签列表（按创建时间降序）
     */
    List<TagPO> findAllByOrderByCreatedAtDesc();
    
    /**
     * 查找热门标签（根据使用次数）
     * 
     * @param pageable 分页参数
     * @return 标签列表
     */
    @Query("SELECT t FROM TagPO t JOIN ArticleTagPO at ON t.id = at.tagId " +
           "GROUP BY t.id ORDER BY COUNT(at.articleId) DESC")
    List<TagPO> findPopularTags(Pageable pageable);
    
    /**
     * 统计标签使用次数
     * 
     * @param tagId 标签ID
     * @return 使用次数
     */
    @Query("SELECT COUNT(at) FROM ArticleTagPO at WHERE at.tagId = :tagId")
    long countUsageByTagId(@Param("tagId") Long tagId);
    
    /**
     * 分页查询标签及其文章数量
     * 
     * @param pageable 分页参数
     * @return 标签及文章数量的对象数组列表
     */
    @Query("SELECT t, COALESCE(COUNT(at.articleId), 0) " +
           "FROM TagPO t LEFT JOIN ArticleTagPO at ON t.id = at.tagId " +
           "GROUP BY t.id, t.name, t.createdAt, t.updatedAt " +
           "ORDER BY t.createdAt DESC")
    List<Object[]> findTagsWithArticleCount(Pageable pageable);
}