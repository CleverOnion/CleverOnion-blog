package com.cleveronion.blog.application.comment.service;

import com.cleveronion.blog.application.comment.dto.CommentWithRepliesDTO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    // ========== 新版评论系统测试（v2.0.0） ==========
    
    @Test
    @DisplayName("应该查询顶级评论及最新回复（有回复）")
    void shouldFindTopLevelCommentsWithLatestReplies_WithReplies() {
        // Given
        ArticleId articleId = new ArticleId("1");
        int page = 0;
        int size = 10;
        int replyLimit = 3;
        
        // 模拟顶级评论
        CommentAggregate topComment1 = mock(CommentAggregate.class);
        CommentAggregate topComment2 = mock(CommentAggregate.class);
        CommentId commentId1 = new CommentId(1L);
        CommentId commentId2 = new CommentId(2L);
        
        when(topComment1.getId()).thenReturn(commentId1);
        when(topComment2.getId()).thenReturn(commentId2);
        
        List<CommentAggregate> topComments = List.of(topComment1, topComment2);
        when(commentRepository.findTopLevelCommentsByArticleId(articleId, page, size))
            .thenReturn(topComments);
        
        // 模拟批量查询回复数
        Map<Long, Long> replyCountMap = new HashMap<>();
        replyCountMap.put(1L, 5L);
        replyCountMap.put(2L, 3L);
        when(commentRepository.countRepliesByParentIds(anyList())).thenReturn(replyCountMap);
        
        // 模拟最新回复
        CommentAggregate reply1 = mock(CommentAggregate.class);
        CommentAggregate reply2 = mock(CommentAggregate.class);
        when(commentRepository.findByParentId(commentId1, 0, replyLimit))
            .thenReturn(List.of(reply1, reply2));
        when(commentRepository.findByParentId(commentId2, 0, replyLimit))
            .thenReturn(List.of(reply1));
        
        // When
        List<CommentWithRepliesDTO> result = queryService.findTopLevelCommentsWithLatestReplies(
            articleId, page, size, replyLimit);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 验证第一个评论
        CommentWithRepliesDTO dto1 = result.get(0);
        assertEquals(topComment1, dto1.getComment());
        assertEquals(5L, dto1.getReplyCount());
        assertEquals(2, dto1.getLatestReplies().size());
        
        // 验证第二个评论
        CommentWithRepliesDTO dto2 = result.get(1);
        assertEquals(topComment2, dto2.getComment());
        assertEquals(3L, dto2.getReplyCount());
        assertEquals(1, dto2.getLatestReplies().size());
        
        // 验证方法调用
        verify(commentRepository).findTopLevelCommentsByArticleId(articleId, page, size);
        verify(commentRepository).countRepliesByParentIds(anyList());
        verify(commentRepository).findByParentId(commentId1, 0, replyLimit);
        verify(commentRepository).findByParentId(commentId2, 0, replyLimit);
    }
    
    @Test
    @DisplayName("应该查询顶级评论及最新回复（无回复）")
    void shouldFindTopLevelCommentsWithLatestReplies_NoReplies() {
        // Given
        ArticleId articleId = new ArticleId("1");
        int page = 0;
        int size = 10;
        int replyLimit = 3;
        
        // 模拟顶级评论（没有回复）
        CommentAggregate topComment = mock(CommentAggregate.class);
        CommentId commentId = new CommentId(1L);
        when(topComment.getId()).thenReturn(commentId);
        
        List<CommentAggregate> topComments = List.of(topComment);
        when(commentRepository.findTopLevelCommentsByArticleId(articleId, page, size))
            .thenReturn(topComments);
        
        // 模拟批量查询回复数（0条回复）
        Map<Long, Long> replyCountMap = new HashMap<>();
        replyCountMap.put(1L, 0L);
        when(commentRepository.countRepliesByParentIds(anyList())).thenReturn(replyCountMap);
        
        // When
        List<CommentWithRepliesDTO> result = queryService.findTopLevelCommentsWithLatestReplies(
            articleId, page, size, replyLimit);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        CommentWithRepliesDTO dto = result.get(0);
        assertEquals(topComment, dto.getComment());
        assertEquals(0L, dto.getReplyCount());
        assertTrue(dto.getLatestReplies().isEmpty());
        
        // 验证没有查询回复（因为回复数为0）
        verify(commentRepository, never()).findByParentId(any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("应该查询顶级评论及最新回复（空列表）")
    void shouldFindTopLevelCommentsWithLatestReplies_EmptyList() {
        // Given
        ArticleId articleId = new ArticleId("1");
        int page = 0;
        int size = 10;
        int replyLimit = 3;
        
        when(commentRepository.findTopLevelCommentsByArticleId(articleId, page, size))
            .thenReturn(new ArrayList<>());
        
        // When
        List<CommentWithRepliesDTO> result = queryService.findTopLevelCommentsWithLatestReplies(
            articleId, page, size, replyLimit);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证不会调用后续查询
        verify(commentRepository, never()).countRepliesByParentIds(anyList());
        verify(commentRepository, never()).findByParentId(any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("应该拒绝无效的查询参数")
    void shouldRejectInvalidParametersForFindWithReplies() {
        // Given
        ArticleId articleId = new ArticleId("1");
        
        // When & Then - 空文章ID
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findTopLevelCommentsWithLatestReplies(null, 0, 10, 3));
        
        // 负数页码
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findTopLevelCommentsWithLatestReplies(articleId, -1, 10, 3));
        
        // 无效的每页大小
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findTopLevelCommentsWithLatestReplies(articleId, 0, 0, 3));
        
        // 负数回复限制
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findTopLevelCommentsWithLatestReplies(articleId, 0, 10, -1));
    }
    
    @Test
    @DisplayName("应该支持replyLimit为0（不查询回复）")
    void shouldSupportZeroReplyLimit() {
        // Given
        ArticleId articleId = new ArticleId("1");
        int replyLimit = 0;
        
        CommentAggregate topComment = mock(CommentAggregate.class);
        CommentId commentId = new CommentId(1L);
        when(topComment.getId()).thenReturn(commentId);
        
        when(commentRepository.findTopLevelCommentsByArticleId(articleId, 0, 10))
            .thenReturn(List.of(topComment));
        
        Map<Long, Long> replyCountMap = new HashMap<>();
        replyCountMap.put(1L, 5L);
        when(commentRepository.countRepliesByParentIds(anyList())).thenReturn(replyCountMap);
        
        // When
        List<CommentWithRepliesDTO> result = queryService.findTopLevelCommentsWithLatestReplies(
            articleId, 0, 10, replyLimit);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getReplyCount());
        assertTrue(result.get(0).getLatestReplies().isEmpty());
        
        // 验证没有查询最新回复（replyLimit=0）
        verify(commentRepository, never()).findByParentId(any(), anyInt(), anyInt());
    }
}

