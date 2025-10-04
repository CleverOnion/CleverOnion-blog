package com.cleveronion.blog.application.category.service;

import com.cleveronion.blog.application.category.command.CreateCategoryCommand;
import com.cleveronion.blog.application.category.command.DeleteCategoryCommand;
import com.cleveronion.blog.application.category.command.UpdateCategoryCommand;
import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.event.CategoryCreatedEvent;
import com.cleveronion.blog.domain.article.event.CategoryDeletedEvent;
import com.cleveronion.blog.domain.article.event.CategoryUpdatedEvent;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.repository.CategoryRepository;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 分类命令服务
 * 负责处理所有修改分类状态的操作
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class CategoryCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryCommandService.class);
    
    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final DomainEventPublisher eventPublisher;
    
    public CategoryCommandService(
        CategoryRepository categoryRepository,
        ArticleRepository articleRepository,
        DomainEventPublisher eventPublisher
    ) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 创建分类
     * 
     * @param command 创建分类命令
     * @return 创建的分类聚合
     */
    public CategoryAggregate createCategory(CreateCategoryCommand command) {
        logger.debug("执行创建分类命令: {}", command);
        
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(command.getName())) {
            throw new IllegalArgumentException("分类名称已存在: " + command.getName());
        }
        
        // 创建分类聚合
        CategoryAggregate category = CategoryAggregate.create(
            command.getName(),
            command.getIcon()
        );
        
        // 保存分类
        CategoryAggregate saved = categoryRepository.save(category);
        
        // 发布领域事件
        eventPublisher.publish(new CategoryCreatedEvent(
            this,
            saved.getId(),
            saved.getName()
        ));
        
        logger.info("成功创建分类，分类ID: {}, 名称: {}", 
            saved.getId().getValue(), saved.getName());
        
        return saved;
    }
    
    /**
     * 更新分类
     * 
     * @param command 更新分类命令
     * @return 更新后的分类聚合
     */
    public CategoryAggregate updateCategory(UpdateCategoryCommand command) {
        logger.debug("执行更新分类命令: {}", command);
        
        CategoryId categoryId = command.getCategoryId();
        
        // 查找分类
        CategoryAggregate category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
        
        String oldName = category.getName();
        
        // 检查新名称是否与当前名称相同
        if (category.hasName(command.getName())) {
            // 只检查图标是否需要更新
            if (!java.util.Objects.equals(category.getIcon(), command.getIcon())) {
                category.updateIcon(command.getIcon());
                CategoryAggregate saved = categoryRepository.save(category);
                logger.info("成功更新分类图标，分类ID: {}", categoryId.getValue());
                return saved;
            }
            logger.debug("分类信息未发生变化，跳过更新");
            return category;
        }
        
        // 检查新名称是否已被其他分类使用
        Optional<CategoryAggregate> existingCategory = categoryRepository.findByName(command.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
            throw new IllegalArgumentException("分类名称已存在: " + command.getName());
        }
        
        // 更新名称
        category.updateName(command.getName());
        
        // 更新图标（如果需要）
        if (!java.util.Objects.equals(category.getIcon(), command.getIcon())) {
            category.updateIcon(command.getIcon());
        }
        
        // 保存分类
        CategoryAggregate saved = categoryRepository.save(category);
        
        // 发布领域事件
        eventPublisher.publish(new CategoryUpdatedEvent(
            this,
            saved.getId(),
            oldName,
            saved.getName()
        ));
        
        logger.info("成功更新分类，分类ID: {}, 新名称: {}", 
            saved.getId().getValue(), saved.getName());
        
        return saved;
    }
    
    /**
     * 删除分类
     * 
     * @param command 删除分类命令
     */
    public void deleteCategory(DeleteCategoryCommand command) {
        logger.debug("执行删除分类命令: {}", command);
        
        CategoryId categoryId = command.getCategoryId();
        
        // 检查分类是否存在
        CategoryAggregate category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
        
        // 检查该分类下是否还有文章
        long articleCount = articleRepository.countByCategoryId(categoryId);
        if (articleCount > 0) {
            throw new IllegalStateException(
                "无法删除分类，该分类下还有 " + articleCount + " 篇文章，请先处理这些文章"
            );
        }
        
        // 删除分类
        categoryRepository.deleteById(categoryId);
        
        // 发布领域事件
        eventPublisher.publish(new CategoryDeletedEvent(
            this,
            categoryId,
            category.getName()
        ));
        
        logger.info("成功删除分类，分类ID: {}, 名称: {}", 
            categoryId.getValue(), category.getName());
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
                // 创建分类聚合
                CategoryAggregate category = CategoryAggregate.create(name);
                CategoryAggregate saved = categoryRepository.save(category);
                
                // 发布事件
                eventPublisher.publish(new CategoryCreatedEvent(
                    this,
                    saved.getId(),
                    saved.getName()
                ));
                
                return saved;
            })
            .toList();
        
        logger.info("成功批量创建分类，实际创建数量: {}", createdCategories.size());
        
        return createdCategories;
    }
}

