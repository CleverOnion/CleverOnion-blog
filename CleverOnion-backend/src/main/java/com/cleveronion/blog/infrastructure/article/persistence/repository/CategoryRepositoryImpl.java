package com.cleveronion.blog.infrastructure.article.persistence.repository;

import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.repository.CategoryRepository;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.infrastructure.article.persistence.converter.CategoryConverter;
import com.cleveronion.blog.infrastructure.article.persistence.po.CategoryPO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分类仓储实现类
 * 实现CategoryRepository接口，桥接领域层和基础设施层
 * 
 * @author CleverOnion
 */
@Repository
public class CategoryRepositoryImpl implements CategoryRepository {
    
    private final CategoryJpaRepository categoryJpaRepository;
    
    public CategoryRepositoryImpl(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }
    
    @Override
    public CategoryAggregate save(CategoryAggregate category) {
        CategoryPO categoryPO = CategoryConverter.toCategoryPO(category);
        CategoryPO savedCategoryPO = categoryJpaRepository.save(categoryPO);
        return CategoryConverter.toCategoryAggregate(savedCategoryPO);
    }
    
    @Override
    public Optional<CategoryAggregate> findById(CategoryId id) {
        Optional<CategoryPO> categoryPOOpt = categoryJpaRepository.findById(id.getValue());
        return categoryPOOpt.map(CategoryConverter::toCategoryAggregate);
    }
    
    @Override
    public Optional<CategoryAggregate> findByName(String name) {
        Optional<CategoryPO> categoryPOOpt = categoryJpaRepository.findByName(name);
        return categoryPOOpt.map(CategoryConverter::toCategoryAggregate);
    }
    
    @Override
    public void deleteById(CategoryId id) {
        categoryJpaRepository.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsById(CategoryId id) {
        return categoryJpaRepository.existsById(id.getValue());
    }
    
    @Override
    public boolean existsByName(String name) {
        return categoryJpaRepository.existsByName(name);
    }
    
    @Override
    public List<CategoryAggregate> findAll() {
        List<CategoryPO> categoryPOs = categoryJpaRepository.findAllByOrderByNameAsc();
        return categoryPOs.stream()
            .map(CategoryConverter::toCategoryAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryAggregate> findByNameContaining(String keyword) {
        List<CategoryPO> categoryPOs = categoryJpaRepository.findByNameContainingIgnoreCase(keyword);
        return categoryPOs.stream()
            .map(CategoryConverter::toCategoryAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryAggregate> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<CategoryPO> categoryPOs = categoryJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        return categoryPOs.stream()
            .map(CategoryConverter::toCategoryAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return categoryJpaRepository.count();
    }
    
    @Override
    public List<CategoryAggregate> findRecentlyCreated(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<CategoryPO> categoryPOs = categoryJpaRepository.findRecentlyCreated(pageable);
        return categoryPOs.stream()
            .map(CategoryConverter::toCategoryAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryAggregate> findAllOrderByName(boolean ascending) {
        List<CategoryPO> categoryPOs = ascending ? 
            categoryJpaRepository.findAllByOrderByNameAsc() : 
            categoryJpaRepository.findAllByOrderByNameDesc();
        return categoryPOs.stream()
            .map(CategoryConverter::toCategoryAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryAggregate> findAllOrderByCreatedAt(boolean ascending) {
        List<CategoryPO> categoryPOs = ascending ? 
            categoryJpaRepository.findAllByOrderByCreatedAtAsc() : 
            categoryJpaRepository.findAllByOrderByCreatedAtDesc();
        return categoryPOs.stream()
            .map(CategoryConverter::toCategoryAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryWithArticleCount> findCategoriesWithArticleCount(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Object[]> results = categoryJpaRepository.findCategoriesWithArticleCount(pageable);
        return results.stream()
            .map(result -> {
                CategoryPO categoryPO = (CategoryPO) result[0];
                Long articleCount = (Long) result[1];
                CategoryAggregate categoryAggregate = CategoryConverter.toCategoryAggregate(categoryPO);
                return new CategoryWithArticleCount(categoryAggregate, articleCount);
            })
            .collect(Collectors.toList());
    }
}