package com.cleveronion.blog.domain.article.event;

import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.domain.common.event.DomainEvent;

import java.util.Objects;

/**
 * 标签创建事件
 * 当标签被成功创建时发布此事件
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class TagCreatedEvent extends DomainEvent {
    
    private final TagId tagId;
    private final String tagName;
    
    public TagCreatedEvent(Object source, TagId tagId, String tagName) {
        super(source, String.valueOf(tagId.getValue()));
        this.tagId = Objects.requireNonNull(tagId, "标签ID不能为空");
        this.tagName = Objects.requireNonNull(tagName, "标签名称不能为空");
    }
    
    public TagId getTagId() {
        return tagId;
    }
    
    public String getTagName() {
        return tagName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagCreatedEvent that = (TagCreatedEvent) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "TagCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", tagId=" + tagId.getValue() +
                ", tagName='" + tagName + '\'' +
                '}';
    }
}

