package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.application.auth.dto.AuthResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * 登录响应 DTO
 * 用于封装登录接口的响应数据
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class LoginResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    

    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("user_info")
    private UserInfo userInfo;
    
    @JsonProperty("login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    
    /**
     * 默认构造函数
     */
    public LoginResponse() {
    }
    
    /**
     * 从认证结果构造登录响应
     * 
     * @param authResult 认证结果
     */
    public LoginResponse(AuthResult authResult) {
        if (authResult != null) {
            this.accessToken = authResult.getAccessToken();
            this.expiresIn = authResult.getExpiresIn();
            this.tokenType = authResult.getTokenType();
            this.loginTime = authResult.getLoginTime();
            
            if (authResult.getUserInfo() != null) {
                this.userInfo = new UserInfo(authResult.getUserInfo());
            }
        }
    }
    
    /**
     * 获取访问令牌
     * 
     * @return 访问令牌
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * 设置访问令牌
     * 
     * @param accessToken 访问令牌
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    

    
    /**
     * 获取过期时间（秒）
     * 
     * @return 过期时间
     */
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    /**
     * 设置过期时间（秒）
     * 
     * @param expiresIn 过期时间
     */
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    /**
     * 获取令牌类型
     * 
     * @return 令牌类型
     */
    public String getTokenType() {
        return tokenType;
    }
    
    /**
     * 设置令牌类型
     * 
     * @param tokenType 令牌类型
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    /**
     * 设置用户信息
     * 
     * @param userInfo 用户信息
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    /**
     * 获取登录时间
     * 
     * @return 登录时间
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    /**
     * 设置登录时间
     * 
     * @param loginTime 登录时间
     */
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    /**
     * 用户信息内部类
     */
    public static class UserInfo {
        
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("github_id")
        private Long gitHubId;
        
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("avatar_url")
        private String avatarUrl;
        

        
        /**
         * 默认构造函数
         */
        public UserInfo() {
        }
        
        /**
         * 从认证结果的用户信息构造
         * 
         * @param authUserInfo 认证结果的用户信息
         */
        public UserInfo(AuthResult.UserInfo authUserInfo) {
            if (authUserInfo != null) {
                this.id = authUserInfo.getId();
                this.gitHubId = authUserInfo.getGitHubId();
                this.username = authUserInfo.getUsername();
                this.avatarUrl = authUserInfo.getAvatarUrl();
            }
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
            return "UserInfo{" +
                    "id=" + id +
                    ", gitHubId=" + gitHubId +
                    ", username='" + username + '\'' +
                    ", avatarUrl='" + avatarUrl + '\'' +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "accessToken='[HIDDEN]'" +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", userInfo=" + userInfo +
                ", loginTime=" + loginTime +
                '}';
    }
}