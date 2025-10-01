package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ArticleQueryService 单元测试
 * 
 * @author CleverOnion
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文章查询服务测试")
class ArticleQueryServiceTest {
    
    @Mock(lenient = true)
    private ArticleRepository articleRepository;
    
    @InjectMocks
    private ArticleQueryService queryService;
    
    @Nested
    @DisplayName("基础查询测试")
    class BasicQueryTests {
        
        @Test
        @DisplayName("应该成功通过ID查询文章")
        void should_find_article_by_id() {
            // Given
            ArticleId articleId = new ArticleId("1");
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            
            // When
            Optional<ArticleAggregate> result = queryService.findById(articleId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(mockArticle);
            verify(articleRepository).findById(articleId);
        }
        
        @Test
        @DisplayName("ID为null时应该抛出异常")
        void should_throw_exception_when_id_is_null() {
            // When & Then
            assertThatThrownBy(() -> queryService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文章ID不能为空");
        }
        
        @Test
        @DisplayName("文章不存在时应该返回空Optional")
        void should_return_empty_when_article_not_found() {
            // Given
            ArticleId articleId = new ArticleId("999");
            when(articleRepository.findById(articleId)).thenReturn(Optional.empty());
            
            // When
            Optional<ArticleAggregate> result = queryService.findById(articleId);
            
            // Then
            assertThat(result).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("分页查询测试")
    class PaginationQueryTests {
        
        @Test
        @DisplayName("应该成功查询已发布文章列表")
        void should_find_published_articles() {
            // Given
            List<ArticleAggregate> mockArticles = List.of(
                mock(ArticleAggregate.class), 
                mock(ArticleAggregate.class)
            );
            when(articleRepository.findPublishedArticles(0, 10)).thenReturn(mockArticles);
            
            // When
            List<ArticleAggregate> result = queryService.findPublishedArticles(0, 10);
            
            // Then
            assertThat(result).hasSize(2);
            verify(articleRepository).findPublishedArticles(0, 10);
        }
        
        @Test
        @DisplayName("页码为负数时应该抛出异常")
        void should_throw_exception_when_page_is_negative() {
            // When & Then
            assertThatThrownBy(() -> queryService.findPublishedArticles(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("页码不能小于0");
        }
        
        @Test
        @DisplayName("每页大小为0时应该抛出异常")
        void should_throw_exception_when_size_is_zero() {
            // When & Then
            assertThatThrownBy(() -> queryService.findPublishedArticles(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("每页大小必须在1-100之间");
        }
        
        @Test
        @DisplayName("每页大小超过100时应该抛出异常")
        void should_throw_exception_when_size_exceeds_limit() {
            // When & Then
            assertThatThrownBy(() -> queryService.findPublishedArticles(0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("每页大小必须在1-100之间");
        }
    }
    
    @Nested
    @DisplayName("搜索测试")
    class SearchTests {
        
        @Test
        @DisplayName("应该成功按标题搜索文章")
        void should_search_by_title() {
            // Given
            String keyword = "测试";
            List<ArticleAggregate> mockArticles = List.of(mock(ArticleAggregate.class));
            when(articleRepository.findByTitleContaining(keyword)).thenReturn(mockArticles);
            
            // When
            List<ArticleAggregate> result = queryService.searchByTitle(keyword);
            
            // Then
            assertThat(result).hasSize(1);
            verify(articleRepository).findByTitleContaining(keyword);
        }
        
        @Test
        @DisplayName("搜索关键词为空时应该抛出异常")
        void should_throw_exception_when_keyword_is_empty() {
            // When & Then
            assertThatThrownBy(() -> queryService.searchByTitle(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("搜索关键词不能为空");
        }
    }
    
    @Nested
    @DisplayName("统计测试")
    class CountTests {
        
        @Test
        @DisplayName("应该成功统计已发布文章数量")
        void should_count_published_articles() {
            // Given
            when(articleRepository.countPublishedArticles()).thenReturn(100L);
            
            // When
            long result = queryService.countPublishedArticles();
            
            // Then
            assertThat(result).isEqualTo(100L);
            verify(articleRepository).countPublishedArticles();
        }
        
        @Test
        @DisplayName("应该成功统计作者文章数量")
        void should_count_articles_by_author() {
            // Given
            AuthorId authorId = new AuthorId(1L);
            when(articleRepository.countByAuthorId(authorId)).thenReturn(50L);
            
            // When
            long result = queryService.countByAuthorId(authorId);
            
            // Then
            assertThat(result).isEqualTo(50L);
            verify(articleRepository).countByAuthorId(authorId);
        }
    }
}

