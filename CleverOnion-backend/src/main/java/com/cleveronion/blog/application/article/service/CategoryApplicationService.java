package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.repository.CategoryRepository;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 分类应用服务
 * 负责分类相关的业务流程编排
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class CategoryApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryApplicationService.class);
    
    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    public CategoryApplicationService(CategoryRepository categoryRepository, ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }
    
    /**
     * 创建新分类
     * 
     * @param name 分类名称
     * @return 创建的分类聚合
     */
    public CategoryAggregate createCategory(String name) {
        return createCategory(name, null);
    }
    
    /**
     * 创建新分类
     * 
     * @param name 分类名称
     * @param icon 分类图标
     * @return 创建的分类聚合
     */
    public CategoryAggregate createCategory(String name, String icon) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        String trimmedName = name.trim();
        String trimmedIcon = icon != null ? icon.trim() : null;
        if (trimmedIcon != null && trimmedIcon.isEmpty()) {
            trimmedIcon = null;
        }
        
        logger.debug("开始创建分类，名称: {}, 图标: {}", trimmedName, trimmedIcon);
        
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("分类名称已存在: " + trimmedName);
        }
        
        // 创建分类
        CategoryAggregate category = CategoryAggregate.create(trimmedName, trimmedIcon);
        
        // 保存分类
        CategoryAggregate savedCategory = categoryRepository.save(category);
        
        logger.info("成功创建分类，分类ID: {}, 名称: {}, 图标: {}", 
            savedCategory.getId().getValue(), savedCategory.getName(), savedCategory.getIcon());
        
        return savedCategory;
    }
    
    /**
     * 更新分类名称
     * 
     * @param categoryId 分类ID
     * @param newName 新的分类名称
     * @return 更新后的分类聚合
     */
    public CategoryAggregate updateCategoryName(CategoryId categoryId, String newName) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        String trimmedName = newName.trim();
        
        logger.debug("开始更新分类名称，分类ID: {}, 新名称: {}", categoryId.getValue(), trimmedName);
        
        // 查找分类
        Optional<CategoryAggregate> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("分类不存在");
        }
        
        CategoryAggregate category = categoryOpt.get();
        
        // 检查新名称是否与当前名称相同
        if (category.hasName(trimmedName)) {
            logger.debug("分类名称未发生变化，跳过更新");
            return category;
        }
        
        // 检查新名称是否已被其他分类使用
        Optional<CategoryAggregate> existingCategory = categoryRepository.findByName(trimmedName);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
            throw new IllegalArgumentException("分类名称已存在: " + trimmedName);
        }
        
        // 更新名称
        category.updateName(trimmedName);
        
        // 保存分类
        CategoryAggregate savedCategory = categoryRepository.save(category);
        
        logger.info("成功更新分类名称，分类ID: {}, 新名称: {}", 
            savedCategory.getId().getValue(), savedCategory.getName());
        
        return savedCategory;
    }
    
    /**
     * 更新分类信息
     * 
     * @param categoryId 分类ID
     * @param newName 新的分类名称
     * @param newIcon 新的分类图标
     * @return 更新后的分类聚合
     */
    public CategoryAggregate updateCategory(CategoryId categoryId, String newName, String newIcon) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        String trimmedName = newName.trim();
        String trimmedIcon = newIcon != null ? newIcon.trim() : null;
        if (trimmedIcon != null && trimmedIcon.isEmpty()) {
            trimmedIcon = null;
        }
        
        logger.debug("开始更新分类信息，分类ID: {}, 新名称: {}, 新图标: {}", 
            categoryId.getValue(), trimmedName, trimmedIcon);
        
        // 查找分类
        Optional<CategoryAggregate> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("分类不存在");
        }
        
        CategoryAggregate category = categoryOpt.get();
        
        // 检查新名称是否与当前名称相同
        boolean nameChanged = !category.hasName(trimmedName);
        boolean iconChanged = !java.util.Objects.equals(category.getIcon(), trimmedIcon);
        
        if (!nameChanged && !iconChanged) {
            logger.debug("分类信息未发生变化，跳过更新");
            return category;
        }
        
        // 如果名称发生变化，检查新名称是否已被其他分类使用
        if (nameChanged) {
            Optional<CategoryAggregate> existingCategory = categoryRepository.findByName(trimmedName);
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
                throw new IllegalArgumentException("分类名称已存在: " + trimmedName);
            }
            
            // 更新名称
            category.updateName(trimmedName);
        }
        
        // 如果图标发生变化，更新图标
        if (iconChanged) {
            category.updateIcon(trimmedIcon);
        }
        
        // 保存分类
        CategoryAggregate savedCategory = categoryRepository.save(category);
        
        logger.info("成功更新分类信息，分类ID: {}, 新名称: {}, 新图标: {}", 
            savedCategory.getId().getValue(), savedCategory.getName(), savedCategory.getIcon());
        
        return savedCategory;
    }
    
    /**
     * 删除分类
     * 
     * @param categoryId 分类ID
     */
    public void deleteCategory(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        logger.debug("开始删除分类，分类ID: {}", categoryId.getValue());
        
        // 检查分类是否存在
        Optional<CategoryAggregate> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("分类不存在");
        }
        
        CategoryAggregate category = categoryOpt.get();
        
        // 检查该分类下是否还有文章
        long articleCount = articleRepository.countByCategoryId(categoryId);
        if (articleCount > 0) {
            throw new IllegalStateException("无法删除分类，该分类下还有 " + articleCount + " 篇文章，请先处理这些文章");
        }
        
        // 删除分类
        categoryRepository.deleteById(categoryId);
        
        logger.info("成功删除分类，分类ID: {}, 名称: {}", 
            categoryId.getValue(), category.getName());
    }
    
    /**
     * 根据ID查找分类
     * 
     * @param categoryId 分类ID
     * @return 分类聚合（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<CategoryAggregate> findById(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return categoryRepository.findById(categoryId);
    }
    
    /**
     * 根据ID集合批量查找分类
     * 
     * @param categoryIds 分类ID集合
     * @return 分类聚合列表
     */
    @Transactional(readOnly = true)
    public List<CategoryAggregate> findByIds(Set<CategoryId> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("分类ID集合不能为空");
        }
        
        logger.debug("开始批量查找分类，ID数量: {}", categoryIds.size());
        
        List<CategoryAggregate> categories = categoryRepository.findByIds(categoryIds);
        
        logger.debug("批量查找分类完成，找到数量: {}", categories.size());
        
        return categories;
    }
    
    /**
     * 根据名称查找分类
     * 
     * @param name 分类名称
     * @return 分类聚合（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<CategoryAggregate> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        return categoryRepository.findByName(name.trim());
    }
    
    /**
     * 查找所有分类
     * 
     * @return 分类列表
     */
    @Transactional(readOnly = true)
    public List<CategoryAggregate> findAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * 按名称排序查找所有分类
     * 
     * @param ascending 是否升序排列
     * @return 分类列表
     */
    @Transactional(readOnly = true)
    public List<CategoryAggregate> findAllCategoriesOrderByName(boolean ascending) {
        return categoryRepository.findAllOrderByName(ascending);
    }
    
    /**
     * 按创建时间排序查找所有分类
     * 
     * @param ascending 是否升序排列
     * @return 分类列表
     */
    @Transactional(readOnly = true)
    public List<CategoryAggregate> findAllCategoriesOrderByCreatedAt(boolean ascending) {
        return categoryRepository.findAllOrderByCreatedAt(ascending);
    }
    
    /**
     * 分页查询分类
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类列表
     */
    @Transactional(readOnly = true)
    public List<CategoryAggregate> findCategories(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return categoryRepository.findAll(page, size);
    }
    
    /**
     * 根据名称关键词搜索分类
     * 
     * @param keyword 关键词
     * @return 分类列表
     */
    @Transactional(readOnly = true)
    public List<CategoryAggregate> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        return categoryRepository.findByNameContaining(keyword.trim());
    }
    
    /**
     * 统计分类总数
     * 
     * @return 分类总数
     */
    @Transactional(readOnly = true)
    public long countCategories() {
        return categoryRepository.count();
    }
    
    /**
     * 查找最近创建的分类
     * 
     * @param limit 限制数量
     * @return 分类列表
     */
    @Transactional(readOnly = true)
    public List<CategoryAggregate> findRecentlyCreated(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        return categoryRepository.findRecentlyCreated(limit);
    }
    
    /**
     * 检查分类是否存在
     * 
     * @param categoryId 分类ID
     * @return 如果存在返回true，否则返回false
     */
    @Transactional(readOnly = true)
    public boolean existsById(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return categoryRepository.existsById(categoryId);
    }
    
    /**
     * 检查指定名称的分类是否存在
     * 
     * @param name 分类名称
     * @return 如果存在返回true，否则返回false
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        return categoryRepository.existsByName(name.trim());
    }
    
    /**
     * 批量创建分类
     * 
     * @param names 分类名称列表
     * @return 创建的分类列表
     */
    public List<CategoryAggregate> createCategories(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("分类名称列表不能为空");
        }
        
        logger.debug("开始批量创建分类，数量: {}", names.size());
        
        List<CategoryAggregate> createdCategories = names.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .distinct() // 去重
            .filter(name -> !categoryRepository.existsByName(name)) // 过滤已存在的分类
            .map(name -> {
                CategoryAggregate category = CategoryAggregate.create(name);
                return categoryRepository.save(category);
            })
            .toList();
        
        logger.info("成功批量创建分类，实际创建数量: {}", createdCategories.size());
        
        return createdCategories;
    }
    
    /**
     * 获取分类使用统计信息
     * 
     * @param categoryId 分类ID
     * @return 使用该分类的文章数量
     */
    @Transactional(readOnly = true)
    public long getCategoryUsageCount(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return articleRepository.countByCategoryId(categoryId);
    }
    
    /**
     * 分页查询分类及其文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类及文章数量的列表
     */
    @Transactional(readOnly = true)
    public List<CategoryRepository.CategoryWithArticleCount> findCategoriesWithArticleCount(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        logger.debug("开始分页查询分类及文章数量，页码: {}, 每页大小: {}", page, size);
        
        List<CategoryRepository.CategoryWithArticleCount> result = categoryRepository.findCategoriesWithArticleCount(page, size);
        
        logger.debug("分页查询分类及文章数量完成，返回数量: {}", result.size());
        
        return result;
    }
}