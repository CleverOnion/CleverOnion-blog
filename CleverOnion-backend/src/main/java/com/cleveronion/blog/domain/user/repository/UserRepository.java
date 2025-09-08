package com.cleveronion.blog.domain.user.repository;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 * 定义用户聚合数据访问的契约，遵循DDD仓储模式
 */
public interface UserRepository {
    
    /**
     * 保存用户聚合
     * 如果用户是新用户（ID为空），则创建新记录
     * 如果用户已存在，则更新现有记录
     * 
     * @param userAggregate 要保存的用户聚合
     * @return 保存后的用户聚合（包含生成的ID）
     */
    UserAggregate save(UserAggregate userAggregate);
    
    /**
     * 根据用户ID查找用户聚合
     * 
     * @param userId 用户ID
     * @return 用户聚合的Optional包装
     */
    Optional<UserAggregate> findById(UserId userId);
    
    /**
     * 根据GitHub用户ID查找用户聚合
     * 
     * @param gitHubId GitHub用户ID
     * @return 用户聚合的Optional包装
     */
    Optional<UserAggregate> findByGitHubId(GitHubId gitHubId);
    
    /**
     * 根据用户名查找用户聚合
     * 
     * @param username 用户名
     * @return 用户聚合的Optional包装
     */
    Optional<UserAggregate> findByUsername(String username);
    
    /**
     * 检查用户ID是否已存在
     * 
     * @param userId 用户ID
     * @return true如果已存在
     */
    boolean existsById(UserId userId);
    
    /**
     * 检查GitHub用户ID是否已存在
     * 
     * @param gitHubId GitHub用户ID
     * @return true如果已存在
     */
    boolean existsByGitHubId(GitHubId gitHubId);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return true如果已存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 删除用户
     * 
     * @param userId 要删除的用户ID
     * @return true如果删除成功
     */
    boolean deleteById(UserId userId);
    
    /**
     * 获取所有用户聚合
     * 注意：此方法主要用于管理功能，生产环境中应谨慎使用
     * 
     * @return 所有用户聚合列表
     */
    List<UserAggregate> findAll();
    
    /**
     * 分页获取用户聚合列表
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户聚合列表
     */
    List<UserAggregate> findAll(int offset, int limit);
    
    /**
     * 获取用户总数
     * 
     * @return 用户总数
     */
    long count();
    
    /**
     * 根据用户名模糊搜索用户聚合
     * 
     * @param usernamePattern 用户名模式（支持通配符）
     * @return 匹配的用户聚合列表
     */
    List<UserAggregate> findByUsernameContaining(String usernamePattern);
    
    /**
     * 批量保存用户聚合
     * 
     * @param userAggregates 要保存的用户聚合列表
     * @return 保存后的用户聚合列表
     */
    List<UserAggregate> saveAll(List<UserAggregate> userAggregates);
}