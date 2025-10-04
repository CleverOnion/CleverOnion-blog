package com.cleveronion.blog.domain.comment.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

/**
 * 评论ID值对象
 * 封装评论的唯一标识符，确保领域层的纯粹性
 * 
 * @author CleverOnion
 */
public class CommentId {
    
    private final Long value;
    
    /**
     * 构造函数
     * 
     * @param value ID值，不能为空
     * @throws IllegalArgumentException 如果value为空或小于等于0
     */
    @JsonCreator
    public CommentId(@JsonProperty("value") Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("评论ID不能为空且必须大于0");
        }
        this.value = value;
    }
    
    /**
     * 从Long值创建CommentId
     * 
     * @param value ID值
     * @return CommentId实例
     */
    public static CommentId of(Long value) {
        return new CommentId(value);
    }
    
    /**
     * 获取ID值
     * 
     * @return ID值
     */
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CommentId commentId = (CommentId) obj;
        return Objects.equals(value, commentId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "CommentId{" +
                "value=" + value +
                '}';
    }
}