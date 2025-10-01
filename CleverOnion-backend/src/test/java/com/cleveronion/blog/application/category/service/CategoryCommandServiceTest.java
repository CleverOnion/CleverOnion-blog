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
import com.cleveronion.blog.domain.common.event.DomainEvent;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CategoryCommandService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryCommandService 单元测试")
class CategoryCommandServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ArticleRepository articleRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    private CategoryCommandService commandService;
    
    @BeforeEach
    void setUp() {
        commandService = new CategoryCommandService(
            categoryRepository,
            articleRepository,
            eventPublisher
        );
    }
    
    @Test
    @DisplayName("应该成功创建分类")
    void shouldCreateCategory() {
        // Given
        CreateCategoryCommand command = CreateCategoryCommand.of("技术", "tech-icon");
        CategoryAggregate savedCategory = CategoryAggregate.rebuild(CategoryId.of(1L), "技术", "tech-icon");
        
        when(categoryRepository.existsByName("技术")).thenReturn(false);
        when(categoryRepository.save(any(CategoryAggregate.class))).thenReturn(savedCategory);
        
        // When
        CategoryAggregate result = commandService.createCategory(command);
        
        // Then
        assertNotNull(result);
        assertEquals("技术", result.getName());
        verify(categoryRepository).existsByName("技术");
        verify(categoryRepository).save(any(CategoryAggregate.class));
        verify(eventPublisher).publish(any(CategoryCreatedEvent.class));
    }
    
    @Test
    @DisplayName("应该拒绝创建重复名称的分类")
    void shouldRejectDuplicateCategoryName() {
        // Given
        CreateCategoryCommand command = CreateCategoryCommand.of("技术", null);
        when(categoryRepository.existsByName("技术")).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.createCategory(command)
        );
        
        assertEquals("分类名称已存在: 技术", exception.getMessage());
        verify(categoryRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    @DisplayName("应该成功更新分类")
    void shouldUpdateCategory() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, "新技术", "new-icon");
        CategoryAggregate category = CategoryAggregate.rebuild(categoryId, "技术", "old-icon");
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("新技术")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryAggregate.class))).thenReturn(category);
        
        // When
        CategoryAggregate result = commandService.updateCategory(command);
        
        // Then
        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(any(CategoryAggregate.class));
        verify(eventPublisher).publish(any(CategoryUpdatedEvent.class));
    }
    
    @Test
    @DisplayName("应该拒绝更新为已存在的名称")
    void shouldRejectUpdateToExistingName() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        CategoryId otherCategoryId = CategoryId.of(456L);
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, "已存在", null);
        
        CategoryAggregate category = CategoryAggregate.rebuild(categoryId, "技术", null);
        CategoryAggregate otherCategory = CategoryAggregate.rebuild(otherCategoryId, "已存在", null);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("已存在")).thenReturn(Optional.of(otherCategory));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.updateCategory(command)
        );
        
        assertEquals("分类名称已存在: 已存在", exception.getMessage());
        verify(categoryRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    @DisplayName("应该拒绝更新不存在的分类")
    void shouldRejectUpdateNonExistentCategory() {
        // Given
        CategoryId categoryId = CategoryId.of(999L);
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, "新名称", null);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.updateCategory(command)
        );
        
        assertEquals("分类不存在", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("应该成功删除分类")
    void shouldDeleteCategory() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        DeleteCategoryCommand command = DeleteCategoryCommand.of(categoryId);
        CategoryAggregate category = CategoryAggregate.rebuild(categoryId, "技术", null);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(articleRepository.countByCategoryId(categoryId)).thenReturn(0L);
        
        // When
        commandService.deleteCategory(command);
        
        // Then
        verify(categoryRepository).findById(categoryId);
        verify(articleRepository).countByCategoryId(categoryId);
        verify(categoryRepository).deleteById(categoryId);
        verify(eventPublisher).publish(any(CategoryDeletedEvent.class));
    }
    
    @Test
    @DisplayName("应该拒绝删除有文章的分类")
    void shouldRejectDeleteCategoryWithArticles() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        DeleteCategoryCommand command = DeleteCategoryCommand.of(categoryId);
        CategoryAggregate category = CategoryAggregate.rebuild(categoryId, "技术", null);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(articleRepository.countByCategoryId(categoryId)).thenReturn(5L);
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> commandService.deleteCategory(command)
        );
        
        assertTrue(exception.getMessage().contains("无法删除分类"));
        assertTrue(exception.getMessage().contains("5 篇文章"));
        verify(categoryRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    @DisplayName("应该拒绝删除不存在的分类")
    void shouldRejectDeleteNonExistentCategory() {
        // Given
        CategoryId categoryId = CategoryId.of(999L);
        DeleteCategoryCommand command = DeleteCategoryCommand.of(categoryId);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.deleteCategory(command)
        );
        
        assertEquals("分类不存在", exception.getMessage());
        verify(categoryRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("应该成功批量创建分类")
    void shouldCreateCategories() {
        // Given
        List<String> names = List.of("技术", "生活", "读书");
        
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(CategoryAggregate.class)))
            .thenAnswer(invocation -> {
                CategoryAggregate cat = invocation.getArgument(0);
                long id = cat.getName().hashCode();  // 使用名称hash作为ID
                return CategoryAggregate.rebuild(CategoryId.of(id), cat.getName(), cat.getIcon());
            });
        
        // When
        List<CategoryAggregate> result = commandService.createCategories(names);
        
        // Then
        assertEquals(3, result.size());
        verify(categoryRepository, times(3)).save(any(CategoryAggregate.class));
        verify(eventPublisher, times(3)).publish(any(CategoryCreatedEvent.class));
    }
    
    @Test
    @DisplayName("批量创建应该过滤已存在的分类")
    void shouldFilterExistingCategoriesInBatchCreate() {
        // Given
        List<String> names = List.of("技术", "生活", "读书");
        
        when(categoryRepository.existsByName("技术")).thenReturn(true);
        when(categoryRepository.existsByName("生活")).thenReturn(false);
        when(categoryRepository.existsByName("读书")).thenReturn(false);
        when(categoryRepository.save(any(CategoryAggregate.class)))
            .thenAnswer(invocation -> {
                CategoryAggregate cat = invocation.getArgument(0);
                long id = Math.abs(cat.getName().hashCode());
                return CategoryAggregate.rebuild(CategoryId.of(id), cat.getName(), cat.getIcon());
            });
        
        // When
        List<CategoryAggregate> result = commandService.createCategories(names);
        
        // Then
        assertEquals(2, result.size()); // 只创建了2个（过滤掉已存在的"技术"）
        verify(categoryRepository, times(2)).save(any(CategoryAggregate.class));
        verify(eventPublisher, times(2)).publish(any(CategoryCreatedEvent.class));
    }
    
    @Test
    @DisplayName("创建分类应该发布正确的事件")
    void shouldPublishCorrectEventOnCreate() {
        // Given
        CreateCategoryCommand command = CreateCategoryCommand.of("技术", "tech");
        CategoryAggregate savedCategory = CategoryAggregate.rebuild(CategoryId.of(1L), "技术", "tech");
        
        when(categoryRepository.existsByName("技术")).thenReturn(false);
        when(categoryRepository.save(any(CategoryAggregate.class))).thenReturn(savedCategory);
        
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        
        // When
        commandService.createCategory(command);
        
        // Then
        verify(eventPublisher).publish(eventCaptor.capture());
        DomainEvent publishedEvent = eventCaptor.getValue();
        
        assertTrue(publishedEvent instanceof CategoryCreatedEvent);
        CategoryCreatedEvent createdEvent = (CategoryCreatedEvent) publishedEvent;
        assertEquals("技术", createdEvent.getCategoryName());
    }
    
    @Test
    @DisplayName("更新分类应该发布正确的事件")
    void shouldPublishCorrectEventOnUpdate() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, "新技术", null);
        CategoryAggregate category = CategoryAggregate.rebuild(categoryId, "旧技术", null);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("新技术")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryAggregate.class))).thenReturn(category);
        
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        
        // When
        commandService.updateCategory(command);
        
        // Then
        verify(eventPublisher).publish(eventCaptor.capture());
        DomainEvent publishedEvent = eventCaptor.getValue();
        
        assertTrue(publishedEvent instanceof CategoryUpdatedEvent);
        CategoryUpdatedEvent updatedEvent = (CategoryUpdatedEvent) publishedEvent;
        assertEquals("旧技术", updatedEvent.getOldName());
        assertEquals("新技术", updatedEvent.getNewName());
    }
    
    @Test
    @DisplayName("删除分类应该发布正确的事件")
    void shouldPublishCorrectEventOnDelete() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        DeleteCategoryCommand command = DeleteCategoryCommand.of(categoryId);
        CategoryAggregate category = CategoryAggregate.rebuild(categoryId, "技术", null);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(articleRepository.countByCategoryId(categoryId)).thenReturn(0L);
        
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        
        // When
        commandService.deleteCategory(command);
        
        // Then
        verify(eventPublisher).publish(eventCaptor.capture());
        DomainEvent publishedEvent = eventCaptor.getValue();
        
        assertTrue(publishedEvent instanceof CategoryDeletedEvent);
        CategoryDeletedEvent deletedEvent = (CategoryDeletedEvent) publishedEvent;
        assertEquals("技术", deletedEvent.getCategoryName());
        assertEquals(categoryId, deletedEvent.getCategoryId());
    }
    
    @Test
    @DisplayName("更新分类时如果信息未变化应该跳过更新")
    void shouldSkipUpdateIfNoChange() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, "技术", "icon");
        CategoryAggregate category = CategoryAggregate.rebuild(categoryId, "技术", "icon");
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        
        // When
        CategoryAggregate result = commandService.updateCategory(command);
        
        // Then
        assertNotNull(result);
        verify(categoryRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    @DisplayName("批量创建应该去重")
    void shouldDeduplicateInBatchCreate() {
        // Given
        List<String> names = List.of("技术", "技术", "生活");
        
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(CategoryAggregate.class)))
            .thenAnswer(invocation -> {
                CategoryAggregate cat = invocation.getArgument(0);
                long id = Math.abs(cat.getName().hashCode());
                return CategoryAggregate.rebuild(CategoryId.of(id), cat.getName(), cat.getIcon());
            });
        
        // When
        List<CategoryAggregate> result = commandService.createCategories(names);
        
        // Then
        assertEquals(2, result.size()); // 去重后只有2个
        verify(categoryRepository, times(2)).save(any(CategoryAggregate.class));
    }
    
    @Test
    @DisplayName("批量创建应该拒绝空列表")
    void shouldRejectEmptyListInBatchCreate() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.createCategories(List.of())
        );
        
        assertEquals("分类名称列表不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("批量创建应该过滤空字符串")
    void shouldFilterEmptyStringsInBatchCreate() {
        // Given
        List<String> names = List.of("技术", "", "  ", "生活");
        
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(CategoryAggregate.class)))
            .thenAnswer(invocation -> {
                CategoryAggregate cat = invocation.getArgument(0);
                long id = Math.abs(cat.getName().hashCode());
                return CategoryAggregate.rebuild(CategoryId.of(id), cat.getName(), cat.getIcon());
            });
        
        // When
        List<CategoryAggregate> result = commandService.createCategories(names);
        
        // Then
        assertEquals(2, result.size()); // 只创建了"技术"和"生活"
        verify(categoryRepository, times(2)).save(any(CategoryAggregate.class));
    }
}

