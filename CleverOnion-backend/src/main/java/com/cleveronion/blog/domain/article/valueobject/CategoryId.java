package com.cleveronion.blog.domain.article.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 分类ID值对象
 * 在文章聚合中作为对分类聚合的引用
 * 作为不可变值对象，封装分类的唯一标识
 * 
 * @author CleverOnion
 */
public class CategoryId {
    
    private final Long value;
    
    /**
     * 构造分类ID值对象
     * 
     * @param value 分类ID值，不能为null且必须大于0
     * @throws IllegalArgumentException 当ID值无效时抛出
     */
    @JsonCreator
    public CategoryId(@JsonProperty("value") Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("分类ID必须是大于0的正整数");
        }
        this.value = value;
    }
    
    /**
     * 获取分类ID值
     * 
     * @return 分类ID值
     */
    public Long getValue() {
        return value;
    }
    
    /**
     * 生成新的分类ID
     * 
     * @return 新的分类ID值对象
     */
    public static CategoryId generate() {
        // 在实际应用中，这里应该使用ID生成策略（如雪花算法、UUID等）
        // 这里使用时间戳作为简单的ID生成策略
        return new CategoryId(System.currentTimeMillis());
    }
    
    /**
     * 创建分类ID值对象的静态工厂方法
     * 
     * @param value 分类ID值
     * @return 分类ID值对象
     */
    public static CategoryId of(Long value) {
        return new CategoryId(value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "CategoryId{" +
                "value=" + value +
                '}';
    }
}