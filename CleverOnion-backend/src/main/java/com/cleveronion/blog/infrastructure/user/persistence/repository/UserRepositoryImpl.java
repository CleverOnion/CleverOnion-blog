package com.cleveronion.blog.infrastructure.user.persistence.repository;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.infrastructure.user.persistence.converter.UserConverter;
import com.cleveronion.blog.infrastructure.user.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户仓储实现类
 * 实现UserRepository接口，桥接领域层和基础设施层
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    
    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }
    
    @Override
    public UserAggregate save(UserAggregate userAggregate) {
        if (userAggregate == null) {
            throw new IllegalArgumentException("用户聚合不能为空");
        }
        
        UserPO userPO;
        
        if (userAggregate.isNewUser()) {
            // 新用户，直接转换并保存
            userPO = UserConverter.toUserPO(userAggregate);
        } else {
            // 已存在用户，先查找再更新
            Optional<UserPO> existingUserPO = userJpaRepository.findById(userAggregate.getId().getValue());
            if (existingUserPO.isPresent()) {
                userPO = existingUserPO.get();
                UserConverter.updateUserPO(userPO, userAggregate);
            } else {
                throw new IllegalStateException("要更新的用户不存在: " + userAggregate.getId().getValue());
            }
        }
        
        UserPO savedUserPO = userJpaRepository.save(userPO);
        return UserConverter.toUserAggregate(savedUserPO);
    }
    
    @Override
    public Optional<UserAggregate> findById(UserId userId) {
        if (userId == null) {
            return Optional.empty();
        }
        
        return userJpaRepository.findById(userId.getValue())
                .map(UserConverter::toUserAggregate);
    }
    
    @Override
    public List<UserAggregate> findByIds(Set<UserId> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        
        Set<Long> ids = userIds.stream()
                .map(UserId::getValue)
                .collect(Collectors.toSet());
        
        return userJpaRepository.findAllById(ids)
                .stream()
                .map(UserConverter::toUserAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<UserAggregate> findByGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) {
            return Optional.empty();
        }
        
        return userJpaRepository.findByGitHubId(gitHubId.getValue())
                .map(UserConverter::toUserAggregate);
    }
    
    @Override
    public Optional<UserAggregate> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return userJpaRepository.findByUsername(username.trim())
                .map(UserConverter::toUserAggregate);
    }
    
    @Override
    public boolean existsByGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) {
            return false;
        }
        
        return userJpaRepository.existsByGitHubId(gitHubId.getValue());
    }
    
    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        return userJpaRepository.existsByUsername(username.trim());
    }
    
    @Override
    public boolean existsById(UserId userId) {
        if (userId == null) {
            return false;
        }
        
        return userJpaRepository.existsById(userId.getValue());
    }
    
    @Override
    public boolean deleteById(UserId userId) {
        if (userId == null) {
            return false;
        }
        
        if (userJpaRepository.existsById(userId.getValue())) {
            userJpaRepository.deleteById(userId.getValue());
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<UserAggregate> findAll() {
        return userJpaRepository.findAll()
                .stream()
                .map(UserConverter::toUserAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserAggregate> findAll(int offset, int limit) {
        if (offset < 0 || limit <= 0) {
            throw new IllegalArgumentException("分页参数无效: offset=" + offset + ", limit=" + limit);
        }
        
        // 计算页码（从0开始）
        int page = offset / limit;
        
        return userJpaRepository.findAll(
                org.springframework.data.domain.PageRequest.of(page, limit)
        ).getContent()
                .stream()
                .map(UserConverter::toUserAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return userJpaRepository.count();
    }
    
    @Override
    public List<UserAggregate> findByUsernameContaining(String usernamePattern) {
        if (usernamePattern == null || usernamePattern.trim().isEmpty()) {
            return List.of();
        }
        
        return userJpaRepository.findByUsernameContaining(usernamePattern.trim())
                .stream()
                .map(UserConverter::toUserAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserAggregate> saveAll(List<UserAggregate> userAggregates) {
        if (userAggregates == null || userAggregates.isEmpty()) {
            return List.of();
        }
        
        List<UserPO> userPOs = userAggregates.stream()
                .map(UserConverter::toUserPO)
                .collect(Collectors.toList());
        
        List<UserPO> savedUserPOs = userJpaRepository.saveAll(userPOs);
        
        return savedUserPOs.stream()
                .map(UserConverter::toUserAggregate)
                .collect(Collectors.toList());
    }
}