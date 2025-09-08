package com.cleveronion.blog.infrastructure.user.persistence.converter;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.infrastructure.user.persistence.po.UserPO;

/**
 * 用户聚合与持久化对象转换器
 * 负责UserAggregate和UserPO之间的相互转换
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class UserConverter {
    
    /**
     * 将UserAggregate转换为UserPO
     * 
     * @param userAggregate 用户聚合
     * @return 用户持久化对象
     */
    public static UserPO toUserPO(UserAggregate userAggregate) {
        if (userAggregate == null) {
            return null;
        }
        
        UserPO userPO = new UserPO(
            userAggregate.getGitHubId().getValue(),
            userAggregate.getUsername(),
            userAggregate.getAvatarUrl()
        );
        
        // 如果聚合有ID，说明是已存在的用户
        if (userAggregate.getId() != null) {
            userPO.setId(userAggregate.getId().getValue());
        }
        
        // 时间戳由基础设施层的@CreationTimestamp和@UpdateTimestamp注解自动管理
        
        return userPO;
    }
    
    /**
     * 将UserPO转换为UserAggregate
     * 
     * @param userPO 用户持久化对象
     * @return 用户聚合
     */
    public static UserAggregate toUserAggregate(UserPO userPO) {
        if (userPO == null) {
            return null;
        }
        
        // 使用reconstruct方法重建聚合
        UserAggregate userAggregate = UserAggregate.reconstruct(
            UserId.of(userPO.getId()),
            GitHubId.of(userPO.getGitHubId()),
            userPO.getUsername(),
            userPO.getAvatarUrl()
        );
        
        return userAggregate;
    }
    
    /**
     * 更新UserPO的字段（用于更新操作）
     * 
     * @param userPO 要更新的持久化对象
     * @param userAggregate 包含新数据的聚合
     */
    public static void updateUserPO(UserPO userPO, UserAggregate userAggregate) {
        if (userPO == null || userAggregate == null) {
            return;
        }
        
        userPO.setUsername(userAggregate.getUsername());
        userPO.setAvatarUrl(userAggregate.getAvatarUrl());
        // updatedAt会由@UpdateTimestamp自动更新
    }
}