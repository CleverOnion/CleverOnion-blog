package com.cleveronion.blog.domain.article.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

/**
 * 文章ID值对象
 * 封装文章的唯一标识符，确保领域层的纯粹性
 * 
 * @author CleverOnion
 */
public class ArticleId {
    
    private final String value;
    
    /**
     * 构造函数
     * 
     * @param value ID值，不能为空
     * @throws IllegalArgumentException 如果value为空或空字符串
     */
    @JsonCreator
    public ArticleId(@JsonProperty("value") String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        this.value = value.trim();
    }
    
    /**
     * 生成新的文章ID
     * 
     * @return 新的ArticleId实例
     */
    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID().toString());
    }
    
    /**
     * 从字符串创建ArticleId
     * 
     * @param value ID字符串
     * @return ArticleId实例
     */
    public static ArticleId of(String value) {
        return new ArticleId(value);
    }
    
    /**
     * 获取ID值
     * 
     * @return ID字符串
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 检查是否为有效的UUID格式
     * 
     * @return 如果是有效UUID格式返回true
     */
    @JsonIgnore
    public boolean isValidUuid() {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取ID的简短形式（前8位）
     * 
     * @return 简短ID字符串
     */
    @JsonIgnore
    public String getShortForm() {
        return value.length() > 8 ? value.substring(0, 8) : value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArticleId articleId = (ArticleId) obj;
        return Objects.equals(value, articleId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "ArticleId{" +
                "value='" + value + '\'' +
                '}';
    }
}