package com.cleveronion.blog.domain.article.event;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.common.event.DomainEvent;

import java.util.Objects;

/**
 * 分类删除事件
 * 当分类被删除时发布此事件
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CategoryDeletedEvent extends DomainEvent {
    
    private final CategoryId categoryId;
    private final String categoryName;
    
    public CategoryDeletedEvent(Object source, CategoryId categoryId, String categoryName) {
        super(source, String.valueOf(categoryId.getValue()));
        this.categoryId = Objects.requireNonNull(categoryId, "分类ID不能为空");
        this.categoryName = Objects.requireNonNull(categoryName, "分类名称不能为空");
    }
    
    public CategoryId getCategoryId() {
        return categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryDeletedEvent that = (CategoryDeletedEvent) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "CategoryDeletedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", categoryId=" + categoryId.getValue() +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}

