package com.cleveronion.domain.shared.event

/**
 * 领域事件处理器接口
 * 
 * 定义了处理特定类型领域事件的接口
 * 
 * @param T 要处理的领域事件类型
 */
interface DomainEventHandler<T : DomainEvent> {
    /**
     * 处理领域事件
     * 
     * @param event 要处理的领域事件
     */
    suspend fun handle(event: T)
    
    /**
     * 获取此处理器能够处理的事件类型
     * 
     * @return 事件类型的Class对象
     */
    fun getEventType(): Class<T>
}