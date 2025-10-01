package com.cleveronion.blog.application.category.service;

import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.repository.CategoryRepository;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CategoryQueryService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryQueryService 单元测试")
class CategoryQueryServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ArticleRepository articleRepository;
    
    private CategoryQueryService queryService;
    
    @BeforeEach
    void setUp() {
        queryService = new CategoryQueryService(categoryRepository, articleRepository);
    }
    
    @Test
    @DisplayName("应该根据ID查找分类")
    void shouldFindById() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        CategoryAggregate category = CategoryAggregate.create("技术", null);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        
        // When
        Optional<CategoryAggregate> result = queryService.findById(categoryId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("技术", result.get().getName());
        verify(categoryRepository).findById(categoryId);
    }
    
    @Test
    @DisplayName("应该拒绝空的分类ID")
    void shouldRejectNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> queryService.findById(null));
    }
    
    @Test
    @DisplayName("应该批量查找分类")
    void shouldFindByIds() {
        // Given
        Set<CategoryId> ids = Set.of(CategoryId.of(1L), CategoryId.of(2L));
        List<CategoryAggregate> categories = List.of(
            CategoryAggregate.create("技术", null),
            CategoryAggregate.create("生活", null)
        );
        
        when(categoryRepository.findByIds(ids)).thenReturn(categories);
        
        // When
        List<CategoryAggregate> result = queryService.findByIds(ids);
        
        // Then
        assertEquals(2, result.size());
        verify(categoryRepository).findByIds(ids);
    }
    
    @Test
    @DisplayName("应该根据名称查找分类")
    void shouldFindByName() {
        // Given
        CategoryAggregate category = CategoryAggregate.create("技术", null);
        when(categoryRepository.findByName("技术")).thenReturn(Optional.of(category));
        
        // When
        Optional<CategoryAggregate> result = queryService.findByName("技术");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("技术", result.get().getName());
    }
    
    @Test
    @DisplayName("应该查找所有分类")
    void shouldFindAll() {
        // Given
        List<CategoryAggregate> categories = List.of(
            CategoryAggregate.create("技术", null),
            CategoryAggregate.create("生活", null)
        );
        when(categoryRepository.findAll()).thenReturn(categories);
        
        // When
        List<CategoryAggregate> result = queryService.findAll();
        
        // Then
        assertEquals(2, result.size());
    }
    
    @Test
    @DisplayName("应该按名称排序查找")
    void shouldFindAllOrderByName() {
        // Given
        List<CategoryAggregate> categories = List.of(
            CategoryAggregate.create("A分类", null),
            CategoryAggregate.create("B分类", null)
        );
        when(categoryRepository.findAllOrderByName(true)).thenReturn(categories);
        
        // When
        List<CategoryAggregate> result = queryService.findAllOrderByName(true);
        
        // Then
        assertEquals(2, result.size());
        verify(categoryRepository).findAllOrderByName(true);
    }
    
    @Test
    @DisplayName("应该分页查询分类")
    void shouldFindWithPagination() {
        // Given
        List<CategoryAggregate> categories = List.of(CategoryAggregate.create("技术", null));
        when(categoryRepository.findAll(0, 10)).thenReturn(categories);
        
        // When
        List<CategoryAggregate> result = queryService.findWithPagination(0, 10);
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("分页查询应该拒绝无效参数")
    void shouldRejectInvalidPaginationParams() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findWithPagination(-1, 10));
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findWithPagination(0, 0));
    }
    
    @Test
    @DisplayName("应该搜索分类")
    void shouldSearchByName() {
        // Given
        List<CategoryAggregate> categories = List.of(CategoryAggregate.create("技术分类", null));
        when(categoryRepository.findByNameContaining("技术")).thenReturn(categories);
        
        // When
        List<CategoryAggregate> result = queryService.searchByName("技术");
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("应该统计分类总数")
    void shouldCountAll() {
        // Given
        when(categoryRepository.count()).thenReturn(10L);
        
        // When
        long result = queryService.countAll();
        
        // Then
        assertEquals(10L, result);
    }
    
    @Test
    @DisplayName("应该检查分类是否存在")
    void shouldCheckExistence() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        
        // When
        boolean result = queryService.existsById(categoryId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    @DisplayName("应该查询分类使用统计")
    void shouldGetUsageCount() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        when(articleRepository.countByCategoryId(categoryId)).thenReturn(5L);
        
        // When
        long result = queryService.getCategoryUsageCount(categoryId);
        
        // Then
        assertEquals(5L, result);
    }
}

