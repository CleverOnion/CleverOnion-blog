package com.cleveronion.domain.shared.aggregate

import com.cleveronion.domain.shared.event.DomainEvent

/**
 * 聚合根抽象基类
 * 
 * 聚合根是聚合的唯一入口点，负责维护聚合内部的一致性和业务规则。
 * 聚合根管理领域事件的产生和发布。
 * 
 * @param T 聚合根标识符的类型
 */
abstract class AggregateRoot<T> {
    private val domainEvents = mutableListOf<DomainEvent>()
    
    /**
     * 获取聚合产生的领域事件
     * 
     * @return 领域事件列表的只读副本
     */
    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()
    
    /**
     * 清除已处理的领域事件
     * 
     * 通常在事件发布后调用此方法清理事件列表
     */
    fun clearDomainEvents() {
        domainEvents.clear()
    }
    
    /**
     * 添加领域事件
     * 
     * 当聚合内发生重要业务事件时，调用此方法记录事件
     * 
     * @param event 要添加的领域事件
     */
    protected fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
    
    /**
     * 聚合根必须实现业务规则验证
     * 
     * 此方法用于验证聚合的业务不变性，确保聚合始终处于有效状态
     */
    abstract fun ensureBusinessRules()
    
    /**
     * 获取聚合根标识符
     * 
     * @return 聚合根的唯一标识符
     */
    abstract fun getId(): T
    
    /**
     * 聚合根相等性比较基于标识符
     * 
     * 两个聚合根如果具有相同的标识符，则认为它们是相等的
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateRoot<*>) return false
        return getId() == other.getId()
    }
    
    /**
     * 基于标识符计算哈希码
     */
    override fun hashCode(): Int {
        return getId()?.hashCode() ?: 0
    }
    
    /**
     * 字符串表示形式
     */
    override fun toString(): String {
        return "${this::class.simpleName}(id=${getId()})"
    }
}