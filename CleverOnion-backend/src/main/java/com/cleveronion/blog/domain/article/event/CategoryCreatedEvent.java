package com.cleveronion.blog.domain.article.event;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.common.event.DomainEvent;

import java.util.Objects;

/**
 * 分类创建事件
 * 当分类被成功创建时发布此事件
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CategoryCreatedEvent extends DomainEvent {
    
    private final CategoryId categoryId;
    private final String categoryName;
    
    public CategoryCreatedEvent(Object source, CategoryId categoryId, String categoryName) {
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
        CategoryCreatedEvent that = (CategoryCreatedEvent) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "CategoryCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", categoryId=" + categoryId.getValue() +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}

