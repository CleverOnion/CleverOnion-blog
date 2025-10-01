package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.application.article.command.CreateArticleDraftCommand;
import com.cleveronion.blog.application.article.command.PublishArticleCommand;
import com.cleveronion.blog.application.article.command.UpdateArticleCommand;
import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.valueobject.*;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ArticleCommandService 单元测试
 * 
 * @author CleverOnion
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文章命令服务测试")
class ArticleCommandServiceTest {
    
    @Mock(lenient = true)
    private ArticleRepository articleRepository;
    
    @Mock(lenient = true)
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private ArticleCommandService commandService;
    
    @Nested
    @DisplayName("创建文章草稿测试")
    class CreateDraftTests {
        
        @Test
        @DisplayName("应该成功创建文章草稿")
        void should_create_draft_successfully() {
            // Given
            ArticleContent content = new ArticleContent("测试标题", "测试内容", "测试摘要");
            CategoryId categoryId = new CategoryId(1L);
            AuthorId authorId = new AuthorId(1L);
            Set<TagId> tagIds = Set.of(new TagId(1L), new TagId(2L));
            
            CreateArticleDraftCommand command = new CreateArticleDraftCommand(
                content, categoryId, authorId, tagIds);
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.getId()).thenReturn(new ArticleId("1"));
            when(mockArticle.getContent()).thenReturn(content);
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.save(any(ArticleAggregate.class))).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.createDraft(command);
            
