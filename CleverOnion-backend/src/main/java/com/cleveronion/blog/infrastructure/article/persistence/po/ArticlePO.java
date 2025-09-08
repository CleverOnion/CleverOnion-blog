package com.cleveronion.blog.infrastructure.article.persistence.po;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 文章持久化对象
 * 映射数据库articles表
 * 
 * @author CleverOnion
 */
@Entity
@Table(name = "articles")
public class ArticlePO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "summary", length = 500)
    private String summary;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 默认构造函数（JPA要求）
    public ArticlePO() {}
    
    /**
     * 带参数的构造函数
     * 
     * @param title 标题
     * @param summary 摘要
     * @param content 内容
     * @param status 状态
     * @param categoryId 分类ID
     * @param authorId 作者ID
     */
    public ArticlePO(String title, String summary, String content, String status, Long categoryId, Long authorId) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.status = status;
        this.categoryId = categoryId;
        this.authorId = authorId;
    }
    
    /**
     * 优化查询用的构造函数（不包含content字段）
     * 
     * @param id ID
     * @param title 标题
     * @param summary 摘要
     * @param status 状态
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public ArticlePO(Long id, String title, String summary, String status, Long categoryId, Long authorId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.status = status;
        this.categoryId = categoryId;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticlePO articlePO = (ArticlePO) o;
        return Objects.equals(id, articlePO.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "ArticlePO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", authorId=" + authorId +
                ", categoryId=" + categoryId +
                '}';
    }
}