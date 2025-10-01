package com.cleveronion.blog.application.article.command;

import com.cleveronion.blog.domain.article.valueobject.ArticleContent;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.article.valueobject.AuthorId;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.article.valueobject.TagId;

import java.util.Objects;
import java.util.Set;

/**
 * 更新文章命令
 * 封装更新文章所需的所有参数
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
public class UpdateArticleCommand {
    
    private final ArticleId articleId;
    private final ArticleContent newContent;
    private final CategoryId newCategoryId;
    private final Set<TagId> newTagIds;
    private final AuthorId authorId;
    
    /**
     * 构造更新文章命令
     * 
     * @param articleId 文章ID（不能为空）
     * @param newContent 新的文章内容（不能为空）
     * @param newCategoryId 新的分类ID（可选，null表示不更新）
     * @param newTagIds 新的标签ID集合（可选，null表示不更新）
     * @param authorId 作者ID（不能为空，用于权限验证）
     */
    public UpdateArticleCommand(
            ArticleId articleId,
            ArticleContent newContent,
            CategoryId newCategoryId,
            Set<TagId> newTagIds,
            AuthorId authorId) {
        this.articleId = Objects.requireNonNull(articleId, "文章ID不能为空");
        this.newContent = Objects.requireNonNull(newContent, "文章内容不能为空");
        this.newCategoryId = newCategoryId;
        this.newTagIds = newTagIds;
        this.authorId = Objects.requireNonNull(authorId, "作者ID不能为空");
    }
    
    public ArticleId getArticleId() {
        return articleId;
    }
    
    public ArticleContent getNewContent() {
        return newContent;
    }
    
    public CategoryId getNewCategoryId() {
        return newCategoryId;
    }
    
    public Set<TagId> getNewTagIds() {
        return newTagIds;
    }
    
    public AuthorId getAuthorId() {
        return authorId;
    }
    
    @Override
    public String toString() {
        return "UpdateArticleCommand{" +
                "articleId=" + articleId.getValue() +
                ", title='" + newContent.getTitle() + '\'' +
                ", authorId=" + authorId.getValue() +
                ", updateCategory=" + (newCategoryId != null) +
                ", updateTags=" + (newTagIds != null) +
                '}';
    }
}


