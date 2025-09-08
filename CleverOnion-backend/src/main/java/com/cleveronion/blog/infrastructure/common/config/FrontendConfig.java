package com.cleveronion.blog.infrastructure.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 前端配置类
 * 用于读取前端相关的配置信息，避免在代码中硬编码前端地址
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "frontend")
public class FrontendConfig {
    
    /**
     * 前端基础URL
     */
    private String baseUrl;
    
    /**
     * 认证相关配置
     */
    private Auth auth = new Auth();
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public Auth getAuth() {
        return auth;
    }
    
    public void setAuth(Auth auth) {
        this.auth = auth;
    }
    
    /**
     * 认证相关配置
     */
    public static class Auth {
        
        /**
         * 认证回调路径
         */
        private String callbackPath;
        
        /**
         * 认证错误路径
         */
        private String errorPath;
        
        public String getCallbackPath() {
            return callbackPath;
        }
        
        public void setCallbackPath(String callbackPath) {
            this.callbackPath = callbackPath;
        }
        
        public String getErrorPath() {
            return errorPath;
        }
        
        public void setErrorPath(String errorPath) {
            this.errorPath = errorPath;
        }
    }
    
    /**
     * 获取完整的认证回调URL
     * 
     * @param accessToken 访问令牌
     * @param expiresIn 过期时间（秒）
     * @return 完整的回调URL
     */
    public String getAuthCallbackUrl(String accessToken, long expiresIn) {
        return String.format(
            "%s%s?access_token=%s&expires_in=%d",
            baseUrl,
            auth.callbackPath,
            accessToken,
            expiresIn
        );
    }
    
    /**
     * 获取完整的认证错误URL
     * 
     * @param message 错误消息
     * @return 完整的错误URL
     */
    public String getAuthErrorUrl(String message) {
        return String.format(
            "%s%s?message=%s",
            baseUrl,
            auth.errorPath,
            message
        );
    }
}