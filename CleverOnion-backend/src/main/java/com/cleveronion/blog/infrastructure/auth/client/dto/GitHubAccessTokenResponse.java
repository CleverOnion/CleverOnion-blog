package com.cleveronion.blog.infrastructure.auth.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub Access Token 响应 DTO
 * 用于接收 GitHub OAuth2 Access Token 接口的响应
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class GitHubAccessTokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("scope")
    private String scope;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("error_description")
    private String errorDescription;
    
    @JsonProperty("error_uri")
    private String errorUri;
    
    // 默认构造函数
    public GitHubAccessTokenResponse() {}
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getErrorDescription() {
        return errorDescription;
    }
    
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
    
    public String getErrorUri() {
        return errorUri;
    }
    
    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
    }
    
    /**
     * 检查响应是否成功
     * 
     * @return true如果成功获取到access token
     */
    public boolean isSuccess() {
        return accessToken != null && !accessToken.trim().isEmpty() && error == null;
    }
    
    /**
     * 检查响应是否有错误
     * 
     * @return true如果有错误
     */
    public boolean hasError() {
        return error != null && !error.trim().isEmpty();
    }
    
    /**
     * 获取完整的错误信息
     * 
     * @return 错误信息
     */
    public String getFullErrorMessage() {
        if (!hasError()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder(error);
        if (errorDescription != null && !errorDescription.trim().isEmpty()) {
            sb.append(": ").append(errorDescription);
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "GitHubAccessTokenResponse{" +
                "accessToken='" + (accessToken != null ? "[HIDDEN]" : null) + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", scope='" + scope + '\'' +
                ", error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}