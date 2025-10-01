package com.cleveronion.blog.application.tag.command;

import com.cleveronion.blog.domain.article.valueobject.TagId;

/**
 * 更新标签命令
 * 不可变对象，封装更新标签所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class UpdateTagCommand {
    
    private final TagId tagId;
    private final String newName;
    
    private UpdateTagCommand(TagId tagId, String newName) {
        this.tagId = validateTagId(tagId);
        this.newName = validateName(newName);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param tagId 标签ID
     * @param newName 新的标签名称
     * @return 更新标签命令
     */
    public static UpdateTagCommand of(TagId tagId, String newName) {
        return new UpdateTagCommand(tagId, newName);
    }
    
    /**
     * 验证标签ID
     */
    private TagId validateTagId(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        return tagId;
    }
    
    /**
     * 验证标签名称
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
    
    public TagId getTagId() {
        return tagId;
    }
    
    public String getNewName() {
        return newName;
    }
    
    @Override
    public String toString() {
        return "UpdateTagCommand{" +
                "tagId=" + tagId.getValue() +
                ", newName='" + newName + '\'' +
                '}';
    }
}

