package com.cleveronion.blog.domain.article.event;

import com.cleveronion.blog.domain.article.valueobject.TagId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 标签删除领域事件
 * 当标签被删除时发布此事件，用于通知其他组件进行相关的清理工作
 * 
 * @author CleverOnion
 */
public class TagDeletedEvent {
    
    /**
     * 被删除的标签ID
     */
    private final TagId tagId;
    
    /**
     * 标签名称
     */
    private final String tagName;
    
    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredAt;
    
    /**
     * 构造函数
     * 
     * @param tagId 被删除的标签ID
     * @param tagName 标签名称
     */
    public TagDeletedEvent(TagId tagId, String tagName) {
        this.tagId = Objects.requireNonNull(tagId, "标签ID不能为空");
        this.tagName = Objects.requireNonNull(tagName, "标签名称不能为空");
        this.occurredAt = LocalDateTime.now();
    }
    
    /**
     * 获取被删除的标签ID
     * 
     * @return 标签ID
     */
    public TagId getTagId() {
        return tagId;
    }
    
    /**
     * 获取标签名称
     * 
     * @return 标签名称
     */
    public String getTagName() {
        return tagName;
    }
    
    /**
     * 获取事件发生时间
     * 
     * @return 事件发生时间
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagDeletedEvent that = (TagDeletedEvent) o;
        return Objects.equals(tagId, that.tagId) &&
               Objects.equals(tagName, that.tagName) &&
               Objects.equals(occurredAt, that.occurredAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tagId, tagName, occurredAt);
    }
    
    @Override
    public String toString() {
        return "TagDeletedEvent{" +
                "tagId=" + tagId +
                ", tagName='" + tagName + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}