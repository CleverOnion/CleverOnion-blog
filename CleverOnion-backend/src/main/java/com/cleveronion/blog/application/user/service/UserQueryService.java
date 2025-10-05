package com.cleveronion.blog.application.user.service;

import com.cleveronion.blog.common.cache.CacheNames;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.presentation.api.dto.UserListResponse;
import com.cleveronion.blog.presentation.api.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户查询服务（CQRS - Query）
 * 负责处理所有用户查询操作（读操作）
 * 
 * <p>使用 Redis 缓存提升查询性能：
 * <ul>
 *   <li>用户详情缓存：30 分钟</li>
 *   <li>用户列表缓存：30 分钟</li>
 * </ul>
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
public class UserQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserQueryService.class);
    
    private final UserRepository userRepository;
    
    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 根据用户ID查找用户（带缓存）
     * 
     * <p>缓存Key: user:detail::{userId}
     * <p>缓存时间: 30分钟
     * 
     * @param userId 用户ID
     * @return 用户聚合的Optional包装
     */
    @Cacheable(
        cacheNames = CacheNames.USER_DETAIL,
        key = "#userId.value",
        unless = "#result == null"
    )
    public Optional<UserAggregate> findById(UserId userId) {
        if (userId == null) {
            return Optional.empty();
        }
        
        logger.debug("从数据库查询用户，ID: {}", userId.getValue());
        return userRepository.findById(userId);
    }
    
    /**
     * 根据用户ID集合批量查找用户
     * 注意：批量查询暂时不使用缓存，避免缓存键冲突问题
     * 
     * @param userIds 用户ID集合
     * @return 用户聚合列表
     */
    public List<UserAggregate> findByIds(Set<UserId> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            logger.debug("用户ID集合为空，返回空列表");
            return List.of();
        }
        
        logger.info("从数据库批量查询用户，用户ID数量: {}, IDs: {}", userIds.size(), userIds);
        List<UserAggregate> result = userRepository.findByIds(userIds);
        logger.info("批量查询用户完成，查询到 {} 个用户，结果: {}", 
            result.size(),
            result.stream().map(u -> "ID=" + u.getId().getValue() + ",Username=" + u.getUsername()).collect(Collectors.joining("; ")));
        return result;
    }
    
    /**
     * 根据GitHub用户ID查找用户
     * 
     * @param gitHubId GitHub用户ID
     * @return 用户聚合的Optional包装
     */
    public Optional<UserAggregate> findByGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) {
            return Optional.empty();
        }
        
        logger.debug("根据GitHub ID查询用户: {}", gitHubId.getValue());
        return userRepository.findByGitHubId(gitHubId);
    }
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户聚合的Optional包装
     */
    public Optional<UserAggregate> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        
        logger.debug("根据用户名查询用户: {}", username);
        return userRepository.findByUsername(username.trim());
    }
    
    /**
     * 检查GitHub用户ID是否已存在
     * 
     * @param gitHubId GitHub用户ID
     * @return true如果已存在
     */
    public boolean existsByGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) {
            return false;
        }
        
        logger.debug("检查GitHub ID是否存在: {}", gitHubId.getValue());
        return userRepository.existsByGitHubId(gitHubId);
    }
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return true如果已存在
     */
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        logger.debug("检查用户名是否存在: {}", username);
        return userRepository.existsByUsername(username.trim());
    }
    
    /**
     * 分页获取用户列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 用户列表响应
     */
    public UserListResponse findWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        if (size > 100) {
            throw new IllegalArgumentException("每页大小不能超过100");
        }
        
        logger.debug("分页查询用户列表，页码: {}, 每页大小: {}", page, size);
        
        // 计算偏移量
        int offset = page * size;
        
        // 获取分页用户列表
        List<UserAggregate> users = userRepository.findAll(offset, size);
        
        // 获取用户总数
        long totalCount = userRepository.count();
        
        // 转换为UserResponse列表
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        
        logger.debug("成功获取用户列表，总数: {}, 当前页用户数: {}", totalCount, userResponses.size());
        
        return UserListResponse.of(userResponses, totalCount, page, size);
    }
    
    /**
     * 获取用户总数
     * 
     * @return 用户总数
     */
    public long countAll() {
        logger.debug("统计用户总数");
        long count = userRepository.count();
        logger.debug("用户总数统计完成: {}", count);
        return count;
    }
}

