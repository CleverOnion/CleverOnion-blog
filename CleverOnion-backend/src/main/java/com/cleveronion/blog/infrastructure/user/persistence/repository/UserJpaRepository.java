package com.cleveronion.blog.infrastructure.user.persistence.repository;

import com.cleveronion.blog.infrastructure.user.persistence.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户JPA仓储接口
 * 继承JpaRepository，提供基础的CRUD操作
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserPO, Long> {
    
    /**
     * 根据GitHub用户ID查找用户
     * 
     * @param gitHubId GitHub用户ID
     * @return 用户持久化对象的Optional包装
     */
    Optional<UserPO> findByGitHubId(Long gitHubId);
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户持久化对象的Optional包装
     */
    Optional<UserPO> findByUsername(String username);
    
    /**
     * 检查GitHub用户ID是否存在
     * 
     * @param gitHubId GitHub用户ID
     * @return 是否存在
     */
    boolean existsByGitHubId(Long gitHubId);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据用户名模糊查询
     * 
     * @param usernamePattern 用户名模式
     * @return 匹配的用户列表
     */
    @Query("SELECT u FROM UserPO u WHERE u.username LIKE %:pattern%")
    List<UserPO> findByUsernameContaining(@Param("pattern") String usernamePattern);
}