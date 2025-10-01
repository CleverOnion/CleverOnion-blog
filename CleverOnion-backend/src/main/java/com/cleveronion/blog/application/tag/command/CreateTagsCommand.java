package com.cleveronion.blog.application.tag.command;

import java.util.List;

/**
 * 批量创建标签命令
 * 不可变对象，封装批量创建标签所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class CreateTagsCommand {
    
    private final List<String> names;
    
    private CreateTagsCommand(List<String> names) {
        this.names = validateNames(names);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param names 标签名称列表
     * @return 批量创建标签命令
     */
    public static CreateTagsCommand of(List<String> names) {
        return new CreateTagsCommand(names);
    }
    
    /**
     * 验证标签名称列表
     */
    private List<String> validateNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("标签名称列表不能为空");
        }
        
        // 过滤、去重、验证
        List<String> validNames = names.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .distinct()
            .peek(name -> {
                if (name.length() > 30) {
                    throw new IllegalArgumentException("标签名称长度不能超过30个字符: " + name);
                }
            })
            .toList();
        
        if (validNames.isEmpty()) {
            throw new IllegalArgumentException("标签名称列表不能全为空");
        }
        
        return validNames;
    }
    
    // Getters only (no setters - immutable)
    
    public List<String> getNames() {
        return names;
    }
    
    @Override
    public String toString() {
        return "CreateTagsCommand{" +
                "names=" + names +
                '}';
    }
}

