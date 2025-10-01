package com.cleveronion.blog.application.tag.command;

/**
 * 创建标签命令
 * 不可变对象，封装创建标签所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class CreateTagCommand {
    
    private final String name;
    
    private CreateTagCommand(String name) {
        this.name = validateName(name);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param name 标签名称
     * @return 创建标签命令
     */
    public static CreateTagCommand of(String name) {
        return new CreateTagCommand(name);
    }
    
    /**
     * 验证标签名称
     * 
     * @param name 标签名称
     * @return 验证后的名称
     */
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        
        String trimmed = name.trim();
        if (trimmed.length() > 30) {
            throw new IllegalArgumentException("标签名称长度不能超过30个字符");
        }
        
        return trimmed;
    }
    
    // Getters only (no setters - immutable)
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "CreateTagCommand{" +
                "name='" + name + '\'' +
                '}';
    }
}

