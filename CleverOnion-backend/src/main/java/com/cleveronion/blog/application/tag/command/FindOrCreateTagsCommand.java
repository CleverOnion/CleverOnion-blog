package com.cleveronion.blog.application.tag.command;

import java.util.Set;

/**
 * 查找或创建标签命令
 * 不可变对象，封装查找或创建标签所需的参数
 * 如果标签不存在则自动创建
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class FindOrCreateTagsCommand {
    
    private final Set<String> names;
    
    private FindOrCreateTagsCommand(Set<String> names) {
        this.names = validateNames(names);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param names 标签名称集合
     * @return 查找或创建标签命令
     */
    public static FindOrCreateTagsCommand of(Set<String> names) {
        return new FindOrCreateTagsCommand(names);
    }
    
    /**
     * 验证标签名称集合
     */
    private Set<String> validateNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("标签名称集合不能为空");
        }
        
        // 过滤和验证
        Set<String> validNames = names.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .peek(name -> {
                if (name.length() > 30) {
                    throw new IllegalArgumentException("标签名称长度不能超过30个字符: " + name);
                }
            })
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
        
        if (validNames.isEmpty()) {
            throw new IllegalArgumentException("标签名称集合不能全为空");
        }
        
        return validNames;
    }
    
    // Getters only (no setters - immutable)
    
    public Set<String> getNames() {
        return names;
    }
    
    @Override
    public String toString() {
        return "FindOrCreateTagsCommand{" +
                "names=" + names +
                '}';
    }
}

