package com.cleveronion.blog.application.category.service;

import com.cleveronion.blog.application.article.service.CategoryApplicationService;
import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.repository.CategoryRepository;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CategoryApplicationService批量查询功能单元测试
 * 验证批量查询方法的正确性和性能优化效果
 * 
 * @author CleverOnion
 */
@ExtendWith(MockitoExtension.class)
class CategoryApplicationServiceBatchTest {
    
    static {
        // 设置lenient模式避免UnnecessaryStubbingException
        System.setProperty("mockito.strictness", "lenient");
    }

    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private CategoryApplicationService categoryApplicationService;
    
    @BeforeEach
    void setUp() {
        // 测试初始化
    }
    
    private CategoryAggregate createMockCategory(String id, String name) {
        CategoryAggregate category = mock(CategoryAggregate.class);
        lenient().when(category.getId()).thenReturn(CategoryId.of(Long.parseLong(id)));
        lenient().when(category.getName()).thenReturn(name);
        return category;
    }
    
    @Test
    void testFindByIds_WithValidIds_ShouldReturnCategories() {
        // Given
        Set<CategoryId> categoryIds = Set.of(
            CategoryId.of(1L),
            CategoryId.of(2L),
            CategoryId.of(3L)
        );
        
        List<CategoryAggregate> mockCategories = Arrays.asList(
            createMockCategory("1", "技术"),
            createMockCategory("2", "生活"),
            createMockCategory("3", "随笔")
        );
        
        when(categoryRepository.findByIds(categoryIds)).thenReturn(mockCategories);
        
        // When
        List<CategoryAggregate> result = categoryApplicationService.findByIds(categoryIds);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // 验证仓储方法被调用
        verify(categoryRepository, times(1)).findByIds(categoryIds);
    }
    
    @Test
    void testFindByIds_WithEmptyIds_ShouldThrowException() {
        // Given
        Set<CategoryId> emptyIds = Collections.emptySet();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            categoryApplicationService.findByIds(emptyIds);
        });
        
        // 验证仓储方法没有被调用
        verify(categoryRepository, never()).findByIds(any());
    }
    
    @Test
    void testFindByIds_WithNullIds_ShouldThrowException() {
        // Given
        Set<CategoryId> nullIds = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            categoryApplicationService.findByIds(nullIds);
        });
        
        // 验证仓储方法没有被调用
        verify(categoryRepository, never()).findByIds(any());
    }
    
    @Test
    void testFindByIds_WithPartialResults_ShouldReturnAvailableCategories() {
        // Given - 请求3个分类，但只返回2个
        Set<CategoryId> categoryIds = Set.of(
            CategoryId.of(1L),
            CategoryId.of(2L),
            CategoryId.of(999L) // 不存在的分类
        );
        
        List<CategoryAggregate> partialCategories = Arrays.asList(
            createMockCategory("1", "技术"),
            createMockCategory("2", "生活")
        );
        
        when(categoryRepository.findByIds(categoryIds)).thenReturn(partialCategories);
        
        // When
        List<CategoryAggregate> result = categoryApplicationService.findByIds(categoryIds);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 验证仓储方法被调用
        verify(categoryRepository, times(1)).findByIds(categoryIds);
    }
    
    @Test
    void testFindByIds_WithDuplicateIds_ShouldHandleCorrectly() {
        // Given - 测试Set自动去重功能
        Set<CategoryId> categoryIds = new HashSet<>();
        categoryIds.add(CategoryId.of(1L));
        categoryIds.add(CategoryId.of(2L));
        categoryIds.add(CategoryId.of(1L)); // 重复添加，Set会自动去重
        
        List<CategoryAggregate> expectedCategories = Arrays.asList(
            createMockCategory("1", "技术"),
            createMockCategory("2", "生活")
        );
        
        when(categoryRepository.findByIds(categoryIds)).thenReturn(expectedCategories);
        
        // When
        List<CategoryAggregate> result = categoryApplicationService.findByIds(categoryIds);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 应该只有2个，因为重复的被去重了
        assertEquals(2, categoryIds.size()); // 验证Set确实去重了
        
        // 验证仓储方法被调用
        verify(categoryRepository, times(1)).findByIds(categoryIds);
    }
    
    @Test
    void testFindByIds_ComparedToSingleQueries_ShouldBeMoreEfficient() {
        // Given
        Set<CategoryId> categoryIds = Set.of(
            CategoryId.of(1L),
            CategoryId.of(2L),
            CategoryId.of(3L)
        );
        
        List<CategoryAggregate> mockCategories = Arrays.asList(
            createMockCategory("1", "技术"),
            createMockCategory("2", "生活"),
            createMockCategory("3", "随笔")
        );
        
        when(categoryRepository.findByIds(categoryIds)).thenReturn(mockCategories);
        
        // 模拟单个查询的返回
        when(categoryRepository.findById(CategoryId.of(1L))).thenReturn(Optional.of(mockCategories.get(0)));
        when(categoryRepository.findById(CategoryId.of(2L))).thenReturn(Optional.of(mockCategories.get(1)));
        when(categoryRepository.findById(CategoryId.of(3L))).thenReturn(Optional.of(mockCategories.get(2)));
        
        // When - 使用批量查询
        List<CategoryAggregate> batchResult = categoryApplicationService.findByIds(categoryIds);
        
        // 模拟使用单个查询的方式
        List<CategoryAggregate> singleResults = new ArrayList<>();
        for (CategoryId categoryId : categoryIds) {
            Optional<CategoryAggregate> category = categoryApplicationService.findById(categoryId);
            category.ifPresent(singleResults::add);
        }
        
        // Then
        assertEquals(batchResult.size(), singleResults.size());
        
        // 验证批量查询只调用一次仓储方法
        verify(categoryRepository, times(1)).findByIds(categoryIds);
        
        // 验证单个查询调用了多次仓储方法
        verify(categoryRepository, times(3)).findById(any(CategoryId.class));
        
        // 这证明了批量查询的性能优化：1次数据库调用 vs 3次数据库调用
    }
    
    @Test
    void testFindByIds_WithLargeDataSet_ShouldHandleEfficiently() {
        // Given - 大量分类ID
        Set<CategoryId> largeCategoryIds = new HashSet<>();
        List<CategoryAggregate> largeCategoryList = new ArrayList<>();
        
        for (int i = 1; i <= 100; i++) {
            CategoryId categoryId = CategoryId.of((long) i);
            largeCategoryIds.add(categoryId);
            largeCategoryList.add(createMockCategory(String.valueOf(i), "分类" + i));
        }
        
        when(categoryRepository.findByIds(largeCategoryIds)).thenReturn(largeCategoryList);
        
        // When
        List<CategoryAggregate> result = categoryApplicationService.findByIds(largeCategoryIds);
        
        // Then
        assertNotNull(result);
        assertEquals(100, result.size());
        
        // 验证仓储方法只被调用一次（批量查询的关键优势）
        verify(categoryRepository, times(1)).findByIds(largeCategoryIds);
    }
}