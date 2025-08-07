package com.cleveronion.application.shared.port

import com.cleveronion.domain.shared.event.DomainEvent

/**
 * 事件发布端口
 * 
 * 定义了发布领域事件的接口，由基础设施层实现具体的事件发布机制
 */
interface EventPublisherPort {
    /**
     * 发布单个领域事件
     * 
     * @param event 要发布的领域事件
     */
    suspend fun publish(event: DomainEvent)
    
    /**
     * 批量发布领域事件
     * 
     * @param events 要发布的领域事件列表
     */
    suspend fun publishAll(events: List<DomainEvent>)
}