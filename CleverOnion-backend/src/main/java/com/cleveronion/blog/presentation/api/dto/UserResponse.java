package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户响应DTO
 * 用于返回用户信息
 * 
 * @author CleverOnion
 */
public class UserResponse {
    
    /**
     * 用户ID
     */
    @JsonProperty("id")
    private Long id;
    
    /**
     * GitHub ID
     */
    @JsonProperty("github_id")
    private Long gitHubId;
    
    /**
     * 用户名
     */
    @JsonProperty("username")
    private String username;
    
    /**
     * 头像URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    public UserResponse() {
    }
    
    public UserResponse(Long id, Long gitHubId, String username, String avatarUrl) {
        this.id = id;
        this.gitHubId = gitHubId;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
    
    /**
     * 从UserAggregate构造UserResponse
     * 
     * @param userAggregate 用户聚合根
     * @return 用户响应DTO
     */
    public static UserResponse from(UserAggregate userAggregate) {
        if (userAggregate == null) {
            return null;
        }
        
        return new UserResponse(
            userAggregate.getId() != null ? userAggregate.getId().getValue() : null,
            userAggregate.getGitHubId() != null ? userAggregate.getGitHubId().getValue() : null,
            userAggregate.getUsername(),
            userAggregate.getAvatarUrl()
        );
    }
    
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
    
    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", gitHubId=" + gitHubId +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}