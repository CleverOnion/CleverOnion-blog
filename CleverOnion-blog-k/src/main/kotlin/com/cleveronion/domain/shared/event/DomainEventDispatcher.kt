package com.cleveronion.domain.shared.event

/**
 * 领域事件分发器
 * 
 * 负责将领域事件分发给相应的事件处理器
 */
class DomainEventDispatcher {
    private val handlers = mutableMapOf<Class<out DomainEvent>, MutableList<DomainEventHandler<out DomainEvent>>>()
    
    /**
     * 注册事件处理器
     * 
     * @param handler 要注册的事件处理器
     */
    fun <T : DomainEvent> registerHandler(handler: DomainEventHandler<T>) {
        val eventType = handler.getEventType()
        handlers.computeIfAbsent(eventType) { mutableListOf() }.add(handler)
    }
    
    /**
     * 分发事件到所有注册的处理器
     * 
     * @param event 要分发的领域事件
     */
    suspend fun dispatch(event: DomainEvent) {
        val eventHandlers = handlers[event::class.java] ?: return
        
        for (handler in eventHandlers) {
            try {
                @Suppress("UNCHECKED_CAST")
                (handler as DomainEventHandler<DomainEvent>).handle(event)
            } catch (e: Exception) {
                // 记录错误但不中断其他处理器的执行
                println("Error handling event ${event::class.simpleName}: ${e.message}")
            }
        }
    }
    
    /**
     * 批量分发事件
     * 
     * @param events 要分发的领域事件列表
     */
    suspend fun dispatchAll(events: List<DomainEvent>) {
        for (event in events) {
            dispatch(event)
        }
    }
    
    /**
     * 清除所有注册的处理器
     */
    fun clearHandlers() {
        handlers.clear()
    }
    
    /**
     * 获取指定事件类型的处理器数量
     * 
     * @param eventType 事件类型
     * @return 处理器数量
     */
    fun getHandlerCount(eventType: Class<out DomainEvent>): Int {
        return handlers[eventType]?.size ?: 0
    }
}