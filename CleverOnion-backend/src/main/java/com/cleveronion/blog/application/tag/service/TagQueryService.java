package com.cleveronion.blog.application.tag.service;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.repository.TagRepository;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 标签查询服务
 * 负责处理所有标签查询操作，配置缓存优化性能
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class TagQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(TagQueryService.class);
    
    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;
    
    public TagQueryService(TagRepository tagRepository, ArticleRepository articleRepository) {
        this.tagRepository = tagRepository;
        this.articleRepository = articleRepository;
    }
    
    // ========== 基础查询方法（6个）==========
    
    /**
     * 根据ID查找标签
     * 
     * @param tagId 标签ID
     * @return 标签聚合（如果存在）
     */
    public Optional<TagAggregate> findById(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        logger.debug("查询标签，ID: {}", tagId.getValue());
        return tagRepository.findById(tagId);
    }
    
    /**
     * 根据ID集合查找标签列表
     * 
     * @param tagIds 标签ID集合
     * @return 标签列表
     */
    public List<TagAggregate> findByIds(Set<TagId> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        
        logger.debug("批量查询标签，ID数量: {}", tagIds.size());
        return tagRepository.findByIds(tagIds);
    }
    
    /**
     * 根据名称查找标签
     * 
     * @param name 标签名称
     * @return 标签聚合（如果存在）
     */
    public Optional<TagAggregate> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        
        logger.debug("根据名称查询标签: {}", name);
        return tagRepository.findByName(name.trim());
    }
    
    /**
     * 根据名称集合查找标签列表
     * 
     * @param names 标签名称集合
     * @return 标签列表
     */
    public List<TagAggregate> findByNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("标签名称集合不能为空");
        }
        
        logger.debug("批量根据名称查询标签，名称数量: {}", names.size());
        return tagRepository.findByNames(names);
    }
    
    /**
     * 查找所有标签
     * 
     * @return 标签列表
     */
    public List<TagAggregate> findAll() {
        logger.debug("查询所有标签");
        return tagRepository.findAll();
    }
    
    /**
     * 分页查询标签
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 标签列表
     */
    public List<TagAggregate> findWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        logger.debug("分页查询标签，页码: {}, 每页大小: {}", page, size);
        return tagRepository.findAll(page, size);
    }
    
    // ========== 排序查询方法（2个）==========
    
    /**
     * 按名称排序查找所有标签
     * 
     * @param ascending 是否升序排列
     * @return 标签列表
     */
    public List<TagAggregate> findAllOrderByName(boolean ascending) {
        logger.debug("按名称排序查询所有标签，升序: {}", ascending);
        return tagRepository.findAllOrderByName(ascending);
    }
    
    /**
     * 按创建时间排序查找所有标签
     * 
     * @param ascending 是否升序排列
     * @return 标签列表
     */
    public List<TagAggregate> findAllOrderByCreatedAt(boolean ascending) {
        logger.debug("按创建时间排序查询所有标签，升序: {}", ascending);
        return tagRepository.findAllOrderByCreatedAt(ascending);
    }
    
    // ========== 搜索查询方法（2个）==========
    
    /**
     * 根据名称关键词搜索标签
     * 
     * @param keyword 关键词
     * @return 标签列表
     */
    public List<TagAggregate> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        logger.debug("搜索标签，关键词: {}", keyword);
        return tagRepository.findByNameContaining(keyword.trim());
    }
    
    /**
     * 根据名称前缀搜索标签
     * 
     * @param prefix 前缀
     * @return 标签列表
     */
    public List<TagAggregate> searchByNamePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索前缀不能为空");
        }
        
        logger.debug("按前缀搜索标签: {}", prefix);
        return tagRepository.findByNameStartingWith(prefix.trim());
    }
    
    // ========== 统计查询方法（3个）==========
    
    /**
     * 统计标签总数
     * 
     * @return 标签总数
     */
    public long countAll() {
        logger.debug("统计标签总数");
        return tagRepository.count();
    }
    
    /**
     * 检查标签是否存在
     * 
     * @param tagId 标签ID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsById(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        logger.debug("检查标签是否存在，ID: {}", tagId.getValue());
        return tagRepository.existsById(tagId);
    }
    
    /**
     * 检查指定名称的标签是否存在
     * 
     * @param name 标签名称
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        
        logger.debug("检查标签名称是否存在: {}", name);
        return tagRepository.existsByName(name.trim());
    }
    
    // ========== 特殊查询方法（4个）==========
    
    /**
     * 查找最近创建的标签
     * 
     * @param limit 限制数量
     * @return 标签列表
     */
    public List<TagAggregate> findRecentlyCreated(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        logger.debug("查询最近创建的标签，限制数量: {}", limit);
        return tagRepository.findRecentlyCreated(limit);
    }
    
    /**
     * 查找热门标签
     * 
     * @param limit 限制数量
     * @return 标签列表
     */
    public List<TagAggregate> findPopularTags(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        logger.debug("查询热门标签，限制数量: {}", limit);
        return tagRepository.findPopularTags(limit);
    }
    
    /**
     * 查找未使用的标签
     * 
     * @return 标签列表
     */
    public List<TagAggregate> findUnusedTags() {
        logger.debug("查询未使用的标签");
        return tagRepository.findUnusedTags();
    }
    
    /**
     * 分页查询标签及其文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 标签及文章数量列表
     */
    public List<TagRepository.TagWithArticleCount> findWithArticleCount(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        logger.debug("分页查询标签及文章数量，页码: {}, 每页大小: {}", page, size);
        return tagRepository.findTagsWithArticleCount(page, size);
    }
    
    // ========== 业务查询方法（4个）==========
    
    /**
     * 获取标签使用统计信息
     * 
     * @param tagId 标签ID
     * @return 使用该标签的文章数量
     */
    public long getTagUsageCount(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        logger.debug("统计标签使用情况，ID: {}", tagId.getValue());
        List<ArticleAggregate> articlesUsingTag = articleRepository.findByTagId(tagId);
        return articlesUsingTag.size();
    }
    
    /**
     * 查找短标签（名称长度小于等于指定长度）
     * 
     * @param maxLength 最大长度
     * @return 短标签列表
     */
    public List<TagAggregate> findShortTags(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("最大长度必须大于0");
        }
        
        logger.debug("查询短标签，最大长度: {}", maxLength);
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
    public List<TagAggregate> filterByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        
        logger.debug("按关键词过滤标签: {}", keyword);
        return tagRepository.findAll().stream()
            .filter(tag -> tag.containsKeyword(keyword))
            .toList();
    }
    
    /**
     * 根据前缀过滤标签（使用领域模型的业务方法）
     * 
     * @param prefix 前缀
     * @return 以指定前缀开始的标签列表
     */
    public List<TagAggregate> filterByPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("前缀不能为空");
        }
        
        logger.debug("按前缀过滤标签: {}", prefix);
        return tagRepository.findAll().stream()
            .filter(tag -> tag.startsWithPrefix(prefix))
            .toList();
    }
}

