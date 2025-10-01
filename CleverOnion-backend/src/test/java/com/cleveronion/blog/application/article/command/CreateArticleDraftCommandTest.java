package com.cleveronion.blog.application.article.command;

import com.cleveronion.blog.domain.article.valueobject.ArticleContent;
import com.cleveronion.blog.domain.article.valueobject.AuthorId;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CreateArticleDraftCommand 单元测试
 * 
 * @author CleverOnion
 */
@DisplayName("创建文章草稿命令测试")
class CreateArticleDraftCommandTest {
    
    @Test
    @DisplayName("应该成功创建命令对象")
    void should_create_command_successfully() {
        // Given
        ArticleContent content = new ArticleContent("测试标题", "测试内容", "测试摘要");
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(1L);
        Set<TagId> tagIds = Set.of(new TagId(1L), new TagId(2L));
        
        // When
        CreateArticleDraftCommand command = new CreateArticleDraftCommand(
            content, categoryId, authorId, tagIds);
        
        // Then
        assertThat(command.getContent()).isEqualTo(content);
        assertThat(command.getCategoryId()).isEqualTo(categoryId);
        assertThat(command.getAuthorId()).isEqualTo(authorId);
        assertThat(command.getTagIds()).isEqualTo(tagIds);
    }
    
    @Test
    @DisplayName("内容为null时应该抛出异常")
    void should_throw_exception_when_content_is_null() {
        // Given
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(1L);
        
        // When & Then
        assertThatThrownBy(() -> new CreateArticleDraftCommand(
            null, categoryId, authorId, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("文章内容不能为空");
    }
    
    @Test
    @DisplayName("分类ID为null时应该抛出异常")
    void should_throw_exception_when_category_id_is_null() {
        // Given
        ArticleContent content = new ArticleContent("标题", "内容", "摘要");
        AuthorId authorId = new AuthorId(1L);
        
        // When & Then
        assertThatThrownBy(() -> new CreateArticleDraftCommand(
            content, null, authorId, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("分类ID不能为空");
    }
    
    @Test
    @DisplayName("作者ID为null时应该抛出异常")
    void should_throw_exception_when_author_id_is_null() {
        // Given
        ArticleContent content = new ArticleContent("标题", "内容", "摘要");
        CategoryId categoryId = new CategoryId(1L);
        
        // When & Then
        assertThatThrownBy(() -> new CreateArticleDraftCommand(
            content, categoryId, null, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("作者ID不能为空");
    }
    
    @Test
    @DisplayName("标签ID可以为null")
    void should_allow_null_tag_ids() {
        // Given
        ArticleContent content = new ArticleContent("标题", "内容", "摘要");
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(1L);
        
        // When
        CreateArticleDraftCommand command = new CreateArticleDraftCommand(
            content, categoryId, authorId, null);
        
        // Then
        assertThat(command.getTagIds()).isNull();
    }
    
    @Test
    @DisplayName("toString方法应该返回有意义的信息")
    void should_return_meaningful_toString() {
        // Given
        ArticleContent content = new ArticleContent("测试标题", "测试内容", null);
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(100L);
        
        // When
        CreateArticleDraftCommand command = new CreateArticleDraftCommand(
            content, categoryId, authorId, null);
        String result = command.toString();
        
        // Then
        assertThat(result).contains("测试标题");
        assertThat(result).contains("categoryId=1");
        assertThat(result).contains("authorId=100");
    }
}

