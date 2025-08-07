package com.cleveronion.domain.shared.event

import java.time.Instant

/**
 * 领域事件基接口
 * 
 * 领域事件用于表示在领域中发生的重要业务事件，
 * 这些事件可以被其他聚合或外部系统订阅和处理。
 */
interface DomainEvent {
    /**
     * 事件发生的时间
     */
    val occurredOn: Instant
    
    /**
     * 产生事件的聚合根标识符
     */
    val aggregateId: String
}