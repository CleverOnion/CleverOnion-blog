package com.cleveronion.blog.infrastructure.user.persistence.po;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户持久化对象
 * 映射数据库users表
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
public class UserPO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "github_id", nullable = false, unique = true)
    private Long gitHubId;
    
    @Column(name = "username", nullable = false, length = 255)
    private String username;
    
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 默认构造函数（JPA要求）
    public UserPO() {}
    
    /**
     * 构造函数
     * 
     * @param gitHubId GitHub用户ID
     * @param username 用户名
     * @param avatarUrl 头像URL
     */
    public UserPO(Long gitHubId, String username, String avatarUrl) {
        this.gitHubId = gitHubId;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getGitHubId() {
        return gitHubId;
    }
    
    public void setGitHubId(Long gitHubId) {
        this.gitHubId = gitHubId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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
        UserPO userPO = (UserPO) o;
        return Objects.equals(id, userPO.id) &&
               Objects.equals(gitHubId, userPO.gitHubId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, gitHubId);
    }
    
    @Override
    public String toString() {
        return "UserPO{" +
                "id=" + id +
                ", gitHubId=" + gitHubId +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}