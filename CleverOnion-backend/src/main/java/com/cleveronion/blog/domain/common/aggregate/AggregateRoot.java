package com.cleveronion.blog.domain.common.aggregate;

import com.cleveronion.blog.domain.common.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 提供领域事件管理功能
 */
public abstract class AggregateRoot {
    
    @JsonIgnore
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * 添加领域事件
     * 
     * @param event 要添加的领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        if (event != null) {
            this.domainEvents.add(event);
        }
    }
    
    /**
     * 获取所有领域事件（只读）
     * 
     * @return 领域事件列表的只读副本
     */
    @JsonIgnore
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * 清除所有领域事件
     * 通常在事件发布后调用
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    /**
     * 检查是否有待发布的领域事件
     * 
     * @return true如果有待发布的事件
     */
    @JsonIgnore
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }
    
    /**
     * 获取聚合根标识
     * 子类必须实现此方法
     * 
     * @return 聚合根的唯一标识
     */
    public abstract String getAggregateId();
}