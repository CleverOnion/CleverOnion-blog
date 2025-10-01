package com.cleveronion.blog.domain.article.event;

import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.domain.common.event.DomainEvent;

import java.util.Objects;

/**
 * 标签更新事件
 * 当标签信息被更新时发布此事件
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class TagUpdatedEvent extends DomainEvent {
    
    private final TagId tagId;
    private final String oldName;
    private final String newName;
    
    public TagUpdatedEvent(Object source, TagId tagId, String oldName, String newName) {
        super(source, String.valueOf(tagId.getValue()));
        this.tagId = Objects.requireNonNull(tagId, "标签ID不能为空");
        this.oldName = oldName;
        this.newName = Objects.requireNonNull(newName, "新名称不能为空");
    }
    
    public TagId getTagId() {
        return tagId;
    }
    
    public String getOldName() {
        return oldName;
    }
    
    public String getNewName() {
        return newName;
    }
    
    public boolean isNameChanged() {
        return !Objects.equals(oldName, newName);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagUpdatedEvent that = (TagUpdatedEvent) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "TagUpdatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", tagId=" + tagId.getValue() +
                ", oldName='" + oldName + '\'' +
                ", newName='" + newName + '\'' +
                '}';
    }
}

