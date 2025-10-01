package com.cleveronion.blog.application.auth.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.cleveronion.blog.application.auth.command.GitHubLoginCommand;
import com.cleveronion.blog.application.auth.dto.AuthResult;
import com.cleveronion.blog.application.user.command.SyncGitHubUserCommand;
import com.cleveronion.blog.application.user.service.UserCommandService;
import com.cleveronion.blog.application.user.service.UserQueryService;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.infrastructure.auth.client.GitHubOAuth2Client;
import com.cleveronion.blog.infrastructure.auth.client.dto.GitHubAccessTokenResponse;
import com.cleveronion.blog.infrastructure.auth.client.dto.GitHubUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务
 * 负责认证相关的业务流程编排
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class AuthApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthApplicationService.class);
    
    private final GitHubOAuth2Client gitHubOAuth2Client;
    private final UserCommandService userCommandService;
    
    public AuthApplicationService(GitHubOAuth2Client gitHubOAuth2Client,
                                UserCommandService userCommandService) {
        this.gitHubOAuth2Client = gitHubOAuth2Client;
        this.userCommandService = userCommandService;
    }
    
    /**
     * 生成GitHub OAuth2授权URL
     * 
     * @param state 状态参数，用于防止CSRF攻击
     * @return GitHub授权URL
     */
    public String generateGitHubAuthUrl(String state) {
        logger.info("生成GitHub OAuth2授权URL，状态参数: {}", state);
        return gitHubOAuth2Client.generateAuthorizationUrl(state);
    }
    
    /**
     * 处理GitHub OAuth2登录
     * 完整的登录流程：
     * 1. 使用授权码获取GitHub访问令牌
     * 2. 使用访问令牌获取GitHub用户信息
     * 3. 创建或更新本地用户
     * 4. 生成Sa-Token认证令牌
     * 
     * @param command GitHub登录命令
     * @return 认证结果
     * @throws AuthenticationException 当认证失败时抛出
     */
    public AuthResult loginWithGitHub(GitHubLoginCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("GitHub登录命令不能为空");
        }
        
        logger.info("开始处理GitHub OAuth2登录，授权码: {}", command.getCode());
        
        try {
            // 1. 获取GitHub访问令牌
            GitHubAccessTokenResponse tokenResponse = gitHubOAuth2Client.getAccessToken(command.getCode());
            logger.debug("成功获取GitHub访问令牌");
            
            // 2. 获取GitHub用户信息
            GitHubUserInfo gitHubUserInfo = gitHubOAuth2Client.getUserInfo(tokenResponse.getAccessToken());
            logger.debug("成功获取GitHub用户信息，用户ID: {}, 用户名: {}", 
                gitHubUserInfo.getId(), gitHubUserInfo.getLogin());
            
            // 3. 创建或更新本地用户
            // 构建同步用户命令
            SyncGitHubUserCommand syncCommand = SyncGitHubUserCommand.fromGitHubInfo(gitHubUserInfo);
            UserAggregate userAggregate = userCommandService.syncUserFromGitHub(syncCommand);
            logger.debug("成功创建或更新本地用户，用户ID: {}", userAggregate.getId().getValue());
            
            // 4. 生成Sa-Token认证令牌
            String userId = userAggregate.getId().getValue().toString();
            StpUtil.login(userId);
            
            String accessToken = StpUtil.getTokenValue();
            Long expiresIn = StpUtil.getTokenTimeout();
            
            logger.info("GitHub OAuth2登录成功，用户ID: {}, GitHub ID: {}, 用户名: {}", 
                userAggregate.getId().getValue(), 
                userAggregate.getGitHubId().getValue(), 
                userAggregate.getUsername());
            
            // 5. 构建认证结果
            return new AuthResult(
                accessToken,
                expiresIn,
                "Bearer",
                userAggregate
            );
            
        } catch (GitHubOAuth2Client.GitHubOAuth2Exception e) {
            logger.error("GitHub OAuth2认证失败: {}", e.getMessage(), e);
            throw new AuthenticationException("GitHub OAuth2认证失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("GitHub OAuth2登录过程中发生未知错误", e);
            throw new AuthenticationException("GitHub OAuth2登录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 用户登出
     * 
     * @param userId 用户ID
     */
    public void logout(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("尝试登出时用户ID为空");
            return;
        }
        
        try {
            // 检查用户是否已登录
            if (StpUtil.isLogin(userId)) {
                StpUtil.logout(userId);
                logger.info("用户登出成功，用户ID: {}", userId);
            } else {
                logger.debug("用户未登录，无需登出，用户ID: {}", userId);
            }
        } catch (Exception e) {
            logger.error("用户登出失败，用户ID: {}", userId, e);
            throw new AuthenticationException("用户登出失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查用户是否已登录
     * 
     * @param userId 用户ID
     * @return true如果已登录
     */
    @Transactional(readOnly = true)
    public boolean isLogin(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        
        try {
            return StpUtil.isLogin(userId);
        } catch (Exception e) {
            logger.error("检查用户登录状态失败，用户ID: {}", userId, e);
            return false;
        }
    }
    
    /**
     * 认证异常
     */
    public static class AuthenticationException extends RuntimeException {
        
        public AuthenticationException(String message) {
            super(message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}