package com.cleveronion.blog.application.tag.service;

import com.cleveronion.blog.application.tag.command.*;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.event.TagCreatedEvent;
import com.cleveronion.blog.domain.article.event.TagDeletedEvent;
import com.cleveronion.blog.domain.article.event.TagUpdatedEvent;
import com.cleveronion.blog.domain.article.repository.TagRepository;
import com.cleveronion.blog.domain.article.valueobject.TagId;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TagCommandService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TagCommandService 单元测试")
class TagCommandServiceTest {
    
    @Mock
    private TagRepository tagRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    private TagCommandService commandService;
    
    @BeforeEach
    void setUp() {
        commandService = new TagCommandService(tagRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("应该成功创建标签")
    void shouldCreateTag() {
        // Given
        CreateTagCommand command = CreateTagCommand.of("Java");
        TagAggregate savedTag = TagAggregate.rebuild(TagId.of(1L), "Java");
        
        when(tagRepository.existsByName("Java")).thenReturn(false);
        when(tagRepository.save(any(TagAggregate.class))).thenReturn(savedTag);
        
        // When
        TagAggregate result = commandService.createTag(command);
        
        // Then
        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(tagRepository).existsByName("Java");
        verify(tagRepository).save(any(TagAggregate.class));
        verify(eventPublisher).publish(any(TagCreatedEvent.class));
    }
    
    @Test
    @DisplayName("应该拒绝创建重复名称的标签")
    void shouldRejectDuplicateTagName() {
        // Given
        CreateTagCommand command = CreateTagCommand.of("Java");
        when(tagRepository.existsByName("Java")).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.createTag(command)
        );
        
        assertEquals("标签名称已存在: Java", exception.getMessage());
        verify(tagRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    @DisplayName("应该成功更新标签")
    void shouldUpdateTag() {
        // Given
        TagId tagId = TagId.of(123L);
        UpdateTagCommand command = UpdateTagCommand.of(tagId, "NewJava");
        TagAggregate tag = TagAggregate.rebuild(tagId, "Java");
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.findByName("NewJava")).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagAggregate.class))).thenReturn(tag);
        
        // When
        TagAggregate result = commandService.updateTagName(command);
        
