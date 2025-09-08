package com.cleveronion.blog.application.user.service;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.infrastructure.auth.client.dto.GitHubUserInfo;
import com.cleveronion.blog.presentation.api.dto.UserListResponse;
import com.cleveronion.blog.presentation.api.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户应用服务
 * 负责用户相关的业务流程编排
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class UserApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    
    private final UserRepository userRepository;
    
    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 通过GitHub用户信息创建或更新用户
     * 如果用户已存在，则更新用户信息
     * 如果用户不存在，则创建新用户
     * 
     * @param gitHubUserInfo GitHub用户信息
     * @return 用户聚合
     */
    public UserAggregate createOrUpdateUserFromGitHub(GitHubUserInfo gitHubUserInfo) {
        if (gitHubUserInfo == null) {
            throw new IllegalArgumentException("GitHub用户信息不能为空");
        }
        
        if (gitHubUserInfo.getId() == null) {
            throw new IllegalArgumentException("GitHub用户ID不能为空");
        }
        
        if (gitHubUserInfo.getLogin() == null || gitHubUserInfo.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("GitHub用户名不能为空");
        }
        
        GitHubId gitHubId = GitHubId.of(gitHubUserInfo.getId());
        
        logger.debug("开始处理GitHub用户，ID: {}, 用户名: {}", gitHubUserInfo.getId(), gitHubUserInfo.getLogin());
        
        // 查找是否已存在该GitHub用户
        Optional<UserAggregate> existingUser = userRepository.findByGitHubId(gitHubId);
        
        UserAggregate userAggregate;
        
        if (existingUser.isPresent()) {
            // 用户已存在，更新用户信息
            userAggregate = existingUser.get();
            logger.debug("用户已存在，更新用户信息，用户ID: {}", userAggregate.getId().getValue());
            
            // 更新用户信息（用户名和头像可能会变化）
            userAggregate.updateProfile(
                gitHubUserInfo.getDisplayName(),
                gitHubUserInfo.getAvatarUrl()
            );
        } else {
            // 用户不存在，创建新用户
            logger.debug("用户不存在，创建新用户");
            
            userAggregate = UserAggregate.createFromGitHub(
                gitHubId,
                gitHubUserInfo.getDisplayName(),
                gitHubUserInfo.getAvatarUrl()
            );
        }
        
        // 保存用户聚合
        UserAggregate savedUser = userRepository.save(userAggregate);
        
        logger.info("成功处理GitHub用户，用户ID: {}, GitHub ID: {}, 用户名: {}", 
            savedUser.getId().getValue(), 
            savedUser.getGitHubId().getValue(), 
            savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * 根据用户ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户聚合的Optional包装
     */
    @Transactional(readOnly = true)
    public Optional<UserAggregate> findById(UserId userId) {
        if (userId == null) {
            return Optional.empty();
        }
        
        return userRepository.findById(userId);
    }
    
    /**
     * 根据GitHub用户ID查找用户
     * 
     * @param gitHubId GitHub用户ID
     * @return 用户聚合的Optional包装
     */
    @Transactional(readOnly = true)
    public Optional<UserAggregate> findByGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) {
            return Optional.empty();
        }
        
        return userRepository.findByGitHubId(gitHubId);
    }
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户聚合的Optional包装
     */
    @Transactional(readOnly = true)
    public Optional<UserAggregate> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return userRepository.findByUsername(username.trim());
    }
    
    /**
     * 检查GitHub用户ID是否已存在
     * 
     * @param gitHubId GitHub用户ID
     * @return true如果已存在
     */
    @Transactional(readOnly = true)
    public boolean existsByGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) {
            return false;
        }
        
        return userRepository.existsByGitHubId(gitHubId);
    }
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return true如果已存在
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        return userRepository.existsByUsername(username.trim());
    }
    
    /**
     * 分页获取用户列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 用户列表响应
     */
    @Transactional(readOnly = true)
    public UserListResponse getUsersWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        if (size > 100) {
            throw new IllegalArgumentException("每页大小不能超过100");
        }
        
        logger.debug("开始分页查询用户列表，页码: {}, 每页大小: {}", page, size);
        
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
    @Transactional(readOnly = true)
    public long countUsers() {
        logger.debug("开始统计用户总数");
        
        long count = userRepository.count();
        
        logger.debug("用户总数统计完成: {}", count);
        
        return count;
    }
}