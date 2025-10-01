package com.cleveronion.blog.domain.article.event;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.common.event.DomainEvent;

import java.util.Objects;

/**
 * 分类更新事件
 * 当分类信息被更新时发布此事件
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CategoryUpdatedEvent extends DomainEvent {
    
    private final CategoryId categoryId;
    private final String oldName;
    private final String newName;
    
    public CategoryUpdatedEvent(Object source, CategoryId categoryId, String oldName, String newName) {
        super(source, String.valueOf(categoryId.getValue()));
        this.categoryId = Objects.requireNonNull(categoryId, "分类ID不能为空");
        this.oldName = oldName;
        this.newName = Objects.requireNonNull(newName, "新名称不能为空");
    }
    
    public CategoryId getCategoryId() {
        return categoryId;
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
        CategoryUpdatedEvent that = (CategoryUpdatedEvent) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "CategoryUpdatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", categoryId=" + categoryId.getValue() +
                ", oldName='" + oldName + '\'' +
                ", newName='" + newName + '\'' +
                '}';
    }
}

