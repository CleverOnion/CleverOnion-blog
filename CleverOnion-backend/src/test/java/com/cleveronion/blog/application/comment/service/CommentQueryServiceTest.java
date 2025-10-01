package com.cleveronion.blog.application.comment.service;

import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.repository.CommentRepository;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CommentQueryService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentQueryService 单元测试")
class CommentQueryServiceTest {
    
    @Mock
    private CommentRepository commentRepository;
    
    private CommentQueryService queryService;
    
    @BeforeEach
    void setUp() {
        queryService = new CommentQueryService(commentRepository);
    }
    
    @Test
    @DisplayName("应该根据ID查找评论")
    void shouldFindById() {
        // Given
        CommentId commentId = new CommentId(1L);
        CommentAggregate comment = mock(CommentAggregate.class);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        
        // When
        Optional<CommentAggregate> result = queryService.findById(commentId);
        
        // Then
        assertTrue(result.isPresent());
        verify(commentRepository).findById(commentId);
    }
    
    @Test
    @DisplayName("应该拒绝空的评论ID")
    void shouldRejectNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> queryService.findById(null));
    }
    
    @Test
    @DisplayName("应该根据文章ID查找评论")
    void shouldFindByArticleId() {
        // Given
        ArticleId articleId = new ArticleId("1");
        List<CommentAggregate> comments = List.of(mock(CommentAggregate.class));
        
        when(commentRepository.findByArticleId(articleId)).thenReturn(comments);
        
        // When
        List<CommentAggregate> result = queryService.findByArticleId(articleId);
        
        // Then
        assertEquals(1, result.size());
        verify(commentRepository).findByArticleId(articleId);
    }
    
    @Test
    @DisplayName("应该查找文章的顶级评论")
    void shouldFindTopLevelByArticleId() {
        // Given
        ArticleId articleId = new ArticleId("1");
        List<CommentAggregate> comments = List.of(mock(CommentAggregate.class));
        
        when(commentRepository.findTopLevelCommentsByArticleId(articleId)).thenReturn(comments);
        
        // When
        List<CommentAggregate> result = queryService.findTopLevelByArticleId(articleId);
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("应该分页查找顶级评论")
    void shouldFindTopLevelByArticleIdWithPage() {
        // Given
        ArticleId articleId = new ArticleId("1");
        List<CommentAggregate> comments = List.of(mock(CommentAggregate.class));
        
        when(commentRepository.findTopLevelCommentsByArticleId(articleId, 0, 10)).thenReturn(comments);
        
        // When
        List<CommentAggregate> result = queryService.findTopLevelByArticleId(articleId, 0, 10);
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("分页查询应该拒绝无效参数")
    void shouldRejectInvalidPaginationParams() {
        // Given
        ArticleId articleId = new ArticleId("1");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findTopLevelByArticleId(articleId, -1, 10));
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findTopLevelByArticleId(articleId, 0, 0));
    }
    
    @Test
    @DisplayName("应该查找评论的回复")
    void shouldFindRepliesByParentId() {
        // Given
        CommentId parentId = new CommentId(1L);
        List<CommentAggregate> replies = List.of(mock(CommentAggregate.class));
        
        when(commentRepository.findByParentId(parentId)).thenReturn(replies);
        
        // When
        List<CommentAggregate> result = queryService.findRepliesByParentId(parentId);
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("应该分页查找回复")
    void shouldFindRepliesByParentIdWithPage() {
        // Given
        CommentId parentId = new CommentId(1L);
        List<CommentAggregate> replies = List.of(mock(CommentAggregate.class));
        
        when(commentRepository.findByParentId(parentId, 0, 5)).thenReturn(replies);
        
        // When
        List<CommentAggregate> result = queryService.findRepliesByParentId(parentId, 0, 5);
        
        // Then
        assertEquals(1, result.size());
    }
    
    @Test
    @DisplayName("应该统计文章的评论数")
    void shouldCountByArticleId() {
        // Given
        ArticleId articleId = new ArticleId("1");
        when(commentRepository.countByArticleId(articleId)).thenReturn(10L);
        
        // When
        long result = queryService.countByArticleId(articleId);
        
        // Then
        assertEquals(10L, result);
    }
    
    @Test
    @DisplayName("应该统计文章的顶级评论数")
    void shouldCountTopLevelByArticleId() {
        // Given
        ArticleId articleId = new ArticleId("1");
        when(commentRepository.countTopLevelCommentsByArticleId(articleId)).thenReturn(5L);
        
        // When
        long result = queryService.countTopLevelByArticleId(articleId);
        
        // Then
        assertEquals(5L, result);
    }
    
    @Test
    @DisplayName("应该统计评论的回复数")
    void shouldCountRepliesByParentId() {
        // Given
        CommentId parentId = new CommentId(1L);
        when(commentRepository.countRepliesByParentId(parentId)).thenReturn(3L);
        
        // When
        long result = queryService.countRepliesByParentId(parentId);
        
        // Then
        assertEquals(3L, result);
    }
}

