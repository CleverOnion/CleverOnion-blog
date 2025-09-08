package com.cleveronion.blog.application.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.cleveronion.blog.application.user.service.UserApplicationService;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.infrastructure.common.config.AdminConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 管理员应用服务
 * 处理管理员相关的业务逻辑
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
public class AdminApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminApplicationService.class);
    
    private final AdminConfig adminConfig;
    private final UserApplicationService userApplicationService;
    
    public AdminApplicationService(AdminConfig adminConfig, UserApplicationService userApplicationService) {
        this.adminConfig = adminConfig;
        this.userApplicationService = userApplicationService;
    }
    
    /**
     * 检查当前登录用户是否为管理员
     * 
     * @return true如果当前用户是管理员，false否则
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUserAdmin() {
        try {
            // 检查用户是否已登录
            if (!StpUtil.isLogin()) {
                logger.debug("用户未登录，不是管理员");
                return false;
            }
            
            // 获取当前登录用户ID
            String loginId = StpUtil.getLoginIdAsString();
            Long userId = Long.valueOf(loginId);
            
            logger.debug("检查用户是否为管理员，用户ID: {}", userId);
            
            // 根据用户ID查找用户信息
            UserId userIdVO = UserId.of(userId);
            Optional<UserAggregate> userOptional = userApplicationService.findById(userIdVO);
            
            if (userOptional.isEmpty()) {
                logger.warn("未找到用户信息，用户ID: {}", userId);
                return false;
            }
            
            UserAggregate user = userOptional.get();
            Long githubId = user.getGitHubId().getValue();
            
            // 检查GitHub ID是否在管理员列表中
            boolean isAdmin = adminConfig.isAdmin(githubId);
            
            logger.debug("用户管理员检查结果，用户ID: {}, GitHub ID: {}, 是否为管理员: {}", 
                userId, githubId, isAdmin);
            
            return isAdmin;
            
        } catch (Exception e) {
            logger.error("检查用户管理员权限时发生错误", e);
            return false;
        }
    }
    
    /**
     * 检查指定GitHub ID是否为管理员
     * 
     * @param githubId GitHub ID
     * @return true如果是管理员，false否则
     */
    @Transactional(readOnly = true)
    public boolean isAdminByGitHubId(Long githubId) {
        if (githubId == null) {
            return false;
        }
        
        boolean isAdmin = adminConfig.isAdmin(githubId);
        logger.debug("GitHub ID管理员检查结果，GitHub ID: {}, 是否为管理员: {}", githubId, isAdmin);
        
        return isAdmin;
    }
}