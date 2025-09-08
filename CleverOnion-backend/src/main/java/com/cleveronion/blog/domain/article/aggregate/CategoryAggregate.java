package com.cleveronion.blog.domain.article.aggregate;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.common.aggregate.AggregateRoot;
import java.util.Objects;

/**
 * 分类聚合根
 * 负责管理分类的业务逻辑和规则
 * 
 * @author CleverOnion
 */
public class CategoryAggregate extends AggregateRoot {
    private CategoryId id;
    private String name;
    private String icon;
    
    /**
     * 私有构造函数，防止外部直接实例化
     */
    private CategoryAggregate() {
    }
    
    /**
     * 创建新分类
     * 
     * @param name 分类名称
     * @return 新的分类实例
     */
    public static CategoryAggregate create(String name) {
        CategoryAggregate category = new CategoryAggregate();
        // 新创建的分类不设置ID，由数据库自动生成
        category.id = null;
        category.name = validateAndTrimName(name);
        category.icon = null;
        return category;
    }
    
    /**
     * 创建新分类（包含图标）
     * 
     * @param name 分类名称
     * @param icon 分类图标
     * @return 新的分类实例
     */
    public static CategoryAggregate create(String name, String icon) {
        CategoryAggregate category = new CategoryAggregate();
        // 新创建的分类不设置ID，由数据库自动生成
        category.id = null;
        category.name = validateAndTrimName(name);
        category.icon = validateIcon(icon);
        return category;
    }
    
    /**
     * 验证并修剪分类名称
     * 
     * @param name 分类名称
     * @return 修剪后的分类名称
     * @throws IllegalArgumentException 当分类名称无效时抛出
     */
    private static String validateAndTrimName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (name.trim().length() > 255) {
            throw new IllegalArgumentException("分类名称长度不能超过255个字符");
        }
        return name.trim();
    }
    
    /**
     * 验证分类图标
     * 
     * @param icon 分类图标
     * @return 验证后的图标
     * @throws IllegalArgumentException 当图标无效时抛出
     */
    private static String validateIcon(String icon) {
        if (icon != null && icon.trim().length() > 255) {
            throw new IllegalArgumentException("分类图标长度不能超过255个字符");
        }
        return icon != null ? icon.trim() : null;
    }
    
    /**
     * 重建分类聚合根（用于从持久化存储重建）
     * 
     * @param id 分类ID
     * @param name 分类名称
     * @return 重建的分类实例
     */
    public static CategoryAggregate rebuild(CategoryId id, String name) {
        CategoryAggregate category = new CategoryAggregate();
        category.id = id;
        category.name = validateAndTrimName(name);
        category.icon = null;
        return category;
    }
    
    /**
     * 重建分类聚合根（用于从持久化存储重建，包含图标）
     * 
     * @param id 分类ID
     * @param name 分类名称
     * @param icon 分类图标
     * @return 重建的分类实例
     */
    public static CategoryAggregate rebuild(CategoryId id, String name, String icon) {
        CategoryAggregate category = new CategoryAggregate();
        category.id = id;
        category.name = validateAndTrimName(name);
        category.icon = validateIcon(icon);
        return category;
    }
    
    /**
     * 更新分类名称
     * 
     * @param newName 新的分类名称
     */
    public void updateName(String newName) {
        String validatedName = validateAndTrimName(newName);
        this.name = validatedName;
    }
    
    /**
     * 更新分类图标
     * 
     * @param newIcon 新的分类图标
     */
    public void updateIcon(String newIcon) {
        this.icon = validateIcon(newIcon);
    }
    
    /**
     * 检查分类名称是否匹配
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
     * 检查分类名称是否包含指定关键词（不区分大小写）
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
     * 获取分类名称长度
     * 
     * @return 分类名称的字符数
     */
    public int getNameLength() {
        return this.name.length();
    }
    
    // Getters
    public CategoryId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    @Override
    public String getAggregateId() {
        return id != null ? id.getValue().toString() : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryAggregate category = (CategoryAggregate) o;
        return Objects.equals(id, category.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "CategoryAggregate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}