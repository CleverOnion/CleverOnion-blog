package com.cleveronion.blog.application.tag.command;

import com.cleveronion.blog.domain.article.aggregate.TagAggregate;

import java.util.List;

/**
 * 批量保存标签命令
 * 不可变对象，封装批量保存标签所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class SaveTagsCommand {
    
    private final List<TagAggregate> tags;
    
    private SaveTagsCommand(List<TagAggregate> tags) {
        this.tags = validateTags(tags);
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @param tags 标签聚合列表
     * @return 批量保存标签命令
     */
    public static SaveTagsCommand of(List<TagAggregate> tags) {
        return new SaveTagsCommand(tags);
    }
    
    /**
     * 验证标签列表
     */
    private List<TagAggregate> validateTags(List<TagAggregate> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("标签列表不能为空");
        }
        
        // 验证每个标签不为null
        if (tags.stream().anyMatch(tag -> tag == null)) {
            throw new IllegalArgumentException("标签列表中包含null元素");
        }
        
        return List.copyOf(tags); // 创建不可变副本
    }
    
    // Getters only (no setters - immutable)
    
    public List<TagAggregate> getTags() {
        return tags;
    }
    
    @Override
    public String toString() {
        return "SaveTagsCommand{" +
                "tagCount=" + tags.size() +
                '}';
    }
}

