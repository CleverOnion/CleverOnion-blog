package com.cleveronion.blog.domain.article.aggregate;

import com.cleveronion.blog.domain.article.valueobject.*;
import com.cleveronion.blog.domain.common.aggregate.AggregateRoot;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 文章聚合根
 * 管理文章的完整生命周期和业务规则
 * 作为聚合的唯一入口，确保数据一致性
 * 
 * @author CleverOnion
 */
public class ArticleAggregate extends AggregateRoot {
    @JsonProperty("id")
    private ArticleId id;
    
    @JsonProperty("content")
    private ArticleContent content;
    
    @JsonProperty("status")
    private ArticleStatus status;
    
    @JsonProperty("categoryId")
    private CategoryId categoryId;
    
    @JsonProperty("authorId")
    private AuthorId authorId;
    
    @JsonProperty("tagIds")
    private Set<TagId> tagIds;
    
    @JsonProperty("publishedAt")
    private LocalDateTime publishedAt;
    
    /**
     * 私有构造函数，防止外部直接实例化
     */
    private ArticleAggregate() {
        this.tagIds = new HashSet<>();
    }
    
    /**
     * 用于 JSON 反序列化的构造函数
     * 注意：此构造函数仅供 Jackson 使用，不应在业务代码中调用
     */
    @JsonCreator
    private ArticleAggregate(
            @JsonProperty("id") ArticleId id,
            @JsonProperty("content") ArticleContent content,
            @JsonProperty("status") ArticleStatus status,
            @JsonProperty("categoryId") CategoryId categoryId,
            @JsonProperty("authorId") AuthorId authorId,
            @JsonProperty("tagIds") Set<TagId> tagIds,
            @JsonProperty("publishedAt") LocalDateTime publishedAt) {
        this.id = id;
        this.content = content;
        this.status = status;
        this.categoryId = categoryId;
        this.authorId = authorId;
        this.tagIds = tagIds != null ? tagIds : new HashSet<>();
        this.publishedAt = publishedAt;
    }
    
    /**
     * 创建新文章（草稿状态）
     * 
     * @param content 文章内容
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 新创建的文章聚合根
     */
    public static ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
        if (content == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        ArticleAggregate article = new ArticleAggregate();
        // 新创建的文章不设置ID，由数据库自动生成
        article.id = null;
        article.content = content;
        article.status = ArticleStatus.DRAFT;
        article.categoryId = categoryId;
        article.authorId = authorId;
        
        return article;
    }
    
    /**
     * 重建文章聚合根（用于从持久化存储重建）
     * 
     * @param id 文章ID
     * @param content 文章内容
     * @param status 文章状态
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @param tagIds 标签ID集合
     * @param publishedAt 发布时间
     * @return 重建的文章聚合根
     */
    public static ArticleAggregate rebuild(ArticleId id, ArticleContent content, ArticleStatus status,
                                 CategoryId categoryId, AuthorId authorId, Set<TagId> tagIds, LocalDateTime publishedAt) {
        ArticleAggregate article = new ArticleAggregate();
        article.id = id;
        article.content = content;
        article.status = status;
        article.categoryId = categoryId;
        article.authorId = authorId;
        article.tagIds = tagIds != null ? new HashSet<>(tagIds) : new HashSet<>();
        article.publishedAt = publishedAt;
        
        return article;
    }
    
    /**
     * 更新文章内容
     * 
     * @param newContent 新的文章内容
     */
    public void updateContent(ArticleContent newContent) {
        if (newContent == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        this.content = newContent;
    }
    
    /**
     * 发布文章
     * 只有草稿状态的文章才能发布
     */
    public void publish() {
        if (!status.isDraft()) {
            throw new IllegalStateException("只有草稿状态的文章才能发布");
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    /**
     * 归档文章
     * 只有已发布的文章才能归档
     */
    public void archive() {
        if (!status.isPublished()) {
            throw new IllegalStateException("只有已发布的文章才能归档");
        }
        this.status = ArticleStatus.ARCHIVED;
    }
    
    /**
     * 撤回到草稿状态
     * 已发布或已归档的文章可以撤回到草稿状态
     */
    public void revertToDraft() {
        if (status.isDraft()) {
            throw new IllegalStateException("文章已经是草稿状态");
        }
        this.publishedAt = null;
        this.status = ArticleStatus.DRAFT;
    }
    
    /**
     * 更新分类
     * 
     * @param newCategoryId 新的分类ID
     */
    public void updateCategory(CategoryId newCategoryId) {
        if (newCategoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        this.categoryId = newCategoryId;
    }
    
    /**
     * 添加标签
     * 
     * @param tagId 标签ID
     */
    public void addTag(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        this.tagIds.add(tagId);
    }
    
    /**
     * 移除标签
     * 
     * @param tagId 标签ID
     */
    public void removeTag(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        this.tagIds.remove(tagId);
    }
    
    /**
     * 清空所有标签
     */
    public void clearTags() {
        this.tagIds.clear();
    }
    
    /**
     * 检查是否属于指定作者
     * 
     * @param authorId 作者ID
     * @return 如果属于指定作者返回true，否则返回false
     */
    public boolean belongsToAuthor(AuthorId authorId) {
        return this.authorId.equals(authorId);
    }
    
    /**
     * 检查是否包含指定标签
     * 
     * @param tagId 标签ID
     * @return 如果包含指定标签返回true，否则返回false
     */
    public boolean hasTag(TagId tagId) {
        return this.tagIds.contains(tagId);
    }
    
    /**
     * 获取标签数量
     * 
     * @return 标签数量
     */
    public int getTagCount() {
        return this.tagIds.size();
    }
    
    // Getters
    public ArticleId getId() {
        return id;
    }
    
    public ArticleContent getContent() {
        return content;
    }
    
    public ArticleStatus getStatus() {
        return status;
    }
    
    public CategoryId getCategoryId() {
        return categoryId;
    }
    
    public AuthorId getAuthorId() {
        return authorId;
    }
    
    public Set<TagId> getTagIds() {
        return new HashSet<>(tagIds);
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    @Override
    public String getAggregateId() {
        return id != null ? id.getValue().toString() : null;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleAggregate article = (ArticleAggregate) o;
        return Objects.equals(id, article.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "ArticleAggregate{" +
                "id=" + id +
                ", title='" + (content != null ? content.getTitle() : "null") + '\'' +
                ", status=" + status +
                ", authorId=" + authorId +
                ", tagCount=" + tagIds.size() +
                '}';
    }
}