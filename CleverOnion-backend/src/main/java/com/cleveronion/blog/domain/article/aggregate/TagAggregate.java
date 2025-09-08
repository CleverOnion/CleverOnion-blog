package com.cleveronion.blog.domain.article.aggregate;

import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.domain.common.aggregate.AggregateRoot;

import java.util.Objects;

/**
 * 标签聚合根
 * 负责管理标签的业务逻辑和规则
 * 
 * @author CleverOnion
 */
public class TagAggregate extends AggregateRoot {
    
    private TagId id;
    private String name;
    
    /**
     * 私有构造函数，防止外部直接实例化
     */
    private TagAggregate() {
    }
    
    /**
     * 创建新标签
     * 
     * @param name 标签名称
     * @return 新的标签实例
     */
    public static TagAggregate create(String name) {
        TagAggregate tag = new TagAggregate();
        // 新创建的标签不设置ID，由数据库自动生成
        tag.id = null;
        tag.name = validateAndTrimName(name);
        return tag;
    }
    
    /**
     * 验证并修剪标签名称
     * 
     * @param name 标签名称
     * @return 修剪后的标签名称
     * @throws IllegalArgumentException 当标签名称无效时抛出
     */
    private static String validateAndTrimName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        if (name.trim().length() > 255) {
            throw new IllegalArgumentException("标签名称长度不能超过255个字符");
        }
        return name.trim();
    }
    
    /**
     * 重建标签（用于从持久化存储恢复）
     * 
     * @param id 标签ID
     * @param name 标签名称
     * @return 重建的标签实例
     */
    public static TagAggregate rebuild(TagId id, String name) {
        TagAggregate tag = new TagAggregate();
        tag.id = id;
        tag.name = validateAndTrimName(name);
        return tag;
    }
    
    /**
     * 更新标签名称
     * 
     * @param newName 新的标签名称
     */
    public void updateName(String newName) {
        String validatedName = validateAndTrimName(newName);
        this.name = validatedName;
    }
    
    /**
     * 检查标签名称是否匹配
     * 
     * @param name 要检查的名称
     * @return 如果名称匹配返回true，否则返回false
     */
    public boolean hasName(String name) {
        if (name == null) {
            return false;
        }
        return this.name.equals(name.trim());
    }
    
    /**
     * 检查标签名称是否包含指定关键词（不区分大小写）
     * 
     * @param keyword 关键词
     * @return 如果包含关键词返回true，否则返回false
     */
    public boolean containsKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        return this.name.toLowerCase().contains(keyword.trim().toLowerCase());
    }
    
    /**
     * 检查标签名称是否以指定前缀开始（不区分大小写）
     * 
     * @param prefix 前缀
     * @return 如果以指定前缀开始返回true，否则返回false
     */
    public boolean startsWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return false;
        }
        return this.name.toLowerCase().startsWith(prefix.trim().toLowerCase());
    }
    
    /**
     * 获取标签名称长度
     * 
     * @return 标签名称的字符数
     */
    public int getNameLength() {
        return this.name.length();
    }
    
    /**
     * 检查是否为短标签（名称长度小于等于指定长度）
     * 
     * @param maxLength 最大长度
     * @return 如果是短标签返回true，否则返回false
     */
    public boolean isShortTag(int maxLength) {
        return this.name.length() <= maxLength;
    }
    
    // Getters
    public TagId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String getAggregateId() {
        return id != null ? id.getValue().toString() : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagAggregate tag = (TagAggregate) o;
        return Objects.equals(id, tag.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TagAggregate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}