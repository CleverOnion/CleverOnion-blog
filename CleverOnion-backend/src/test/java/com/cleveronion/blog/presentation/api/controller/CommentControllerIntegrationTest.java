package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.comment.dto.CommentWithRepliesDTO;
import com.cleveronion.blog.application.comment.service.CommentCommandService;
import com.cleveronion.blog.application.comment.service.CommentQueryService;
import com.cleveronion.blog.application.user.service.UserQueryService;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * CommentController 集成测试（新版评论系统）
 * 测试 /api/comments/top-level-with-replies 接口
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
@WebMvcTest(CommentController.class)
@DisplayName("CommentController 集成测试（新版评论系统）")
class CommentControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CommentQueryService commentQueryService;
    
    @MockBean
    private UserQueryService userQueryService;
    
    @MockBean
    private CommentCommandService commentCommandService;
    
    @Test
    @DisplayName("应该成功查询顶级评论及最新回复")
    void shouldGetTopLevelCommentsWithReplies() throws Exception {
        // Given
        Long articleId = 1L;
        
        // 模拟顶级评论
        CommentAggregate topComment = createMockComment(1L, "这是顶级评论", 1L, null, true);
        
        // 模拟最新回复
        CommentAggregate reply1 = createMockComment(2L, "这是回复1", 1L, 1L, false);
        CommentAggregate reply2 = createMockComment(3L, "这是回复2", 1L, 1L, false);
        
        CommentWithRepliesDTO dto = new CommentWithRepliesDTO(
            topComment, 
            5L,  // 回复总数
            List.of(reply1, reply2)  // 最新2条回复
        );
        
        when(commentQueryService.findTopLevelCommentsWithLatestReplies(
            any(), eq(0), eq(10), eq(3)))
            .thenReturn(List.of(dto));
        
        when(commentQueryService.countTopLevelByArticleId(any()))
            .thenReturn(1L);
        
        // 模拟用户信息
        UserAggregate user = createMockUser(1L, "TestUser");
        when(userQueryService.findById(any()))
            .thenReturn(Optional.of(user));
        
        // When & Then
        mockMvc.perform(get("/comments/top-level-with-replies")
                .param("articleId", articleId.toString())
                .param("page", "0")
                .param("size", "10")
                .param("replyLimit", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(jsonPath("$.data.comments").isArray())
            .andExpect(jsonPath("$.data.comments", hasSize(1)))
            // 验证顶级评论
            .andExpect(jsonPath("$.data.comments[0].id").value("1"))
            .andExpect(jsonPath("$.data.comments[0].content").value("这是顶级评论"))
            .andExpect(jsonPath("$.data.comments[0].reply_count").value(5))
            .andExpect(jsonPath("$.data.comments[0].latest_replies").isArray())
            .andExpect(jsonPath("$.data.comments[0].latest_replies", hasSize(2)))
            // 验证最新回复
            .andExpect(jsonPath("$.data.comments[0].latest_replies[0].id").value("2"))
            .andExpect(jsonPath("$.data.comments[0].latest_replies[1].id").value("3"))
            // 验证分页信息
            .andExpect(jsonPath("$.data.total_count").value(1))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(10));
    }
    
    @Test
    @DisplayName("应该返回没有回复的顶级评论")
    void shouldGetTopLevelCommentsWithoutReplies() throws Exception {
        // Given
        Long articleId = 1L;
        
        // 模拟顶级评论（无回复）
        CommentAggregate topComment = createMockComment(1L, "这是顶级评论", 1L, null, true);
        
        CommentWithRepliesDTO dto = new CommentWithRepliesDTO(
            topComment, 
            0L,  // 没有回复
            new ArrayList<>()  // 空的回复列表
        );
        
        when(commentQueryService.findTopLevelCommentsWithLatestReplies(
            any(), anyInt(), anyInt(), anyInt()))
            .thenReturn(List.of(dto));
        
        when(commentQueryService.countTopLevelByArticleId(any()))
            .thenReturn(1L);
        
        // 模拟用户信息
        UserAggregate user = createMockUser(1L, "TestUser");
        when(userQueryService.findById(any()))
            .thenReturn(Optional.of(user));
        
        // When & Then
        mockMvc.perform(get("/comments/top-level-with-replies")
                .param("articleId", articleId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.comments[0].reply_count").value(0))
            .andExpect(jsonPath("$.data.comments[0].latest_replies").isEmpty());
    }
    
    @Test
    @DisplayName("应该返回空列表当没有评论时")
    void shouldReturnEmptyListWhenNoComments() throws Exception {
        // Given
        Long articleId = 999L;
        
        when(commentQueryService.findTopLevelCommentsWithLatestReplies(
            any(), anyInt(), anyInt(), anyInt()))
            .thenReturn(new ArrayList<>());
        
        when(commentQueryService.countTopLevelByArticleId(any()))
            .thenReturn(0L);
        
        // When & Then
        mockMvc.perform(get("/comments/top-level-with-replies")
                .param("articleId", articleId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.comments").isEmpty())
            .andExpect(jsonPath("$.data.total_count").value(0));
    }
    
    @Test
    @DisplayName("应该使用默认参数值")
    void shouldUseDefaultParameters() throws Exception {
        // Given
        Long articleId = 1L;
        
        when(commentQueryService.findTopLevelCommentsWithLatestReplies(
            any(), eq(0), eq(10), eq(3)))  // 验证默认值
            .thenReturn(new ArrayList<>());
        
        when(commentQueryService.countTopLevelByArticleId(any()))
            .thenReturn(0L);
        
        // When & Then - 只传articleId，其他使用默认值
        mockMvc.perform(get("/comments/top-level-with-replies")
                .param("articleId", articleId.toString()))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("应该支持自定义replyLimit参数")
    void shouldSupportCustomReplyLimit() throws Exception {
        // Given
        Long articleId = 1L;
        
        when(commentQueryService.findTopLevelCommentsWithLatestReplies(
            any(), anyInt(), anyInt(), eq(5)))  // 验证自定义replyLimit
            .thenReturn(new ArrayList<>());
        
        when(commentQueryService.countTopLevelByArticleId(any()))
            .thenReturn(0L);
        
        // When & Then
        mockMvc.perform(get("/comments/top-level-with-replies")
                .param("articleId", articleId.toString())
                .param("replyLimit", "5"))
            .andExpect(status().isOk());
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 创建模拟的评论聚合对象
     */
    private CommentAggregate createMockComment(Long id, String content, Long userId, 
                                                Long parentId, boolean isTopLevel) {
        CommentAggregate comment = mock(CommentAggregate.class);
        
        when(comment.getId()).thenReturn(new CommentId(id));
        when(comment.getContent()).thenReturn(content);
        when(comment.getArticleId()).thenReturn(new ArticleId("1"));
        when(comment.getUserId()).thenReturn(new UserId(userId));
        when(comment.getParentId()).thenReturn(parentId != null ? new CommentId(parentId) : null);
        when(comment.isTopLevel()).thenReturn(isTopLevel);
        when(comment.getPublishedAt()).thenReturn(LocalDateTime.now());
        
        return comment;
    }
    
    /**
     * 创建模拟的用户聚合对象
     */
    private UserAggregate createMockUser(Long userId, String username) {
        UserAggregate user = mock(UserAggregate.class);
        
        when(user.getId()).thenReturn(new UserId(userId));
        when(user.getUsername()).thenReturn(username);
        when(user.getGitHubId()).thenReturn(null);
        when(user.getAvatarUrl()).thenReturn("https://example.com/avatar.jpg");
        
        return user;
    }
}

