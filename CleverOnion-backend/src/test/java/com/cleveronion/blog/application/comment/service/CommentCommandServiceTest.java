package com.cleveronion.blog.application.comment.service;

import com.cleveronion.blog.application.comment.command.CreateCommentCommand;
import com.cleveronion.blog.application.comment.command.DeleteCommentCommand;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.event.CommentCreatedEvent;
import com.cleveronion.blog.domain.comment.event.CommentDeletedEvent;
import com.cleveronion.blog.domain.comment.repository.CommentRepository;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.common.event.DomainEvent;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import com.cleveronion.blog.domain.user.valueobject.UserId;
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
 * CommentCommandService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentCommandService 单元测试")
class CommentCommandServiceTest {
    
    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    private CommentCommandService commandService;
    
    @BeforeEach
    void setUp() {
        commandService = new CommentCommandService(commentRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("应该成功创建顶级评论")
    void shouldCreateTopLevelComment() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand("Great article!", 1L, 100L, null);
        CommentAggregate comment = mock(CommentAggregate.class);
        
        when(comment.getId()).thenReturn(new CommentId(1L));
        when(comment.getArticleId()).thenReturn(new ArticleId("1"));
        when(comment.getUserId()).thenReturn(UserId.of(100L));
        when(commentRepository.save(any(CommentAggregate.class))).thenReturn(comment);
        
        // When
        CommentAggregate result = commandService.createComment(command);
        
        // Then
        assertNotNull(result);
        verify(commentRepository).save(any(CommentAggregate.class));
        verify(eventPublisher).publish(any(CommentCreatedEvent.class));
    }
    
    @Test
    @DisplayName("应该成功创建回复评论")
    void shouldCreateReplyComment() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand("Nice comment!", 1L, 100L, 10L);
        CommentAggregate parentComment = mock(CommentAggregate.class);
        CommentAggregate replyComment = mock(CommentAggregate.class);
        
        when(parentComment.getArticleId()).thenReturn(new ArticleId("1"));
        when(commentRepository.findById(new CommentId(10L))).thenReturn(Optional.of(parentComment));
        when(replyComment.getId()).thenReturn(new CommentId(2L));
        when(replyComment.getArticleId()).thenReturn(new ArticleId("1"));
        when(replyComment.getUserId()).thenReturn(UserId.of(100L));
        when(commentRepository.save(any(CommentAggregate.class))).thenReturn(replyComment);
        
        // When
        CommentAggregate result = commandService.createComment(command);
        
        // Then
        assertNotNull(result);
        verify(commentRepository).findById(new CommentId(10L));
        verify(commentRepository).save(any(CommentAggregate.class));
        verify(eventPublisher).publish(any(CommentCreatedEvent.class));
    }
    
