package com.cleveronion.blog.application.user.command;

import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.infrastructure.auth.client.dto.GitHubUserInfo;

/**
 * 同步GitHub用户命令
 * 不可变对象，封装GitHub用户同步所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class SyncGitHubUserCommand {
    
    private final GitHubId gitHubId;
    private final String username;
    private final String avatarUrl;
    
    private SyncGitHubUserCommand(GitHubId gitHubId, String username, String avatarUrl) {
        this.gitHubId = validateGitHubId(gitHubId);
        this.username = validateUsername(username);
        this.avatarUrl = avatarUrl;
    }
    
    /**
     * 从GitHubUserInfo创建命令对象的工厂方法
     * 
     * @param info GitHub用户信息
     * @return 同步GitHub用户命令
     */
    public static SyncGitHubUserCommand fromGitHubInfo(GitHubUserInfo info) {
        return new SyncGitHubUserCommand(
            GitHubId.of(info.getId()),
            info.getDisplayName(),
            info.getAvatarUrl()
        );
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param gitHubId GitHub用户ID
     * @param username 用户名
     * @param avatarUrl 头像URL
     * @return 同步GitHub用户命令
     */
    public static SyncGitHubUserCommand of(GitHubId gitHubId, String username, String avatarUrl) {
        return new SyncGitHubUserCommand(gitHubId, username, avatarUrl);
    }
    
    /**
     * 验证GitHub ID
     */
    private GitHubId validateGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) {
            throw new IllegalArgumentException("GitHub用户ID不能为空");
        }
        return gitHubId;
    }
    
    /**
     * 验证用户名
     */
    private String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return username.trim();
    }
    
    // Getters only (no setters - immutable)
    
    public GitHubId getGitHubId() {
        return gitHubId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    @Override
    public String toString() {
        return "SyncGitHubUserCommand{" +
                "gitHubId=" + gitHubId.getValue() +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}

