package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.event.TagDeletedEvent;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.repository.TagRepository;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 标签应用服务
 * 负责标签相关的业务流程编排
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class TagApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TagApplicationService.class);
    
    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public TagApplicationService(TagRepository tagRepository, ArticleRepository articleRepository, ApplicationEventPublisher eventPublisher) {
        this.tagRepository = tagRepository;
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 创建新标签
     * 
     * @param name 标签名称
     * @return 创建的标签聚合
     */
    public TagAggregate createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        
        String trimmedName = name.trim();
        
        logger.debug("开始创建标签，名称: {}", trimmedName);
        
        // 检查标签名称是否已存在
        if (tagRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("标签名称已存在: " + trimmedName);
        }
        
        // 创建标签
        TagAggregate tag = TagAggregate.create(trimmedName);
        
        // 保存标签
        TagAggregate savedTag = tagRepository.save(tag);
        
        logger.info("成功创建标签，标签ID: {}, 名称: {}", 
            savedTag.getId().getValue(), savedTag.getName());
        
        return savedTag;
    }
    
    /**
     * 更新标签名称
     * 
     * @param tagId 标签ID
     * @param newName 新的标签名称
     * @return 更新后的标签聚合
     */
    public TagAggregate updateTagName(TagId tagId, String newName) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        
        String trimmedName = newName.trim();
        
        logger.debug("开始更新标签名称，标签ID: {}, 新名称: {}", tagId.getValue(), trimmedName);
        
        // 查找标签
        Optional<TagAggregate> tagOpt = tagRepository.findById(tagId);
        if (tagOpt.isEmpty()) {
            throw new IllegalArgumentException("标签不存在");
        }
        
        TagAggregate tag = tagOpt.get();
        
        // 检查新名称是否与当前名称相同
        if (tag.hasName(trimmedName)) {
            logger.debug("标签名称未发生变化，跳过更新");
            return tag;
        }
        
        // 检查新名称是否已被其他标签使用
        Optional<TagAggregate> existingTag = tagRepository.findByName(trimmedName);
        if (existingTag.isPresent() && !existingTag.get().getId().equals(tagId)) {
            throw new IllegalArgumentException("标签名称已存在: " + trimmedName);
        }
        
        // 更新名称
        tag.updateName(trimmedName);
        
        // 保存标签
        TagAggregate savedTag = tagRepository.save(tag);
        
        logger.info("成功更新标签名称，标签ID: {}, 新名称: {}", 
            savedTag.getId().getValue(), savedTag.getName());
        
        return savedTag;
    }
    
    /**
     * 删除标签
     * 
     * @param tagId 标签ID
     */
    public void deleteTag(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        logger.debug("开始删除标签，标签ID: {}", tagId.getValue());
        
        // 检查标签是否存在
        Optional<TagAggregate> tagOpt = tagRepository.findById(tagId);
        if (tagOpt.isEmpty()) {
            throw new IllegalArgumentException("标签不存在");
        }
        
        TagAggregate tag = tagOpt.get();
        
        // 删除标签
        tagRepository.deleteById(tagId);
        
        // 发布标签删除事件，由事件处理器处理文章标签关联的删除
        TagDeletedEvent event = new TagDeletedEvent(tagId, tag.getName());
        eventPublisher.publishEvent(event);
        
        logger.info("成功删除标签，标签ID: {}, 名称: {}", 
            tagId.getValue(), tag.getName());
    }
    
    /**
     * 根据ID查找标签
     * 
     * @param tagId 标签ID
     * @return 标签聚合（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<TagAggregate> findById(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return tagRepository.findById(tagId);
    }
    
    /**
     * 根据名称查找标签
     * 
     * @param name 标签名称
     * @return 标签聚合（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<TagAggregate> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        
        return tagRepository.findByName(name.trim());
    }
    
    /**
     * 根据ID集合查找标签列表
     * 
     * @param tagIds 标签ID集合
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findByIds(Set<TagId> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        
        return tagRepository.findByIds(tagIds);
    }
    
    /**
     * 根据名称集合查找标签列表
     * 
     * @param names 标签名称集合
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findByNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("标签名称集合不能为空");
        }
        
        return tagRepository.findByNames(names);
    }
    
    /**
     * 查找所有标签
     * 
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findAllTags() {
        return tagRepository.findAll();
    }
    
    /**
     * 按名称排序查找所有标签
     * 
     * @param ascending 是否升序排列
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findAllTagsOrderByName(boolean ascending) {
        return tagRepository.findAllOrderByName(ascending);
    }
    
    /**
     * 按创建时间排序查找所有标签
     * 
     * @param ascending 是否升序排列
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findAllTagsOrderByCreatedAt(boolean ascending) {
        return tagRepository.findAllOrderByCreatedAt(ascending);
    }
    
    /**
     * 分页查询标签
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findTags(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return tagRepository.findAll(page, size);
    }
    
    /**
     * 根据名称关键词搜索标签
     * 
     * @param keyword 关键词
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        return tagRepository.findByNameContaining(keyword.trim());
    }
    
    /**
     * 根据名称前缀搜索标签
     * 
     * @param prefix 前缀
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> searchByNamePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索前缀不能为空");
        }
        
        return tagRepository.findByNameStartingWith(prefix.trim());
    }
    
    /**
     * 统计标签总数
     * 
     * @return 标签总数
     */
    @Transactional(readOnly = true)
    public long countTags() {
        return tagRepository.count();
    }
    
    /**
     * 查找最近创建的标签
     * 
     * @param limit 限制数量
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findRecentlyCreated(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        return tagRepository.findRecentlyCreated(limit);
    }
    
    /**
     * 查找热门标签
     * 
     * @param limit 限制数量
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findPopularTags(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        return tagRepository.findPopularTags(limit);
    }
    
    /**
     * 查找未使用的标签
     * 
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findUnusedTags() {
        return tagRepository.findUnusedTags();
    }
    
    /**
     * 检查标签是否存在
     * 
     * @param tagId 标签ID
     * @return 如果存在返回true，否则返回false
     */
    @Transactional(readOnly = true)
    public boolean existsById(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return tagRepository.existsById(tagId);
    }
    
    /**
     * 检查指定名称的标签是否存在
     * 
     * @param name 标签名称
     * @return 如果存在返回true，否则返回false
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        
        return tagRepository.existsByName(name.trim());
    }
    
    /**
     * 批量创建标签
     * 
     * @param names 标签名称列表
     * @return 创建的标签列表
     */
    public List<TagAggregate> createTags(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("标签名称列表不能为空");
        }
        
        logger.debug("开始批量创建标签，数量: {}", names.size());
        
        List<TagAggregate> createdTags = names.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .distinct() // 去重
            .filter(name -> !tagRepository.existsByName(name)) // 过滤已存在的标签
            .map(name -> {
                TagAggregate tag = TagAggregate.create(name);
                return tagRepository.save(tag);
            })
            .toList();
        
        logger.info("成功批量创建标签，实际创建数量: {}", createdTags.size());
        
        return createdTags;
    }
    
    /**
     * 批量保存标签
     * 
     * @param tags 标签列表
     * @return 保存后的标签列表
     */
    public List<TagAggregate> saveTags(List<TagAggregate> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("标签列表不能为空");
        }
        
        logger.debug("开始批量保存标签，数量: {}", tags.size());
        
        List<TagAggregate> savedTags = tagRepository.saveAll(tags);
        
        logger.info("成功批量保存标签，数量: {}", savedTags.size());
        
        return savedTags;
    }
    
    /**
     * 批量删除标签
     * 
     * @param tagIds 标签ID集合
     */
    public void deleteTags(Set<TagId> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        
        logger.debug("开始批量删除标签，数量: {}", tagIds.size());
        
        // 先获取所有要删除的标签信息，用于发布事件
        List<TagAggregate> tagsToDelete = tagRepository.findByIds(tagIds);
        
        // 删除标签
        tagRepository.deleteByIds(tagIds);
        
        // 为每个删除的标签发布删除事件
        for (TagAggregate tag : tagsToDelete) {
            TagDeletedEvent event = new TagDeletedEvent(tag.getId(), tag.getName());
            eventPublisher.publishEvent(event);
        }
        
        logger.info("成功批量删除标签，数量: {}", tagsToDelete.size());
    }
    
    /**
     * 清理未使用的标签
     * 
     * @return 清理的标签数量
     */
    public int cleanupUnusedTags() {
        logger.debug("开始清理未使用的标签");
        
        List<TagAggregate> unusedTags = tagRepository.findUnusedTags();
        
        if (unusedTags.isEmpty()) {
            logger.info("没有找到未使用的标签");
            return 0;
        }
        
        Set<TagId> unusedTagIds = unusedTags.stream()
            .map(TagAggregate::getId)
            .collect(java.util.stream.Collectors.toSet());
        
        tagRepository.deleteByIds(unusedTagIds);
        
        logger.info("成功清理未使用的标签，数量: {}", unusedTags.size());
        
        return unusedTags.size();
    }
    
    /**
     * 获取标签使用统计信息
     * 
     * @param tagId 标签ID
     * @return 使用该标签的文章数量
     */
    @Transactional(readOnly = true)
    public long getTagUsageCount(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        List<ArticleAggregate> articlesUsingTag = articleRepository.findByTagId(tagId);
        return articlesUsingTag.size();
    }
    
    /**
     * 分页查询标签及其文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 标签及文章数量列表
     */
    @Transactional(readOnly = true)
    public List<TagRepository.TagWithArticleCount> findTagsWithArticleCount(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return tagRepository.findTagsWithArticleCount(page, size);
    }
    
    /**
     * 查找短标签（名称长度小于等于指定长度）
     * 
     * @param maxLength 最大长度
     * @return 短标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> findShortTags(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("最大长度必须大于0");
        }
        
        return tagRepository.findAll().stream()
            .filter(tag -> tag.isShortTag(maxLength))
            .toList();
    }
    
    /**
     * 根据关键词过滤标签（使用领域模型的业务方法）
     * 
     * @param keyword 关键词
     * @return 包含关键词的标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> filterByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        
        return tagRepository.findAll().stream()
            .filter(tag -> tag.containsKeyword(keyword))
            .toList();
    }
    
    /**
     * 根据标签名称查找或创建标签
     * 如果标签不存在，则自动创建
     * 
     * @param names 标签名称集合
     * @return 标签聚合列表
     */
    public List<TagAggregate> findOrCreateByNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        
        logger.debug("开始查找或创建标签，标签名称: {}", names);
        
        // 过滤并清理标签名称
        Set<String> cleanNames = names.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .collect(java.util.stream.Collectors.toSet());
        
        if (cleanNames.isEmpty()) {
            return List.of();
        }
        
        // 查找已存在的标签
        List<TagAggregate> existingTags = tagRepository.findByNames(cleanNames);
        Set<String> existingTagNames = existingTags.stream()
            .map(TagAggregate::getName)
            .collect(java.util.stream.Collectors.toSet());
        
        // 找出需要创建的标签名称
        Set<String> namesToCreate = cleanNames.stream()
            .filter(name -> !existingTagNames.contains(name))
            .collect(java.util.stream.Collectors.toSet());
        
        // 创建不存在的标签
        List<TagAggregate> newTags = namesToCreate.stream()
            .map(name -> {
                TagAggregate tag = TagAggregate.create(name);
                return tagRepository.save(tag);
            })
            .toList();
        
        // 合并已存在的标签和新创建的标签
        List<TagAggregate> allTags = new java.util.ArrayList<>(existingTags);
        allTags.addAll(newTags);
        
        logger.info("成功查找或创建标签，总数: {}, 已存在: {}, 新创建: {}", 
            allTags.size(), existingTags.size(), newTags.size());
        
        return allTags;
    }
    
    /**
     * 根据前缀过滤标签（使用领域模型的业务方法）
     * 
     * @param prefix 前缀
     * @return 以指定前缀开始的标签列表
     */
    @Transactional(readOnly = true)
    public List<TagAggregate> filterByPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("前缀不能为空");
        }
        
        return tagRepository.findAll().stream()
            .filter(tag -> tag.startsWithPrefix(prefix))
            .toList();
    }
}