    @Test
    @DisplayName("创建回复时应该验证父评论存在")
    void shouldValidateParentCommentExists() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand("Reply", 1L, 100L, 999L);
        when(commentRepository.findById(new CommentId(999L))).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.createComment(command)
        );
        
        assertTrue(exception.getMessage().contains("父评论不存在"));
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建回复时应该验证父评论属于同一文章")
    void shouldValidateParentCommentBelongsToSameArticle() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand("Reply", 1L, 100L, 10L);
        CommentAggregate parentComment = mock(CommentAggregate.class);
        
        when(parentComment.getArticleId()).thenReturn(new ArticleId("999")); // 不同的文章ID
        when(commentRepository.findById(new CommentId(10L))).thenReturn(Optional.of(parentComment));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.createComment(command)
        );
        
        assertTrue(exception.getMessage().contains("父评论不属于指定文章"));
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("应该成功删除评论")
    void shouldDeleteComment() {
        // Given
        DeleteCommentCommand command = new DeleteCommentCommand(1L, 100L);
        CommentAggregate comment = mock(CommentAggregate.class);
        
        when(comment.getUserId()).thenReturn(UserId.of(100L));
        when(comment.getArticleId()).thenReturn(new ArticleId("1"));
        when(commentRepository.findById(new CommentId(1L))).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentId(new CommentId(1L))).thenReturn(List.of());
        
        // When
        commandService.deleteComment(command);
        
        // Then
        verify(commentRepository).findById(new CommentId(1L));
        verify(commentRepository).deleteById(new CommentId(1L));
        verify(eventPublisher).publish(any(CommentDeletedEvent.class));
    }
    
    @Test
    @DisplayName("应该拒绝删除不存在的评论")
    void shouldRejectDeleteNonExistentComment() {
        // Given
        DeleteCommentCommand command = new DeleteCommentCommand(999L, 100L);
        when(commentRepository.findById(new CommentId(999L))).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.deleteComment(command)
        );
        
        assertTrue(exception.getMessage().contains("评论不存在"));
        verify(commentRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("应该拒绝删除他人的评论")
    void shouldRejectDeleteOthersComment() {
        // Given
        DeleteCommentCommand command = new DeleteCommentCommand(1L, 100L);
        CommentAggregate comment = mock(CommentAggregate.class);
        
        when(comment.getUserId()).thenReturn(UserId.of(200L)); // 不同的用户
        when(commentRepository.findById(new CommentId(1L))).thenReturn(Optional.of(comment));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.deleteComment(command)
        );
        
        assertTrue(exception.getMessage().contains("无权限删除该评论"));
        verify(commentRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("删除评论时应该递归删除子评论")
    void shouldDeleteCommentWithRepliesRecursively() {
        // Given
        DeleteCommentCommand command = new DeleteCommentCommand(1L, 100L);
        CommentAggregate comment = mock(CommentAggregate.class);
        CommentAggregate reply1 = mock(CommentAggregate.class);
        CommentAggregate reply2 = mock(CommentAggregate.class);
        
        when(comment.getUserId()).thenReturn(UserId.of(100L));
        when(comment.getArticleId()).thenReturn(new ArticleId("1"));
        when(reply1.getId()).thenReturn(new CommentId(2L));
        when(reply2.getId()).thenReturn(new CommentId(3L));
        
        when(commentRepository.findById(new CommentId(1L))).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentId(new CommentId(1L))).thenReturn(List.of(reply1, reply2));
        when(commentRepository.findByParentId(new CommentId(2L))).thenReturn(List.of());
        when(commentRepository.findByParentId(new CommentId(3L))).thenReturn(List.of());
        
        // When
        commandService.deleteComment(command);
        
        // Then
        verify(commentRepository, times(3)).deleteById(any()); // 删除3个评论（1个父+2个子）
        verify(eventPublisher).publish(any(CommentDeletedEvent.class));
    }
    
    @Test
    @DisplayName("创建评论应该发布正确的事件")
    void shouldPublishCorrectEventOnCreate() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand("Great!", 1L, 100L, null);
        CommentAggregate comment = mock(CommentAggregate.class);
        
        when(comment.getId()).thenReturn(new CommentId(1L));
        when(comment.getArticleId()).thenReturn(new ArticleId("1"));
        when(comment.getUserId()).thenReturn(UserId.of(100L));
        when(commentRepository.save(any(CommentAggregate.class))).thenReturn(comment);
        
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        
        // When
        commandService.createComment(command);
        
        // Then
        verify(eventPublisher).publish(eventCaptor.capture());
        DomainEvent publishedEvent = eventCaptor.getValue();
        
        assertTrue(publishedEvent instanceof CommentCreatedEvent);
        CommentCreatedEvent createdEvent = (CommentCreatedEvent) publishedEvent;
        assertEquals(new CommentId(1L), createdEvent.getCommentId());
    }
    
    @Test
    @DisplayName("删除评论应该发布正确的事件")
    void shouldPublishCorrectEventOnDelete() {
        // Given
        DeleteCommentCommand command = new DeleteCommentCommand(1L, 100L);
        CommentAggregate comment = mock(CommentAggregate.class);
        
        when(comment.getUserId()).thenReturn(UserId.of(100L));
        when(comment.getArticleId()).thenReturn(new ArticleId("1"));
        when(commentRepository.findById(new CommentId(1L))).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentId(new CommentId(1L))).thenReturn(List.of());
        
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        
        // When
        commandService.deleteComment(command);
        
        // Then
        verify(eventPublisher).publish(eventCaptor.capture());
        DomainEvent publishedEvent = eventCaptor.getValue();
        
        assertTrue(publishedEvent instanceof CommentDeletedEvent);
        CommentDeletedEvent deletedEvent = (CommentDeletedEvent) publishedEvent;
        assertEquals(new CommentId(1L), deletedEvent.getCommentId());
    }
}

