package com.cleveronion.blog.application.tag.service;

import com.cleveronion.blog.application.tag.command.*;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.event.TagCreatedEvent;
import com.cleveronion.blog.domain.article.event.TagDeletedEvent;
import com.cleveronion.blog.domain.article.event.TagUpdatedEvent;
import com.cleveronion.blog.domain.article.repository.TagRepository;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签命令服务
 * 负责处理所有修改标签状态的操作
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class TagCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(TagCommandService.class);
    
    private final TagRepository tagRepository;
    private final DomainEventPublisher eventPublisher;
    
    public TagCommandService(TagRepository tagRepository, DomainEventPublisher eventPublisher) {
        this.tagRepository = tagRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 创建标签
     * 
     * @param command 创建标签命令
     * @return 创建的标签聚合
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public TagAggregate createTag(CreateTagCommand command) {
        logger.debug("执行创建标签命令: {}", command);
        
        // 检查标签名称是否已存在
        if (tagRepository.existsByName(command.getName())) {
            throw new IllegalArgumentException("标签名称已存在: " + command.getName());
        }
        
        // 创建标签聚合
        TagAggregate tag = TagAggregate.create(command.getName());
        
        // 保存标签
        TagAggregate saved = tagRepository.save(tag);
        
        // 发布领域事件
        eventPublisher.publish(new TagCreatedEvent(
            this,
            saved.getId(),
            saved.getName()
        ));
        
        logger.info("成功创建标签，标签ID: {}, 名称: {}", 
            saved.getId().getValue(), saved.getName());
        
        return saved;
    }
    
    /**
     * 更新标签名称
     * 
     * @param command 更新标签命令
     * @return 更新后的标签聚合
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public TagAggregate updateTagName(UpdateTagCommand command) {
        logger.debug("执行更新标签命令: {}", command);
        
        TagId tagId = command.getTagId();
        
        // 查找标签
        TagAggregate tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("标签不存在"));
        
        String oldName = tag.getName();
        
        // 检查新名称是否与当前名称相同
        if (tag.hasName(command.getNewName())) {
            logger.debug("标签名称未发生变化，跳过更新");
            return tag;
        }
        
        // 检查新名称是否已被其他标签使用
        Optional<TagAggregate> existingTag = tagRepository.findByName(command.getNewName());
        if (existingTag.isPresent() && !existingTag.get().getId().equals(tagId)) {
            throw new IllegalArgumentException("标签名称已存在: " + command.getNewName());
        }
        
        // 更新名称
        tag.updateName(command.getNewName());
        
        // 保存标签
        TagAggregate saved = tagRepository.save(tag);
        
        // 发布领域事件
        eventPublisher.publish(new TagUpdatedEvent(
            this,
            saved.getId(),
            oldName,
            saved.getName()
        ));
        
        logger.info("成功更新标签，标签ID: {}, 新名称: {}", 
            saved.getId().getValue(), saved.getName());
        
        return saved;
    }
    
    /**
     * 删除标签
     * 
     * @param command 删除标签命令
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public void deleteTag(DeleteTagCommand command) {
        logger.debug("执行删除标签命令: {}", command);
        
        TagId tagId = command.getTagId();
        
        // 检查标签是否存在
        TagAggregate tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("标签不存在"));
        
        // 删除标签
        tagRepository.deleteById(tagId);
        
        // 发布领域事件
        eventPublisher.publish(new TagDeletedEvent(
            this,
            tagId,
            tag.getName()
        ));
        
        logger.info("成功删除标签，标签ID: {}, 名称: {}", 
            tagId.getValue(), tag.getName());
    }
    
    /**
     * 批量创建标签
     * 
     * @param command 批量创建标签命令
     * @return 创建的标签列表
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public List<TagAggregate> createTags(CreateTagsCommand command) {
        logger.debug("开始批量创建标签，数量: {}", command.getNames().size());
        
        List<TagAggregate> createdTags = command.getNames().stream()
            .filter(name -> !tagRepository.existsByName(name)) // 过滤已存在的标签
            .map(name -> {
                TagAggregate tag = TagAggregate.create(name);
                TagAggregate saved = tagRepository.save(tag);
                
                // 发布事件
                eventPublisher.publish(new TagCreatedEvent(
                    this,
                    saved.getId(),
                    saved.getName()
                ));
                
                return saved;
            })
            .toList();
        
        logger.info("成功批量创建标签，实际创建数量: {}", createdTags.size());
        
        return createdTags;
    }
    
    /**
     * 批量保存标签
     * 
     * @param command 批量保存标签命令
     * @return 保存后的标签列表
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public List<TagAggregate> saveTags(SaveTagsCommand command) {
        logger.debug("开始批量保存标签，数量: {}", command.getTags().size());
        
        List<TagAggregate> savedTags = tagRepository.saveAll(command.getTags());
        
        logger.info("成功批量保存标签，数量: {}", savedTags.size());
        
        return savedTags;
    }
    
    /**
     * 批量删除标签
     * 
     * @param command 批量删除标签命令
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public void deleteTags(DeleteTagsCommand command) {
        logger.debug("开始批量删除标签，数量: {}", command.getTagIds().size());
        
        // 先获取所有要删除的标签信息，用于发布事件
        List<TagAggregate> tagsToDelete = tagRepository.findByIds(command.getTagIds());
        
        // 删除标签
        tagRepository.deleteByIds(command.getTagIds());
        
        // 为每个删除的标签发布删除事件
        for (TagAggregate tag : tagsToDelete) {
            eventPublisher.publish(new TagDeletedEvent(
                this,
                tag.getId(),
                tag.getName()
            ));
        }
        
        logger.info("成功批量删除标签，数量: {}", tagsToDelete.size());
    }
    
    /**
     * 清理未使用的标签
     * 
     * @param command 清理未使用标签命令
     * @return 清理的标签数量
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public int cleanupUnusedTags(CleanupUnusedTagsCommand command) {
        logger.debug("开始清理未使用的标签");
        
        List<TagAggregate> unusedTags = tagRepository.findUnusedTags();
        
        if (unusedTags.isEmpty()) {
            logger.info("没有找到未使用的标签");
            return 0;
        }
        
        Set<TagId> unusedTagIds = unusedTags.stream()
            .map(TagAggregate::getId)
            .collect(Collectors.toSet());
        
        // 删除标签
        tagRepository.deleteByIds(unusedTagIds);
        
        // 发布删除事件
        for (TagAggregate tag : unusedTags) {
            eventPublisher.publish(new TagDeletedEvent(
                this,
                tag.getId(),
                tag.getName()
            ));
        }
        
        logger.info("成功清理未使用的标签，数量: {}", unusedTags.size());
        
        return unusedTags.size();
    }
    
    /**
     * 根据标签名称查找或创建标签
     * 如果标签不存在，则自动创建
     * 
     * @param command 查找或创建标签命令
     * @return 标签聚合列表
     */
    @CacheEvict(value = {"tags", "tag-lists", "tag-stats"}, allEntries = true)
    public List<TagAggregate> findOrCreateByNames(FindOrCreateTagsCommand command) {
        logger.debug("开始查找或创建标签，标签名称: {}", command.getNames());
        
        Set<String> names = command.getNames();
        
        // 查找已存在的标签
        List<TagAggregate> existingTags = tagRepository.findByNames(names);
        Set<String> existingTagNames = existingTags.stream()
            .map(TagAggregate::getName)
            .collect(Collectors.toSet());
        
        // 找出需要创建的标签名称
        Set<String> namesToCreate = names.stream()
            .filter(name -> !existingTagNames.contains(name))
            .collect(Collectors.toSet());
        
        // 创建不存在的标签
        List<TagAggregate> newTags = namesToCreate.stream()
            .map(name -> {
                TagAggregate tag = TagAggregate.create(name);
                TagAggregate saved = tagRepository.save(tag);
                
                // 发布创建事件
                eventPublisher.publish(new TagCreatedEvent(
                    this,
                    saved.getId(),
                    saved.getName()
                ));
                
                return saved;
            })
            .toList();
        
        // 合并已存在的标签和新创建的标签
        List<TagAggregate> allTags = new ArrayList<>(existingTags);
        allTags.addAll(newTags);
        
        logger.info("成功查找或创建标签，总数: {}, 已存在: {}, 新创建: {}", 
            allTags.size(), existingTags.size(), newTags.size());
        
        return allTags;
    }
}

