package com.cleveronion.blog.application.tag.command;

import com.cleveronion.blog.domain.article.valueobject.TagId;

import java.util.Set;

/**
 * 批量删除标签命令
 * 不可变对象，封装批量删除标签所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class DeleteTagsCommand {
    
    private final Set<TagId> tagIds;
    
    private DeleteTagsCommand(Set<TagId> tagIds) {
        this.tagIds = validateTagIds(tagIds);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param tagIds 标签ID集合
     * @return 批量删除标签命令
     */
    public static DeleteTagsCommand of(Set<TagId> tagIds) {
        return new DeleteTagsCommand(tagIds);
    }
    
    /**
     * 验证标签ID集合
     */
    private Set<TagId> validateTagIds(Set<TagId> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        
        // 验证每个ID不为null
        if (tagIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("标签ID集合中包含null元素");
        }
        
        return Set.copyOf(tagIds); // 创建不可变副本
    }
    
    // Getters only (no setters - immutable)
    
    public Set<TagId> getTagIds() {
        return tagIds;
    }
    
    @Override
    public String toString() {
        return "DeleteTagsCommand{" +
                "tagIdCount=" + tagIds.size() +
                '}';
    }
}

