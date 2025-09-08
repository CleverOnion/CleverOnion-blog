package com.cleveronion.blog.domain.common;

/**
 * 领域事件接口
 * 所有领域事件都应该实现此接口
 * 领域事件用于表示领域中发生的重要业务事件
 * 
 * @author CleverOnion
 */
public interface DomainEvent {
    
    /**
     * 获取事件发生的时间戳
     * 
     * @return 事件发生时间（毫秒时间戳）
     */
    long occurredOn();
    
    /**
     * 获取事件类型
     * 
     * @return 事件类型名称
     */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}