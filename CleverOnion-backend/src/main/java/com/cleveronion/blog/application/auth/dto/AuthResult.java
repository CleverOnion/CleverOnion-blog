package com.cleveronion.blog.application.auth.dto;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 认证结果 DTO
 * 封装认证成功后的结果信息
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class AuthResult {
    
    private final String accessToken;
    private final Long expiresIn;
    private final String tokenType;
    private final UserInfo userInfo;
    private final LocalDateTime loginTime;
    
    /**
     * 构造函数
     * 
     * @param accessToken 访问令牌
     * @param expiresIn 过期时间（秒）
     * @param tokenType 令牌类型
     * @param userAggregate 用户聚合
     */
    public AuthResult(String accessToken, Long expiresIn, 
                     String tokenType, UserAggregate userAggregate) {
        this.accessToken = Objects.requireNonNull(accessToken, "访问令牌不能为空");
        this.expiresIn = expiresIn;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.userInfo = userAggregate != null ? new UserInfo(userAggregate) : null;
        this.loginTime = LocalDateTime.now();
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
     * 获取过期时间（秒）
     * 
     * @return 过期时间
     */
    public Long getExpiresIn() {
        return expiresIn;
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
     * 获取用户信息
     * 
     * @return 用户信息
     */
    public UserInfo getUserInfo() {
        return userInfo;
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
     * 用户信息内部类
     */
    public static class UserInfo {
        private final Long id;
        private final Long gitHubId;
        private final String username;
        private final String avatarUrl;
        
        public UserInfo(UserAggregate userAggregate) {
            this.id = userAggregate.getId() != null ? userAggregate.getId().getValue() : null;
            this.gitHubId = userAggregate.getGitHubId() != null ? userAggregate.getGitHubId().getValue() : null;
            this.username = userAggregate.getUsername();
            this.avatarUrl = userAggregate.getAvatarUrl();
        }
        
        public Long getId() {
            return id;
        }
        
        public Long getGitHubId() {
            return gitHubId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        

        
        /**
         * 检查用户是否有头像
         * 
         * @return true如果有头像
         */
        public boolean hasAvatar() {
            return avatarUrl != null && !avatarUrl.trim().isEmpty();
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
        return "AuthResult{" +
                "accessToken='[HIDDEN]'" +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", userInfo=" + userInfo +
                ", loginTime=" + loginTime +
                '}';
    }
}