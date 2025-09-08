package com.cleveronion.blog.domain.article.valueobject;

import java.util.Objects;

/**
 * 标签ID值对象
 * 在文章聚合中作为对标签聚合的引用
 * 作为不可变值对象，封装标签的唯一标识
 * 
 * @author CleverOnion
 */
public class TagId {
    
    private final Long value;
    
    /**
     * 构造标签ID值对象
     * 
     * @param value 标签ID值，不能为null且必须大于0
     * @throws IllegalArgumentException 当ID值无效时抛出
     */
    public TagId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("标签ID必须是大于0的正整数");
        }
        this.value = value;
    }
    
    /**
     * 获取标签ID值
     * 
     * @return 标签ID值
     */
    public Long getValue() {
        return value;
    }
    
    /**
     * 生成新的标签ID
     * 
     * @return 新的标签ID值对象
     */
    public static TagId generate() {
        // 在实际应用中，这里应该使用ID生成策略（如雪花算法、UUID等）
        // 这里使用时间戳作为简单的ID生成策略
        return new TagId(System.currentTimeMillis());
    }
    
    /**
     * 创建标签ID值对象的静态工厂方法
     * 
     * @param value 标签ID值
     * @return 标签ID值对象
     */
    public static TagId of(Long value) {
        return new TagId(value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagId tagId = (TagId) o;
        return Objects.equals(value, tagId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "TagId{" +
                "value=" + value +
                '}';
    }
}