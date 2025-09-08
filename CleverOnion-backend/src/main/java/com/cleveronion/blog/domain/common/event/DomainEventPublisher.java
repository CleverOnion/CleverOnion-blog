package com.cleveronion.blog.domain.common.event;

/**
 * 领域事件发布器接口
 * 用于在领域层发布事件，保持领域层的纯净性
 * 具体实现由基础设施层提供
 */
public interface DomainEventPublisher {
    
    /**
     * 发布领域事件
     * 
     * @param event 要发布的领域事件
     */
    void publish(DomainEvent event);
    
    /**
     * 批量发布领域事件
     * 
     * @param events 要发布的领域事件数组
     */
    default void publishAll(DomainEvent... events) {
        for (DomainEvent event : events) {
            publish(event);
        }
    }
}