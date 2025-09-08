package com.cleveronion.blog.domain.article.repository;

import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储接口
 * 定义分类聚合的持久化操作
 * 使用领域模型作为参数和返回值，保持领域层的纯净性
 * 
 * @author CleverOnion
 */
public interface CategoryRepository {
    
    /**
     * 保存分类聚合
     * 
     * @param category 分类聚合根
     * @return 保存后的分类聚合根（包含生成的ID）
     */
    CategoryAggregate save(CategoryAggregate category);
    
    /**
     * 根据ID查找分类
     * 
     * @param id 分类ID
     * @return 分类聚合根的Optional包装
     */
    Optional<CategoryAggregate> findById(CategoryId id);
    
    /**
     * 根据名称查找分类
     * 
     * @param name 分类名称
     * @return 分类聚合根的Optional包装
     */
    Optional<CategoryAggregate> findByName(String name);
    
    /**
     * 根据ID删除分类
     * 
     * @param id 分类ID
     */
    void deleteById(CategoryId id);
    
    /**
     * 检查分类是否存在
     * 
     * @param id 分类ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(CategoryId id);
    
    /**
     * 检查指定名称的分类是否存在
     * 
     * @param name 分类名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByName(String name);
    
    /**
     * 查找所有分类
     * 
     * @return 分类列表
     */
    List<CategoryAggregate> findAll();
    
    /**
     * 根据名称关键词搜索分类
     * 
     * @param keyword 关键词
     * @return 分类列表
     */
    List<CategoryAggregate> findByNameContaining(String keyword);
    
    /**
     * 分页查询分类
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类列表
     */
    List<CategoryAggregate> findAll(int page, int size);
    
    /**
     * 统计分类总数
     * 
     * @return 分类总数
     */
    long count();
    
    /**
     * 查找最近创建的分类
     * 
     * @param limit 限制数量
     * @return 分类列表
     */
    List<CategoryAggregate> findRecentlyCreated(int limit);
    
    /**
     * 按名称排序查找所有分类
     * 
     * @param ascending 是否升序排列
     * @return 分类列表
     */
    List<CategoryAggregate> findAllOrderByName(boolean ascending);
    
    /**
     * 按创建时间排序查找所有分类
     * 
     * @param ascending 是否升序排列
     * @return 分类列表
     */
    List<CategoryAggregate> findAllOrderByCreatedAt(boolean ascending);
    
    /**
     * 分页查询分类及其文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类及文章数量的映射
     */
    List<CategoryWithArticleCount> findCategoriesWithArticleCount(int page, int size);
    
    /**
     * 分类及其文章数量的数据传输对象
     */
    class CategoryWithArticleCount {
        private final CategoryAggregate category;
        private final Long articleCount;
        
        public CategoryWithArticleCount(CategoryAggregate category, Long articleCount) {
            this.category = category;
            this.articleCount = articleCount;
        }
        
        public CategoryAggregate getCategory() {
            return category;
        }
        
        public Long getArticleCount() {
            return articleCount;
        }
    }
}