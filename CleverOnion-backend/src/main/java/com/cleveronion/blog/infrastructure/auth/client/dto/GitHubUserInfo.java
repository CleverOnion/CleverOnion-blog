package com.cleveronion.blog.infrastructure.auth.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub 用户信息 DTO
 * 用于接收 GitHub API 返回的用户信息
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class GitHubUserInfo {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("login")
    private String login;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("bio")
    private String bio;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("blog")
    private String blog;
    
    @JsonProperty("company")
    private String company;
    
    @JsonProperty("public_repos")
    private Integer publicRepos;
    
    @JsonProperty("followers")
    private Integer followers;
    
    @JsonProperty("following")
    private Integer following;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    // 默认构造函数
    public GitHubUserInfo() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getBlog() {
        return blog;
    }
    
    public void setBlog(String blog) {
        this.blog = blog;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public Integer getPublicRepos() {
        return publicRepos;
    }
    
    public void setPublicRepos(Integer publicRepos) {
        this.publicRepos = publicRepos;
    }
    
    public Integer getFollowers() {
        return followers;
    }
    
    public void setFollowers(Integer followers) {
        this.followers = followers;
    }
    
    public Integer getFollowing() {
        return following;
    }
    
    public void setFollowing(Integer following) {
        this.following = following;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 获取显示名称
     * 优先使用 name，如果为空则使用 login
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        return (name != null && !name.trim().isEmpty()) ? name : login;
    }
    
    @Override
    public String toString() {
        return "GitHubUserInfo{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}