package com.cleveronion.blog.application.article.command;

import com.cleveronion.blog.domain.article.valueobject.ArticleContent;
import com.cleveronion.blog.domain.article.valueobject.AuthorId;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.article.valueobject.TagId;

import java.util.Objects;
import java.util.Set;

/**
 * 创建文章草稿命令
 * 封装创建文章草稿所需的所有参数
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
public class CreateArticleDraftCommand {
    
    private final ArticleContent content;
    private final CategoryId categoryId;
    private final AuthorId authorId;
    private final Set<TagId> tagIds;
    
    /**
     * 构造创建文章草稿命令
     * 
     * @param content 文章内容（不能为空）
     * @param categoryId 分类ID（不能为空）
     * @param authorId 作者ID（不能为空）
     * @param tagIds 标签ID集合（可选）
     */
    public CreateArticleDraftCommand(
            ArticleContent content,
            CategoryId categoryId,
            AuthorId authorId,
            Set<TagId> tagIds) {
        this.content = Objects.requireNonNull(content, "文章内容不能为空");
        this.categoryId = Objects.requireNonNull(categoryId, "分类ID不能为空");
        this.authorId = Objects.requireNonNull(authorId, "作者ID不能为空");
        this.tagIds = tagIds;
    }
    
    public ArticleContent getContent() {
        return content;
    }
    
    public CategoryId getCategoryId() {
        return categoryId;
    }
    
    public AuthorId getAuthorId() {
        return authorId;
    }
    
    public Set<TagId> getTagIds() {
        return tagIds;
    }
    
    @Override
    public String toString() {
        return "CreateArticleDraftCommand{" +
                "title='" + content.getTitle() + '\'' +
                ", categoryId=" + categoryId.getValue() +
                ", authorId=" + authorId.getValue() +
                ", tagCount=" + (tagIds != null ? tagIds.size() : 0) +
                '}';
    }
}