            // Then
            assertThat(result).isNotNull();
            verify(articleRepository).save(any(ArticleAggregate.class));
        }
        
        @Test
        @DisplayName("创建时应该发布领域事件")
        void should_publish_domain_events_when_create() {
            // Given
            CreateArticleDraftCommand command = new CreateArticleDraftCommand(
                new ArticleContent("标题", "内容", "摘要"),
                new CategoryId(1L),
                new AuthorId(1L),
                null
            );
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.getId()).thenReturn(new ArticleId("1"));
            when(mockArticle.getContent()).thenReturn(command.getContent());
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            commandService.createDraft(command);
            
            // Then
            verify(articleRepository).save(any(ArticleAggregate.class));
        }
    }
    
    @Nested
    @DisplayName("创建并发布文章测试")
    class CreateAndPublishTests {
        
        @Test
        @DisplayName("应该成功创建并发布文章")
        void should_create_and_publish_successfully() {
            // Given
            PublishArticleCommand command = new PublishArticleCommand(
                new ArticleContent("标题", "内容", "摘要"),
                new CategoryId(1L),
                new AuthorId(1L),
                null
            );
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.getId()).thenReturn(new ArticleId("1"));
            when(mockArticle.getContent()).thenReturn(command.getContent());
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.createAndPublish(command);
            
            // Then
            assertThat(result).isNotNull();
            verify(articleRepository).save(any(ArticleAggregate.class));
        }
    }
    
    @Nested
    @DisplayName("更新文章测试")
    class UpdateContentTests {
        
        @Test
        @DisplayName("应该成功更新文章内容")
        void should_update_content_successfully() {
            // Given
            ArticleId articleId = new ArticleId("1");
            ArticleContent newContent = new ArticleContent("新标题", "新内容", "新摘要");
            AuthorId authorId = new AuthorId(1L);
            
            UpdateArticleCommand command = new UpdateArticleCommand(
                articleId, newContent, null, null, authorId);
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(authorId)).thenReturn(true);
            when(mockArticle.getId()).thenReturn(articleId);
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.updateContent(command);
            
            // Then
            assertThat(result).isNotNull();
            verify(articleRepository).findById(articleId);
            verify(mockArticle).updateContent(newContent);
            verify(articleRepository).save(any());
        }
        
        @Test
        @DisplayName("更新不存在的文章应该抛出异常")
        void should_throw_exception_when_article_not_found() {
            // Given
            ArticleId articleId = new ArticleId("999");
            UpdateArticleCommand command = new UpdateArticleCommand(
                articleId,
                new ArticleContent("标题", "内容", "摘要"),
                null,
                null,
                new AuthorId(1L)
            );
            
            when(articleRepository.findById(articleId)).thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> commandService.updateContent(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文章不存在");
        }
        
        @Test
        @DisplayName("非作者更新应该抛出异常")
        void should_throw_exception_when_not_author() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            AuthorId anotherAuthorId = new AuthorId(2L);
            
            UpdateArticleCommand command = new UpdateArticleCommand(
                articleId,
                new ArticleContent("标题", "内容", "摘要"),
                null,
                null,
                anotherAuthorId
            );
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(anotherAuthorId)).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            
            // When & Then
            assertThatThrownBy(() -> commandService.updateContent(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只有文章作者才能执行此操作");
        }
    }
    
    @Nested
    @DisplayName("发布文章测试")
    class PublishTests {
        
        @Test
        @DisplayName("应该成功发布文章")
        void should_publish_successfully() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(authorId)).thenReturn(true);
            when(mockArticle.getId()).thenReturn(articleId);
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.publish(articleId, authorId);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockArticle).publish();
            verify(articleRepository).save(any());
        }
        
        @Test
        @DisplayName("发布不存在的文章应该抛出异常")
        void should_throw_exception_when_publish_non_existing_article() {
            // Given
            ArticleId articleId = new ArticleId("999");
            AuthorId authorId = new AuthorId(1L);
            
            when(articleRepository.findById(articleId)).thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> commandService.publish(articleId, authorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文章不存在");
        }
    }
    
    @Nested
    @DisplayName("归档文章测试")
    class ArchiveTests {
        
        @Test
        @DisplayName("应该成功归档文章")
        void should_archive_successfully() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(authorId)).thenReturn(true);
            when(mockArticle.getId()).thenReturn(articleId);
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.archive(articleId, authorId);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockArticle).archive();
            verify(articleRepository).save(any());
        }
    }
    
    @Nested
    @DisplayName("删除文章测试")
    class DeleteTests {
        
        @Test
        @DisplayName("应该成功删除文章")
        void should_delete_successfully() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(authorId)).thenReturn(true);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            
            // When
            commandService.delete(articleId, authorId);
            
            // Then
            verify(articleRepository).deleteById(articleId);
        }
        
        @Test
        @DisplayName("删除不存在的文章应该抛出异常")
        void should_throw_exception_when_delete_non_existing_article() {
            // Given
            ArticleId articleId = new ArticleId("999");
            AuthorId authorId = new AuthorId(1L);
            
            when(articleRepository.findById(articleId)).thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> commandService.delete(articleId, authorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文章不存在");
        }
        
        @Test
        @DisplayName("非作者删除应该抛出异常")
        void should_throw_exception_when_delete_by_non_author() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            AuthorId anotherAuthorId = new AuthorId(2L);
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(anotherAuthorId)).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            
            // When & Then
            assertThatThrownBy(() -> commandService.delete(articleId, anotherAuthorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只有文章作者才能执行此操作");
        }
    }
    
    @Nested
    @DisplayName("标签管理测试")
    class TagManagementTests {
        
        @Test
        @DisplayName("应该成功添加标签")
        void should_add_tags_successfully() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            Set<TagId> tagIds = Set.of(new TagId(1L), new TagId(2L));
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(authorId)).thenReturn(true);
            when(mockArticle.getId()).thenReturn(articleId);
            when(mockArticle.getTagCount()).thenReturn(2);
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.addTags(articleId, tagIds, authorId);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockArticle, times(2)).addTag(any(TagId.class));
            verify(articleRepository).save(any());
        }
        
        @Test
        @DisplayName("添加空标签集合应该抛出异常")
        void should_throw_exception_when_add_empty_tags() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            Set<TagId> emptyTagIds = Set.of();
            
            // When & Then
            assertThatThrownBy(() -> commandService.addTags(articleId, emptyTagIds, authorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标签ID集合不能为空");
        }
        
        @Test
        @DisplayName("应该成功移除标签")
        void should_remove_tags_successfully() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            Set<TagId> tagIds = Set.of(new TagId(1L));
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(authorId)).thenReturn(true);
            when(mockArticle.getId()).thenReturn(articleId);
            when(mockArticle.getTagCount()).thenReturn(0);
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.removeTags(articleId, tagIds, authorId);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockArticle).removeTag(any(TagId.class));
            verify(articleRepository).save(any());
        }
    }
    
    @Nested
    @DisplayName("撤回文章测试")
    class RevertToDraftTests {
        
        @Test
        @DisplayName("应该成功撤回文章到草稿")
        void should_revert_to_draft_successfully() {
            // Given
            ArticleId articleId = new ArticleId("1");
            AuthorId authorId = new AuthorId(1L);
            
            ArticleAggregate mockArticle = mock(ArticleAggregate.class);
            when(mockArticle.belongsToAuthor(authorId)).thenReturn(true);
            when(mockArticle.getId()).thenReturn(articleId);
            when(mockArticle.hasDomainEvents()).thenReturn(false);
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
            when(articleRepository.save(any())).thenReturn(mockArticle);
            
            // When
            ArticleAggregate result = commandService.revertToDraft(articleId, authorId);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockArticle).revertToDraft();
            verify(articleRepository).save(any());
        }
    }
}

