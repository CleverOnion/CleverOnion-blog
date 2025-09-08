package com.cleveronion.blog.application.article.eventhandler;

import com.cleveronion.blog.domain.article.event.TagDeletedEvent;
import com.cleveronion.blog.infrastructure.article.persistence.repository.ArticleTagJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 标签删除事件处理器
 * 处理标签删除后的相关清理工作，主要是删除文章与标签的关联关系
 * 
 * @author CleverOnion
 */
@Component
public class TagDeletedEventHandler {
    
    private final ArticleTagJpaRepository articleTagJpaRepository;
    private final Logger logger = LoggerFactory.getLogger(TagDeletedEventHandler.class);
    
    public TagDeletedEventHandler(ArticleTagJpaRepository articleTagJpaRepository) {
        this.articleTagJpaRepository = articleTagJpaRepository;
    }
    
    /**
     * 处理标签删除事件
     * 删除所有与该标签相关的文章标签关联记录
     * 
     * @param event 标签删除事件
     */
    @EventListener
    @Transactional
    public void handleTagDeletedEvent(TagDeletedEvent event) {
        logger.info("开始处理标签删除事件，标签ID: {}, 标签名称: {}", 
            event.getTagId().getValue(), event.getTagName());
        
        try {
            // 删除所有与该标签相关的文章标签关联记录
            long tagIdValue = event.getTagId().getValue();
            
            // 先统计要删除的关联记录数量
            long associationCount = articleTagJpaRepository.countByTagId(tagIdValue);
            
            if (associationCount > 0) {
                // 删除所有关联记录
                articleTagJpaRepository.deleteByTagId(tagIdValue);
                logger.info("成功删除标签关联记录，标签ID: {}, 删除关联数量: {}", 
                    tagIdValue, associationCount);
            } else {
                logger.info("标签没有关联的文章，无需删除关联记录，标签ID: {}", tagIdValue);
            }
            
        } catch (Exception e) {
            logger.error("处理标签删除事件失败，标签ID: {}, 错误信息: {}", 
                event.getTagId().getValue(), e.getMessage(), e);
            throw new RuntimeException("处理标签删除事件失败: " + e.getMessage(), e);
        }
    }
}