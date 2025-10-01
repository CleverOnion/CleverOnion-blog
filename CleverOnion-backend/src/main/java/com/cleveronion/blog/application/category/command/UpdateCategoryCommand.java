package com.cleveronion.blog.application.category.command;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;

/**
 * 更新分类命令
 * 不可变对象，封装更新分类所需的所有参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class UpdateCategoryCommand {
    
    private final CategoryId categoryId;
    private final String name;
    private final String icon;
    
    private UpdateCategoryCommand(CategoryId categoryId, String name, String icon) {
        this.categoryId = validateCategoryId(categoryId);
        this.name = validateName(name);
        this.icon = icon;
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param categoryId 分类ID
     * @param name 新的分类名称
     * @param icon 新的分类图标（可为null）
     * @return 更新分类命令
     */
    public static UpdateCategoryCommand of(CategoryId categoryId, String name, String icon) {
        return new UpdateCategoryCommand(categoryId, name, icon);
    }
    
    /**
     * 验证分类ID
     */
    private CategoryId validateCategoryId(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        return categoryId;
    }
    
    /**
     * 验证分类名称
     */
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        String trimmed = name.trim();
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("分类名称长度不能超过50个字符");
        }
        
        return trimmed;
    }
    
    // Getters only (no setters - immutable)
    
    public CategoryId getCategoryId() {
        return categoryId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    @Override
    public String toString() {
        return "UpdateCategoryCommand{" +
                "categoryId=" + categoryId.getValue() +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}