        // Then
        assertNotNull(result);
        verify(tagRepository).findById(tagId);
        verify(tagRepository).save(any(TagAggregate.class));
        verify(eventPublisher).publish(any(TagUpdatedEvent.class));
    }
    
    @Test
    @DisplayName("应该成功删除标签")
    void shouldDeleteTag() {
        // Given
        TagId tagId = TagId.of(123L);
        DeleteTagCommand command = DeleteTagCommand.of(tagId);
        TagAggregate tag = TagAggregate.rebuild(tagId, "Java");
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        
        // When
        commandService.deleteTag(command);
        
        // Then
        verify(tagRepository).findById(tagId);
        verify(tagRepository).deleteById(tagId);
        verify(eventPublisher).publish(any(TagDeletedEvent.class));
    }
    
    @Test
    @DisplayName("应该成功批量创建标签")
    void shouldCreateTags() {
        // Given
        CreateTagsCommand command = CreateTagsCommand.of(List.of("Java", "Python", "Go"));
        
        when(tagRepository.existsByName(anyString())).thenReturn(false);
        when(tagRepository.save(any(TagAggregate.class)))
            .thenAnswer(invocation -> {
                TagAggregate tag = invocation.getArgument(0);
                long id = Math.abs(tag.getName().hashCode());
                return TagAggregate.rebuild(TagId.of(id), tag.getName());
            });
        
        // When
        List<TagAggregate> result = commandService.createTags(command);
        
        // Then
        assertEquals(3, result.size());
        verify(tagRepository, times(3)).save(any(TagAggregate.class));
        verify(eventPublisher, times(3)).publish(any(TagCreatedEvent.class));
    }
    
    @Test
    @DisplayName("批量创建应该过滤已存在的标签")
    void shouldFilterExistingTagsInBatchCreate() {
        // Given
        CreateTagsCommand command = CreateTagsCommand.of(List.of("Java", "Python", "Go"));
        
        when(tagRepository.existsByName("Java")).thenReturn(true);
        when(tagRepository.existsByName("Python")).thenReturn(false);
        when(tagRepository.existsByName("Go")).thenReturn(false);
        when(tagRepository.save(any(TagAggregate.class)))
            .thenAnswer(invocation -> {
                TagAggregate tag = invocation.getArgument(0);
                long id = Math.abs(tag.getName().hashCode());
                return TagAggregate.rebuild(TagId.of(id), tag.getName());
            });
        
        // When
        List<TagAggregate> result = commandService.createTags(command);
        
        // Then
        assertEquals(2, result.size()); // 只创建了2个
        verify(tagRepository, times(2)).save(any(TagAggregate.class));
    }
    
    @Test
    @DisplayName("应该成功批量删除标签")
    void shouldDeleteTags() {
        // Given
        Set<TagId> tagIds = Set.of(TagId.of(1L), TagId.of(2L));
        DeleteTagsCommand command = DeleteTagsCommand.of(tagIds);
        List<TagAggregate> tags = List.of(
            TagAggregate.rebuild(TagId.of(1L), "Java"),
            TagAggregate.rebuild(TagId.of(2L), "Python")
        );
        
        when(tagRepository.findByIds(tagIds)).thenReturn(tags);
        
        // When
        commandService.deleteTags(command);
        
        // Then
        verify(tagRepository).findByIds(tagIds);
        verify(tagRepository).deleteByIds(tagIds);
        verify(eventPublisher, times(2)).publish(any(TagDeletedEvent.class));
    }
    
    @Test
    @DisplayName("应该成功清理未使用的标签")
    void shouldCleanupUnusedTags() {
        // Given
        CleanupUnusedTagsCommand command = CleanupUnusedTagsCommand.create();
        List<TagAggregate> unusedTags = List.of(
            TagAggregate.rebuild(TagId.of(1L), "Unused1"),
            TagAggregate.rebuild(TagId.of(2L), "Unused2")
        );
        
        when(tagRepository.findUnusedTags()).thenReturn(unusedTags);
        
        // When
        int result = commandService.cleanupUnusedTags(command);
        
        // Then
        assertEquals(2, result);
        verify(tagRepository).deleteByIds(any());
        verify(eventPublisher, times(2)).publish(any(TagDeletedEvent.class));
    }
    
    @Test
    @DisplayName("清理时没有未使用标签应该返回0")
    void shouldReturnZeroWhenNoUnusedTags() {
        // Given
        CleanupUnusedTagsCommand command = CleanupUnusedTagsCommand.create();
        when(tagRepository.findUnusedTags()).thenReturn(List.of());
        
        // When
        int result = commandService.cleanupUnusedTags(command);
        
        // Then
        assertEquals(0, result);
        verify(tagRepository, never()).deleteByIds(any());
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    @DisplayName("应该成功查找或创建标签")
    void shouldFindOrCreateTags() {
        // Given
        Set<String> names = Set.of("Java", "Python", "NewTag");
        FindOrCreateTagsCommand command = FindOrCreateTagsCommand.of(names);
        
        List<TagAggregate> existingTags = List.of(
            TagAggregate.rebuild(TagId.of(1L), "Java"),
            TagAggregate.rebuild(TagId.of(2L), "Python")
        );
        
        when(tagRepository.findByNames(names)).thenReturn(existingTags);
        when(tagRepository.save(any(TagAggregate.class)))
            .thenAnswer(invocation -> {
                TagAggregate tag = invocation.getArgument(0);
                return TagAggregate.rebuild(TagId.of(3L), tag.getName());
            });
        
        // When
        List<TagAggregate> result = commandService.findOrCreateByNames(command);
        
        // Then
        assertEquals(3, result.size());
        verify(tagRepository).save(any(TagAggregate.class)); // 只创建了1个新标签
        verify(eventPublisher).publish(any(TagCreatedEvent.class)); // 只发布1个创建事件
    }
    
    @Test
    @DisplayName("创建标签应该发布正确的事件")
    void shouldPublishCorrectEventOnCreate() {
        // Given
        CreateTagCommand command = CreateTagCommand.of("Java");
        TagAggregate savedTag = TagAggregate.rebuild(TagId.of(1L), "Java");
        
        when(tagRepository.existsByName("Java")).thenReturn(false);
        when(tagRepository.save(any(TagAggregate.class))).thenReturn(savedTag);
        
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        
        // When
        commandService.createTag(command);
        
        // Then
        verify(eventPublisher).publish(eventCaptor.capture());
        DomainEvent publishedEvent = eventCaptor.getValue();
        
        assertTrue(publishedEvent instanceof TagCreatedEvent);
        TagCreatedEvent createdEvent = (TagCreatedEvent) publishedEvent;
        assertEquals("Java", createdEvent.getTagName());
    }
    
    @Test
    @DisplayName("更新标签时如果名称未变化应该跳过")
    void shouldSkipUpdateIfNoChange() {
        // Given
        TagId tagId = TagId.of(123L);
        UpdateTagCommand command = UpdateTagCommand.of(tagId, "Java");
        TagAggregate tag = TagAggregate.rebuild(tagId, "Java");
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        
        // When
        TagAggregate result = commandService.updateTagName(command);
        
        // Then
        assertNotNull(result);
        verify(tagRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}

