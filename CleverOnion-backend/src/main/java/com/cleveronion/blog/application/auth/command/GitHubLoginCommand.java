package com.cleveronion.blog.application.auth.command;

import java.util.Objects;

/**
 * GitHub OAuth2 登录命令
 * 封装GitHub OAuth2登录所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class GitHubLoginCommand {
    
    private final String code;
    private final String state;
    
    /**
     * 构造函数
     * 
     * @param code GitHub OAuth2 授权码
     * @param state 状态参数，用于防止CSRF攻击
     */
    public GitHubLoginCommand(String code, String state) {
        this.code = Objects.requireNonNull(code, "授权码不能为空");
        this.state = state; // state可以为空
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
     * 获取状态参数
     * 
     * @return 状态参数
     */
    public String getState() {
        return state;
    }
    
    /**
     * 检查是否有状态参数
     * 
     * @return true如果有状态参数
     */
    public boolean hasState() {
        return state != null && !state.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitHubLoginCommand that = (GitHubLoginCommand) o;
        return Objects.equals(code, that.code) &&
               Objects.equals(state, that.state);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code, state);
    }
    
    @Override
    public String toString() {
        return "GitHubLoginCommand{" +
                "code='" + code + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}