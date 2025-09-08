package com.cleveronion.blog.domain.common.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 * 继承Spring的ApplicationEvent，支持Spring Boot事件发布机制
 * 定义所有领域事件的通用属性和行为
 */
public abstract class DomainEvent extends ApplicationEvent {
    
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String aggregateId;
    private final int version;
    
    /**
     * 构造函数
     * 
     * @param source 事件源对象
     * @param aggregateId 聚合根ID
     */
    protected DomainEvent(Object source, String aggregateId) {
        this(source, aggregateId, 1);
    }
    
    /**
     * 构造函数
     * 
     * @param source 事件源对象
     * @param aggregateId 聚合根ID
     * @param version 事件版本
     */
    protected DomainEvent(Object source, String aggregateId, int version) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.version = version;
    }
    
    /**
     * 获取事件ID
     * 
     * @return 事件的唯一标识
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * 获取事件发生时间
     * 
     * @return 事件发生的时间戳
     */
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    /**
     * 获取事件类型
     * 
     * @return 事件类型名称
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取聚合根ID
     * 
     * @return 触发事件的聚合根标识
     */
    public String getAggregateId() {
        return aggregateId;
    }
    
    /**
     * 获取事件版本
     * 
     * @return 事件版本号
     */
    public int getVersion() {
        return version;
    }
    
    @Override
    public String toString() {
        return "DomainEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + getEventType() + '\'' +
                ", occurredOn=" + occurredOn +
                ", aggregateId='" + aggregateId + '\'' +
                ", version=" + version +
                '}';
    }
}