package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章响应 DTO
 * 用于封装文章接口的响应数据
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class ArticleResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("category")
    private CategoryResponse category;
    
    @JsonProperty("author")
    private UserResponse author;
    
    @JsonProperty("tags")
    private Set<TagResponse> tags;
    
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonProperty("published_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
    
    /**
     * 默认构造函数
     */
    public ArticleResponse() {
    }
    
    /**
     * 从文章聚合构造响应对象
     * 注意：此构造函数只设置基本字段，不包含关联实体
     * 需要使用带有关联实体参数的构造函数来设置完整信息
     * 
     * @param articleAggregate 文章聚合
     */
    public ArticleResponse(ArticleAggregate articleAggregate) {
        if (articleAggregate != null) {
            this.id = articleAggregate.getId() != null ? articleAggregate.getId().getValue() : null;
            this.title = articleAggregate.getContent() != null ? articleAggregate.getContent().getTitle() : null;
            this.content = articleAggregate.getContent() != null ? articleAggregate.getContent().getContent() : null;
            this.summary = articleAggregate.getContent() != null ? articleAggregate.getContent().getSummary() : null;
            this.status = articleAggregate.getStatus() != null ? articleAggregate.getStatus().name() : null;
            this.publishedAt = articleAggregate.getPublishedAt();
            // Note: createdAt and updatedAt are not available in ArticleAggregate
            // These fields will be null unless set through the full constructor
        }
    }
    
    /**
     * 构造函数
     * 
     * @param id 文章ID
     * @param title 文章标题
     * @param content 文章内容
     * @param summary 文章摘要
     * @param status 文章状态
     * @param category 分类信息
     * @param author 作者信息
     * @param tags 标签集合
     */
    public ArticleResponse(String id, String title, String content, String summary, 
                          String status, CategoryResponse category, UserResponse author, Set<TagResponse> tags) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.status = status;
        this.category = category;
        this.author = author;
        this.tags = tags;
    }
    
    /**
     * 从文章聚合和关联实体构造完整的响应对象
     * 
     * @param articleAggregate 文章聚合
     * @param category 分类信息
     * @param author 作者信息
     * @param tags 标签集合
     */
    public ArticleResponse(ArticleAggregate articleAggregate, CategoryResponse category, 
                          UserResponse author, Set<TagResponse> tags) {
        this(articleAggregate);
        this.category = category;
        this.author = author;
        this.tags = tags;
    }
    
    /**
     * 获取文章ID
     * 
     * @return 文章ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 设置文章ID
     * 
     * @param id 文章ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 获取文章标题
     * 
     * @return 文章标题
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * 设置文章标题
     * 
     * @param title 文章标题
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 获取文章内容
     * 
     * @return 文章内容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置文章内容
     * 
     * @param content 文章内容
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 获取文章摘要
     * 
     * @return 文章摘要
     */
    public String getSummary() {
        return summary;
    }
    
    /**
     * 设置文章摘要
     * 
     * @param summary 文章摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    /**
     * 获取文章状态
     * 
     * @return 文章状态
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * 设置文章状态
     * 
     * @param status 文章状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * 获取分类信息
     * 
     * @return 分类信息
     */
    public CategoryResponse getCategory() {
        return category;
    }
    
    /**
     * 设置分类信息
     * 
     * @param category 分类信息
     */
    public void setCategory(CategoryResponse category) {
        this.category = category;
    }
    
    /**
     * 获取作者信息
     * 
     * @return 作者信息
     */
    public UserResponse getAuthor() {
        return author;
    }
    
    /**
     * 设置作者信息
     * 
     * @param author 作者信息
     */
    public void setAuthor(UserResponse author) {
        this.author = author;
    }
    
    /**
     * 获取标签集合
     * 
     * @return 标签集合
     */
    public Set<TagResponse> getTags() {
        return tags;
    }
    
    /**
     * 设置标签集合
     * 
     * @param tags 标签集合
     */
    public void setTags(Set<TagResponse> tags) {
        this.tags = tags;
    }
    
    /**
     * 获取创建时间
     * 
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * 设置创建时间
     * 
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * 获取更新时间
     * 
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * 设置更新时间
     * 
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 获取发布时间
     * 
     * @return 发布时间
     */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    /**
     * 设置发布时间
     * 
     * @param publishedAt 发布时间
     */
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    @Override
    public String toString() {
        return "ArticleResponse{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) + "..." : null) + '\'' +
                ", summary='" + summary + '\'' +
                ", status='" + status + '\'' +
                ", category=" + category +
                ", author=" + author +
                ", tags=" + tags +
                ", publishedAt=" + publishedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}