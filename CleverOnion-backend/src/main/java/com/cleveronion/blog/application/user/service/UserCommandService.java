package com.cleveronion.blog.application.user.service;

import com.cleveronion.blog.application.user.command.SyncGitHubUserCommand;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户命令服务
 * 负责处理所有修改用户状态的操作
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class UserCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCommandService.class);
    
    private final UserRepository userRepository;
    
    public UserCommandService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 同步GitHub用户
     * 如果用户已存在则更新，不存在则创建
     * 
     * @param command 同步GitHub用户命令
     * @return 用户聚合
     */
    public UserAggregate syncUserFromGitHub(SyncGitHubUserCommand command) {
        logger.debug("执行GitHub用户同步命令: {}", command);
        
        Optional<UserAggregate> existing = userRepository.findByGitHubId(command.getGitHubId());
        
        UserAggregate user;
        if (existing.isPresent()) {
            // 更新用户
            user = existing.get();
            user.updateProfile(command.getUsername(), command.getAvatarUrl());
            logger.debug("更新已存在的GitHub用户，ID: {}", command.getGitHubId().getValue());
        } else {
            // 创建用户
            user = UserAggregate.createFromGitHub(
                command.getGitHubId(),
                command.getUsername(),
                command.getAvatarUrl()
            );
            logger.debug("创建新GitHub用户，ID: {}", command.getGitHubId().getValue());
        }
        
        UserAggregate saved = userRepository.save(user);
        
        logger.info("成功同步GitHub用户，用户ID: {}, GitHub ID: {}, 用户名: {}", 
            saved.getId().getValue(), 
            saved.getGitHubId().getValue(), 
            saved.getUsername());
        
        return saved;
    }
}

