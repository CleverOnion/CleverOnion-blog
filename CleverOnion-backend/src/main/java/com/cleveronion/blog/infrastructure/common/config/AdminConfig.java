package com.cleveronion.blog.infrastructure.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 管理员配置类
 * 从配置文件中读取管理员GitHub ID列表
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminConfig {
    
    /**
     * 管理员GitHub ID列表
     */
    private List<Long> githubIds;
    
    /**
     * 获取管理员GitHub ID列表
     * 
     * @return 管理员GitHub ID列表
     */
    public List<Long> getGithubIds() {
        return githubIds;
    }
    
    /**
     * 设置管理员GitHub ID列表
     * 
     * @param githubIds 管理员GitHub ID列表
     */
    public void setGithubIds(List<Long> githubIds) {
        this.githubIds = githubIds;
    }
    
    /**
     * 检查指定的GitHub ID是否为管理员
     * 
     * @param githubId GitHub ID
     * @return true如果是管理员，false否则
     */
    public boolean isAdmin(Long githubId) {
        if (githubId == null || githubIds == null || githubIds.isEmpty()) {
            return false;
        }
        return githubIds.contains(githubId);
    }
}