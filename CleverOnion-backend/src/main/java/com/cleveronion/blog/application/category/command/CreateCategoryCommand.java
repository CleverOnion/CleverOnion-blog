package com.cleveronion.blog.application.category.command;

/**
 * 创建分类命令
 * 不可变对象，封装创建分类所需的所有参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class CreateCategoryCommand {
    
    private final String name;
    private final String icon;
    
    private CreateCategoryCommand(String name, String icon) {
        this.name = validateName(name);
        this.icon = icon;
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param name 分类名称
     * @param icon 分类图标（可为null）
     * @return 创建分类命令
     */
    public static CreateCategoryCommand of(String name, String icon) {
        return new CreateCategoryCommand(name, icon);
    }
    
    /**
     * 验证分类名称
     * 
     * @param name 分类名称
     * @return 验证后的名称
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
    
    public String getName() {
        return name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    @Override
    public String toString() {
        return "CreateCategoryCommand{" +
                "name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}


