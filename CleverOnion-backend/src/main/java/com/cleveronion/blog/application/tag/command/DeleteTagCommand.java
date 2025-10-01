package com.cleveronion.blog.application.tag.command;

import com.cleveronion.blog.domain.article.valueobject.TagId;

/**
 * 删除标签命令
 * 不可变对象，封装删除标签所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class DeleteTagCommand {
    
    private final TagId tagId;
    
    private DeleteTagCommand(TagId tagId) {
        this.tagId = validateTagId(tagId);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param tagId 标签ID
     * @return 删除标签命令
     */
    public static DeleteTagCommand of(TagId tagId) {
        return new DeleteTagCommand(tagId);
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
    
    // Getters only (no setters - immutable)
    
    public TagId getTagId() {
        return tagId;
    }
    
    @Override
    public String toString() {
        return "DeleteTagCommand{" +
                "tagId=" + tagId.getValue() +
                '}';
    }
}

