package com.cleveronion.blog.domain.user.event;

import com.cleveronion.blog.domain.common.event.DomainEvent;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;

import java.util.Objects;

/**
 * 用户创建领域事件
 * 当新用户通过GitHub登录注册时触发
 */
public class UserCreated extends DomainEvent {
    
    private final UserId userId;
    private final GitHubId gitHubId;
    private final String username;
    private final String avatarUrl;
    
    public UserCreated(Object source, UserId userId, GitHubId gitHubId, String username, String avatarUrl) {
        super(source, userId.getValue().toString());
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.gitHubId = Objects.requireNonNull(gitHubId, "GitHub用户ID不能为空");
        this.username = Objects.requireNonNull(username, "用户名不能为空");
        this.avatarUrl = avatarUrl;
    }
    

    
    /**
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public UserId getUserId() {
        return userId;
    }
    
    /**
     * 获取GitHub用户ID
     * 
     * @return GitHub用户ID
     */
    public GitHubId getGitHubId() {
        return gitHubId;
    }
    
    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * 获取头像URL
     * 
     * @return 头像URL
     */
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreated that = (UserCreated) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "UserCreated{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", userId=" + userId +
                ", gitHubId=" + gitHubId +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}