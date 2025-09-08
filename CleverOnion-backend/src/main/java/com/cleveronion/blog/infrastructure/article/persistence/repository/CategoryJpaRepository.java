package com.cleveronion.blog.infrastructure.article.persistence.repository;

import com.cleveronion.blog.infrastructure.article.persistence.po.CategoryPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类JPA仓储接口
 * 继承JpaRepository，提供基础的CRUD操作
 * 
 * @author CleverOnion
 */
@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryPO, Long> {
    
    /**
     * 根据名称查找分类
     * 
     * @param name 分类名称
     * @return 分类持久化对象的Optional包装
     */
    Optional<CategoryPO> findByName(String name);
    
    /**
     * 检查指定名称的分类是否存在
     * 
     * @param name 分类名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByName(String name);
    
    /**
     * 根据名称关键词搜索分类
     * 
     * @param keyword 关键词
     * @return 分类列表
     */
    List<CategoryPO> findByNameContainingIgnoreCase(String keyword);
    
    /**
     * 分页查询分类
     * 
     * @param pageable 分页参数
     * @return 分类列表
     */
    List<CategoryPO> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 查找最近创建的分类
     * 
     * @param pageable 分页参数
     * @return 分类列表
     */
    @Query("SELECT c FROM CategoryPO c ORDER BY c.createdAt DESC")
    List<CategoryPO> findRecentlyCreated(Pageable pageable);
    
    /**
     * 按名称排序查找所有分类
     * 
     * @return 分类列表（按名称升序）
     */
    List<CategoryPO> findAllByOrderByNameAsc();
    
    /**
     * 按名称排序查找所有分类
     * 
     * @return 分类列表（按名称降序）
     */
    List<CategoryPO> findAllByOrderByNameDesc();
    
    /**
     * 按创建时间排序查找所有分类
     * 
     * @return 分类列表（按创建时间升序）
     */
    List<CategoryPO> findAllByOrderByCreatedAtAsc();
    
    /**
     * 按创建时间排序查找所有分类
     * 
     * @return 分类列表（按创建时间降序）
     */
    List<CategoryPO> findAllByOrderByCreatedAtDesc();
    
    /**
     * 分页查询分类及其文章数量
     * 
     * @param pageable 分页参数
     * @return 分类及文章数量的对象数组列表
     */
    @Query("SELECT c, COALESCE(COUNT(a.id), 0) " +
           "FROM CategoryPO c LEFT JOIN ArticlePO a ON c.id = a.categoryId " +
           "GROUP BY c.id, c.name, c.createdAt, c.updatedAt " +
           "ORDER BY c.createdAt DESC")
    List<Object[]> findCategoriesWithArticleCount(Pageable pageable);
}