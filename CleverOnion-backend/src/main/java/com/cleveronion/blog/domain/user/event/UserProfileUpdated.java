package com.cleveronion.blog.domain.user.event;

import com.cleveronion.blog.domain.common.event.DomainEvent;
import com.cleveronion.blog.domain.user.valueobject.UserId;

import java.util.Objects;

/**
 * 用户资料更新领域事件
 * 当用户更新个人资料时触发
 */
public class UserProfileUpdated extends DomainEvent {
    
    private final UserId userId;
    private final String oldUsername;
    private final String newUsername;
    private final String oldAvatarUrl;
    private final String newAvatarUrl;
    
    public UserProfileUpdated(Object source, UserId userId, String oldUsername, String newUsername, 
                             String oldAvatarUrl, String newAvatarUrl) {
        super(source, userId.getValue().toString());
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.oldUsername = oldUsername;
        this.newUsername = newUsername;
        this.oldAvatarUrl = oldAvatarUrl;
        this.newAvatarUrl = newAvatarUrl;
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
     * 获取旧用户名
     * 
     * @return 旧用户名
     */
    public String getOldUsername() {
        return oldUsername;
    }
    
    /**
     * 获取新用户名
     * 
     * @return 新用户名
     */
    public String getNewUsername() {
        return newUsername;
    }
    
    /**
     * 获取旧头像URL
     * 
     * @return 旧头像URL
     */
    public String getOldAvatarUrl() {
        return oldAvatarUrl;
    }
    
    /**
     * 获取新头像URL
     * 
     * @return 新头像URL
     */
    public String getNewAvatarUrl() {
        return newAvatarUrl;
    }
    
    /**
     * 检查用户名是否发生变化
     * 
     * @return true如果用户名发生变化
     */
    public boolean isUsernameChanged() {
        return !Objects.equals(oldUsername, newUsername);
    }
    
    /**
     * 检查头像是否发生变化
     * 
     * @return true如果头像发生变化
     */
    public boolean isAvatarChanged() {
        return !Objects.equals(oldAvatarUrl, newAvatarUrl);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfileUpdated that = (UserProfileUpdated) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "UserProfileUpdated{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", userId=" + userId +
                ", oldUsername='" + oldUsername + '\'' +
                ", newUsername='" + newUsername + '\'' +
                ", oldAvatarUrl='" + oldAvatarUrl + '\'' +
                ", newAvatarUrl='" + newAvatarUrl + '\'' +
                '}';
    }
}