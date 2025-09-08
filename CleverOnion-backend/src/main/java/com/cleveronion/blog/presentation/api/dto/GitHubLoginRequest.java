package com.cleveronion.blog.presentation.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * GitHub OAuth2 登录请求 DTO
 * 用于接收前端传递的 GitHub OAuth2 授权码和状态参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class GitHubLoginRequest {
    
    @JsonProperty("code")
    @NotBlank(message = "授权码不能为空")
    private String code;
    
    @JsonProperty("state")
    private String state;
    
    /**
     * 默认构造函数
     */
    public GitHubLoginRequest() {
    }
    
    /**
     * 构造函数
     * 
     * @param code 授权码
     * @param state 状态参数
     */
    public GitHubLoginRequest(String code, String state) {
        this.code = code;
        this.state = state;
    }
    
    /**
     * 获取授权码
     * 
     * @return 授权码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 设置授权码
     * 
     * @param code 授权码
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * 获取状态参数
     * 
     * @return 状态参数
     */
    public String getState() {
        return state;
    }
    
    /**
     * 设置状态参数
     * 
     * @param state 状态参数
     */
    public void setState(String state) {
        this.state = state;
    }
    
    /**
     * 检查请求是否有效
     * 
     * @return true如果请求有效
     */
    public boolean isValid() {
        return code != null && !code.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "GitHubLoginRequest{" +
                "code='" + (code != null ? "[HIDDEN]" : null) + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}