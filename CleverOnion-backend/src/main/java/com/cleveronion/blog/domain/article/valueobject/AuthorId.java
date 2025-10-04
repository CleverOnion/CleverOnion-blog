package com.cleveronion.blog.domain.article.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 作者ID值对象
 * 在文章聚合中作为对用户聚合的引用
 * 作为不可变值对象，封装作者的唯一标识
 * 
 * @author CleverOnion
 */
public class AuthorId {
    
    private final Long value;
    
    /**
     * 构造作者ID值对象
     * 
     * @param value 作者ID值，不能为null且必须大于0
     * @throws IllegalArgumentException 当ID值无效时抛出
     */
    @JsonCreator
    public AuthorId(@JsonProperty("value") Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("作者ID必须是大于0的正整数");
        }
        this.value = value;
    }
    
    /**
     * 获取作者ID值
     * 
     * @return 作者ID值
     */
    public Long getValue() {
        return value;
    }
    
    /**
     * 创建作者ID值对象的静态工厂方法
     * 
     * @param value 作者ID值
     * @return 作者ID值对象
     */
    public static AuthorId of(Long value) {
        return new AuthorId(value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorId authorId = (AuthorId) o;
        return Objects.equals(value, authorId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "AuthorId{" +
                "value=" + value +
                '}';
    }
}