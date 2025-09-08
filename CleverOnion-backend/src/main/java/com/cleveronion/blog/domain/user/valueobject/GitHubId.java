package com.cleveronion.blog.domain.user.valueobject;

import java.util.Objects;

/**
 * GitHub用户ID值对象
 * 封装GitHub用户ID的业务逻辑和验证规则
 */
public class GitHubId {
    
    private final Long value;
    
    private GitHubId(Long value) {
        this.value = value;
    }
    
    /**
     * 创建GitHub用户ID
     * 
     * @param value GitHub用户ID值
     * @return GitHubId实例
     * @throws IllegalArgumentException 当ID无效时
     */
    public static GitHubId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("GitHub用户ID不能为空");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("GitHub用户ID必须为正数");
        }
        return new GitHubId(value);
    }
    
    /**
     * 从字符串创建GitHub用户ID
     * 
     * @param value 字符串形式的GitHub用户ID
     * @return GitHubId实例
     * @throws IllegalArgumentException 当ID格式无效时
     */
    public static GitHubId fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("GitHub用户ID字符串不能为空");
        }
        try {
            Long id = Long.parseLong(value.trim());
            return of(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("GitHub用户ID格式无效: " + value, e);
        }
    }
    
    /**
     * 从整数创建GitHub用户ID
     * 
     * @param value 整数形式的GitHub用户ID
     * @return GitHubId实例
     */
    public static GitHubId fromInt(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("GitHub用户ID不能为空");
        }
        return of(value.longValue());
    }
    
    /**
     * 获取GitHub用户ID值
     * 
     * @return GitHub用户ID的Long值
     */
    public Long getValue() {
        return value;
    }
    
    /**
     * 检查是否为有效的GitHub用户ID
     * 
     * @return true如果ID有效
     */
    public boolean isValid() {
        return value != null && value > 0;
    }
    
    /**
     * 转换为字符串形式
     * 
     * @return 字符串形式的GitHub用户ID
     */
    public String asString() {
        return value.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitHubId gitHubId = (GitHubId) o;
        return Objects.equals(value, gitHubId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "GitHubId{" + value + "}";
    }
}