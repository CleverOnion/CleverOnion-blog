package com.cleveronion.blog.domain.user.valueobject;

import java.util.Objects;

/**
 * 用户ID值对象
 * 封装用户ID的业务逻辑和验证规则
 */
public class UserId {
    
    private final Long value;
    
    /**
     * 私有构造函数
     * 
     * @param value 用户ID值
     */
    private UserId(Long value) {
        this.value = value;
    }
    
    /**
     * 创建用户ID
     * 
     * @param value 用户ID值
     * @return UserId实例
     * @throws IllegalArgumentException 当ID无效时
     */
    public static UserId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        return new UserId(value);
    }
    
    /**
     * 从字符串创建用户ID
     * 
     * @param value 字符串形式的用户ID
     * @return UserId实例
     * @throws IllegalArgumentException 当ID格式无效时
     */
    public static UserId fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID字符串不能为空");
        }
        try {
            Long id = Long.parseLong(value.trim());
            return of(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("用户ID格式无效: " + value, e);
        }
    }
    
    /**
     * 获取用户ID值
     * 
     * @return 用户ID的Long值
     */
    public Long getValue() {
        return value;
    }
    
    /**
     * 检查是否为有效的用户ID
     * 
     * @return true如果ID有效
     */
    public boolean isValid() {
        return value != null && value > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "UserId{" + value + "}";
    }
}