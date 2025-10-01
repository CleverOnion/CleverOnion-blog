package com.cleveronion.blog.application.tag.service;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.repository.TagRepository;
import com.cleveronion.blog.domain.article.valueobject.TagId;
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
 * TagQueryService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TagQueryService 单元测试")
class TagQueryServiceTest {
    
    @Mock
    private TagRepository tagRepository;
    
    @Mock
    private ArticleRepository articleRepository;
    
    private TagQueryService queryService;
    
    @BeforeEach
    void setUp() {
        queryService = new TagQueryService(tagRepository, articleRepository);
    }
    
    @Test
    @DisplayName("应该根据ID查找标签")
    void shouldFindById() {
        // Given
        TagId tagId = TagId.of(123L);
        TagAggregate tag = TagAggregate.rebuild(tagId, "Java");
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        
        // When
        Optional<TagAggregate> result = queryService.findById(tagId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Java", result.get().getName());
        verify(tagRepository).findById(tagId);
    }
    
    @Test
    @DisplayName("应该拒绝空的标签ID")
    void shouldRejectNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> queryService.findById(null));
    }
    
    @Test
    @DisplayName("应该批量查找标签")
    void shouldFindByIds() {
        // Given
        Set<TagId> ids = Set.of(TagId.of(1L), TagId.of(2L));
        List<TagAggregate> tags = List.of(
            TagAggregate.rebuild(TagId.of(1L), "Java"),
            TagAggregate.rebuild(TagId.of(2L), "Python")
        );
        
        when(tagRepository.findByIds(ids)).thenReturn(tags);
        
        // When
        List<TagAggregate> result = queryService.findByIds(ids);
        
        // Then
        assertEquals(2, result.size());
        verify(tagRepository).findByIds(ids);
    }
    
    @Test
    @DisplayName("应该根据名称查找标签")
    void shouldFindByName() {
        // Given
        TagAggregate tag = TagAggregate.rebuild(TagId.of(1L), "Java");
        when(tagRepository.findByName("Java")).thenReturn(Optional.of(tag));
        
        // When
        Optional<TagAggregate> result = queryService.findByName("Java");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Java", result.get().getName());
    }
    
    @Test
    @DisplayName("应该查找所有标签")
    void shouldFindAll() {
        // Given
        List<TagAggregate> tags = List.of(
            TagAggregate.rebuild(TagId.of(1L), "Java"),
            TagAggregate.rebuild(TagId.of(2L), "Python")
        );
        when(tagRepository.findAll()).thenReturn(tags);
        
        // When
        List<TagAggregate> result = queryService.findAll();
        
        // Then
        assertEquals(2, result.size());
    }
    
    @Test
    @DisplayName("应该分页查询标签")
    void shouldFindWithPagination() {
        // Given
        List<TagAggregate> tags = List.of(TagAggregate.rebuild(TagId.of(1L), "Java"));
        when(tagRepository.findAll(0, 10)).thenReturn(tags);
        
        // When
        List<TagAggregate> result = queryService.findWithPagination(0, 10);
        
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
    @DisplayName("应该搜索标签")
    void shouldSearchByName() {
        // Given
        List<TagAggregate> tags = List.of(TagAggregate.rebuild(TagId.of(1L), "JavaScript"));
        when(tagRepository.findByNameContaining("Java")).thenReturn(tags);
        
        // When
        List<TagAggregate> result = queryService.searchByName("Java");
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("应该按前缀搜索标签")
    void shouldSearchByNamePrefix() {
        // Given
        List<TagAggregate> tags = List.of(TagAggregate.rebuild(TagId.of(1L), "Java"));
        when(tagRepository.findByNameStartingWith("Ja")).thenReturn(tags);
        
        // When
        List<TagAggregate> result = queryService.searchByNamePrefix("Ja");
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("应该统计标签总数")
    void shouldCountAll() {
        // Given
        when(tagRepository.count()).thenReturn(10L);
        
        // When
        long result = queryService.countAll();
        
        // Then
        assertEquals(10L, result);
    }
    
    @Test
    @DisplayName("应该检查标签是否存在")
    void shouldCheckExistence() {
        // Given
        TagId tagId = TagId.of(123L);
        when(tagRepository.existsById(tagId)).thenReturn(true);
        
        // When
        boolean result = queryService.existsById(tagId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    @DisplayName("应该查询热门标签")
    void shouldFindPopularTags() {
        // Given
        List<TagAggregate> tags = List.of(
            TagAggregate.rebuild(TagId.of(1L), "Java"),
            TagAggregate.rebuild(TagId.of(2L), "Python")
        );
        when(tagRepository.findPopularTags(5)).thenReturn(tags);
        
        // When
        List<TagAggregate> result = queryService.findPopularTags(5);
        
        // Then
        assertEquals(2, result.size());
    }
    
    @Test
    @DisplayName("应该查询未使用的标签")
    void shouldFindUnusedTags() {
        // Given
        List<TagAggregate> tags = List.of(TagAggregate.rebuild(TagId.of(1L), "Unused"));
        when(tagRepository.findUnusedTags()).thenReturn(tags);
        
        // When
        List<TagAggregate> result = queryService.findUnusedTags();
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("应该查询标签使用统计")
    void shouldGetUsageCount() {
        // Given
        TagId tagId = TagId.of(123L);
        when(articleRepository.findByTagId(tagId)).thenReturn(List.of(
            mock(ArticleAggregate.class),
            mock(ArticleAggregate.class),
            mock(ArticleAggregate.class)
        ));
        
        // When
        long result = queryService.getTagUsageCount(tagId);
        
        // Then
        assertEquals(3L, result);
    }
}

