package com.cleveronion.blog.domain.user.aggregate;

import com.cleveronion.blog.domain.common.aggregate.AggregateRoot;
import com.cleveronion.blog.domain.user.event.UserCreated;
import com.cleveronion.blog.domain.user.event.UserProfileUpdated;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;

import java.util.Objects;

/**
 * 用户聚合根
 * 包含用户的所有业务逻辑和状态管理
 * 作为聚合根管理用户相关的领域事件
 */
public class UserAggregate extends AggregateRoot {
    
    private UserId id;
    private GitHubId gitHubId;
    private String username;
    private String avatarUrl;
    
    // 私有构造函数，防止外部直接实例化
    private UserAggregate() {}
    
    /**
     * 创建新用户聚合（用于GitHub登录注册）
     * 
     * @param gitHubId GitHub用户ID
     * @param username GitHub用户名
     * @param avatarUrl GitHub头像URL
     * @return 新创建的用户聚合实例
     */
    public static UserAggregate createFromGitHub(GitHubId gitHubId, String username, String avatarUrl) {
        if (gitHubId == null) {
            throw new IllegalArgumentException("GitHub用户ID不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        UserAggregate aggregate = new UserAggregate();
        // 新创建的用户不设置ID，由数据库自动生成
        aggregate.id = null;
        aggregate.gitHubId = gitHubId;
        aggregate.username = username.trim();
        aggregate.avatarUrl = avatarUrl;
        
        return aggregate;
    }
    
    /**
     * 重建用户聚合（用于从数据库加载）
     * 
     * @param id 用户ID
     * @param gitHubId GitHub用户ID
     * @param username 用户名
     * @param avatarUrl 头像URL
     * @return 重建的用户聚合实例
     */
    public static UserAggregate reconstruct(UserId id, GitHubId gitHubId, String username, String avatarUrl) {
        UserAggregate aggregate = new UserAggregate();
        aggregate.id = id;
        aggregate.gitHubId = gitHubId;
        aggregate.username = username;
        aggregate.avatarUrl = avatarUrl;
        
        return aggregate;
    }
    
    /**
     * 更新用户信息
     * 
     * @param username 新的用户名
     * @param avatarUrl 新的头像URL
     */
    public void updateProfile(String username, String avatarUrl) {
        String oldUsername = this.username;
        String oldAvatarUrl = this.avatarUrl;
        
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim();
        }
        this.avatarUrl = avatarUrl;
        
        // 发布用户信息更新事件
        if (!Objects.equals(oldUsername, this.username) || !Objects.equals(oldAvatarUrl, this.avatarUrl)) {
            addDomainEvent(new UserProfileUpdated(this, this.id, oldUsername, this.username, oldAvatarUrl, this.avatarUrl));
        }
    }
    
    /**
     * 更新头像
     * 
     * @param avatarUrl 新的头像URL
     */
    public void updateAvatar(String avatarUrl) {
        String oldAvatarUrl = this.avatarUrl;
        this.avatarUrl = avatarUrl;
        
        // 发布用户信息更新事件
        if (!Objects.equals(oldAvatarUrl, this.avatarUrl)) {
            addDomainEvent(new UserProfileUpdated(this, this.id, this.username, this.username, oldAvatarUrl, this.avatarUrl));
        }
    }
    
    /**
     * 设置用户ID（仅用于持久化后设置）
     * 
     * @param id 用户ID
     */
    public void setId(UserId id) {
        if (this.id != null) {
            throw new IllegalStateException("用户ID已经设置，不能重复设置");
        }
        this.id = id;
        
        // 发布用户创建事件
        if (id != null) {
            addDomainEvent(new UserCreated(this, id, this.gitHubId, this.username, this.avatarUrl));
        }
    }
    
    @Override
    public String getAggregateId() {
        return id != null ? id.getValue().toString() : null;
    }
    
    // 访问器方法
    public UserId getId() {
        return id;
    }
    
    public GitHubId getGitHubId() {
        return gitHubId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public boolean isNewUser() {
        return id == null;
    }
    
    public boolean hasValidUsername() {
        return username != null && !username.trim().isEmpty() && username.length() <= 255;
    }
    
    public boolean hasAvatar() {
        return avatarUrl != null && !avatarUrl.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAggregate that = (UserAggregate) o;
        // 如果有ID，按ID比较；否则按GitHubId比较
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(gitHubId, that.gitHubId);
    }
    
    @Override
    public int hashCode() {
        // 如果有ID，按ID计算hash；否则按GitHubId计算
        return id != null ? Objects.hash(id) : Objects.hash(gitHubId);
    }
    
    @Override
    public String toString() {
        return "UserAggregate{" +
                "id=" + id +
                ", gitHubId=" + gitHubId +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", domainEvents=" + getDomainEvents().size() +
                '}';
    }
}