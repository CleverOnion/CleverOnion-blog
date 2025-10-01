package com.cleveronion.blog.application.category.command;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;

/**
 * 删除分类命令
 * 不可变对象，封装删除分类所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class DeleteCategoryCommand {
    
    private final CategoryId categoryId;
    
    private DeleteCategoryCommand(CategoryId categoryId) {
        this.categoryId = validateCategoryId(categoryId);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param categoryId 分类ID
     * @return 删除分类命令
     */
    public static DeleteCategoryCommand of(CategoryId categoryId) {
        return new DeleteCategoryCommand(categoryId);
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
    
    // Getters only (no setters - immutable)
    
    public CategoryId getCategoryId() {
        return categoryId;
    }
    
    @Override
    public String toString() {
        return "DeleteCategoryCommand{" +
                "categoryId=" + categoryId.getValue() +
                '}';
    }
}


