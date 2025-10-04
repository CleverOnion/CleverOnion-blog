package com.cleveronion.blog.application.category.service;

import com.cleveronion.blog.common.cache.CacheNames;
import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.repository.CategoryRepository;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 分类查询服务（CQRS - Query）
 * 负责处理所有分类查询操作（读操作）
 * 
 * <p>使用 Redis 缓存提升查询性能：
 * <ul>
 *   <li>分类详情缓存：1 小时</li>
 *   <li>分类列表缓存：30 分钟</li>
 *   <li>分类统计缓存：10 分钟</li>
 * </ul>
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
public class CategoryQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryQueryService.class);
    
    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    
    public CategoryQueryService(
        CategoryRepository categoryRepository,
        ArticleRepository articleRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }
    
    /**
     * 根据ID查找分类（带缓存）
     * 
     * <p>缓存Key: category:detail::{categoryId}
     * <p>缓存时间: 1小时
     * 
     * @param categoryId 分类ID
     * @return 分类聚合（如果存在）
     */
    @Cacheable(
        cacheNames = CacheNames.CATEGORY_DETAIL,
        key = "#categoryId.value",
        unless = "#result == null"
    )
    public Optional<CategoryAggregate> findById(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        logger.debug("从数据库查询分类，ID: {}", categoryId.getValue());
        return categoryRepository.findById(categoryId);
    }
    
    /**
     * 根据ID集合批量查找分类（带缓存）
     * 
     * <p>缓存Key: category:list::batch:{ids}
     * <p>缓存时间: 30分钟
     * 
     * @param categoryIds 分类ID集合
     * @return 分类聚合列表
     */
    @Cacheable(
        cacheNames = CacheNames.CATEGORY_LIST,
        key = "'batch:' + #categoryIds.toString()",
        unless = "#result == null"
    )
    public List<CategoryAggregate> findByIds(Set<CategoryId> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("分类ID集合不能为空");
        }
        
        logger.debug("从数据库批量查询分类，ID数量: {}", categoryIds.size());
        return categoryRepository.findByIds(categoryIds);
    }
    
    /**
     * 根据名称查找分类
     * 
     * @param name 分类名称
     * @return 分类聚合（如果存在）
     */
    public Optional<CategoryAggregate> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        logger.debug("根据名称查询分类: {}", name);
        return categoryRepository.findByName(name.trim());
    }
    
    /**
     * 查找所有分类（带缓存）
     * 
     * <p>缓存Key: category:list::all
     * <p>缓存时间: 30分钟
     * 
     * @return 分类列表
     */
    @Cacheable(
        cacheNames = CacheNames.CATEGORY_LIST,
        key = "'all'",
        unless = "#result == null"
    )
    public List<CategoryAggregate> findAll() {
        logger.debug("从数据库查询所有分类");
        return categoryRepository.findAll();
    }
    
    /**
     * 按名称排序查找所有分类
     * 
     * @param ascending 是否升序排列
     * @return 分类列表
     */
    public List<CategoryAggregate> findAllOrderByName(boolean ascending) {
        logger.debug("按名称排序查询所有分类，升序: {}", ascending);
        return categoryRepository.findAllOrderByName(ascending);
    }
    
    /**
     * 按创建时间排序查找所有分类
     * 
     * @param ascending 是否升序排列
     * @return 分类列表
     */
    public List<CategoryAggregate> findAllOrderByCreatedAt(boolean ascending) {
        logger.debug("按创建时间排序查询所有分类，升序: {}", ascending);
        return categoryRepository.findAllOrderByCreatedAt(ascending);
    }
    
    /**
     * 分页查询分类
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类列表
     */
    public List<CategoryAggregate> findWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        logger.debug("分页查询分类，页码: {}, 每页大小: {}", page, size);
        return categoryRepository.findAll(page, size);
    }
    
    /**
     * 根据名称关键词搜索分类
     * 
     * @param keyword 关键词
     * @return 分类列表
     */
    public List<CategoryAggregate> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        logger.debug("搜索分类，关键词: {}", keyword);
        return categoryRepository.findByNameContaining(keyword.trim());
    }
    
    /**
     * 统计分类总数
     * 
     * @return 分类总数
     */
    public long countAll() {
        logger.debug("统计分类总数");
        return categoryRepository.count();
    }
    
    /**
     * 查找最近创建的分类
     * 
     * @param limit 限制数量
     * @return 分类列表
     */
    public List<CategoryAggregate> findRecentlyCreated(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        logger.debug("查询最近创建的分类，限制数量: {}", limit);
        return categoryRepository.findRecentlyCreated(limit);
    }
    
    /**
     * 检查分类是否存在
     * 
     * @param categoryId 分类ID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsById(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        logger.debug("检查分类是否存在，ID: {}", categoryId.getValue());
        return categoryRepository.existsById(categoryId);
    }
    
    /**
     * 检查指定名称的分类是否存在
     * 
     * @param name 分类名称
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        logger.debug("检查分类名称是否存在: {}", name);
        return categoryRepository.existsByName(name.trim());
    }
    
    /**
     * 获取分类使用统计信息（带缓存）
     * 
     * <p>缓存Key: category:count::{categoryId}
     * <p>缓存时间: 10分钟
     * 
     * @param categoryId 分类ID
     * @return 使用该分类的文章数量
     */
    @Cacheable(
        cacheNames = CacheNames.CATEGORY_COUNT,
        key = "#categoryId.value"
    )
    public long getCategoryUsageCount(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        logger.debug("从数据库统计分类使用情况，ID: {}", categoryId.getValue());
        return articleRepository.countByCategoryId(categoryId);
    }
    
    /**
     * 分页查询分类及其文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类及文章数量的列表
     */
    public List<CategoryRepository.CategoryWithArticleCount> findWithArticleCount(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        logger.debug("分页查询分类及文章数量，页码: {}, 每页大小: {}", page, size);
        return categoryRepository.findCategoriesWithArticleCount(page, size);
    }
}


