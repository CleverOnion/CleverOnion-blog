package com.cleveronion.blog.infrastructure.common.event;

import com.cleveronion.blog.domain.common.event.DomainEvent;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 领域事件发布器实现
 * 使用Spring的ApplicationEventPublisher来发布领域事件
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
@Component
public class DomainEventPublisherImpl implements DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisherImpl.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public DomainEventPublisherImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            logger.warn("尝试发布null事件，已忽略");
            return;
        }
        
        logger.debug("发布领域事件: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
    
    /**
     * 批量发布领域事件列表
     * 
     * @param events 领域事件列表
     */
    public void publish(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        logger.debug("批量发布领域事件，数量: {}", events.size());
        events.forEach(this::publish);
    }
}


