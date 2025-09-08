package com.cleveronion.blog.infrastructure.article.persistence.repository;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.valueobject.*;
import com.cleveronion.blog.infrastructure.article.persistence.converter.ArticleConverter;
import com.cleveronion.blog.infrastructure.article.persistence.po.ArticlePO;
import com.cleveronion.blog.infrastructure.article.persistence.po.ArticleTagPO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * 文章仓储实现类
 * 实现ArticleRepository接口，桥接领域层和基础设施层
 * 
 * @author CleverOnion
 */
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    
    private final ArticleJpaRepository articleJpaRepository;
    private final ArticleTagJpaRepository articleTagJpaRepository;
    
    public ArticleRepositoryImpl(ArticleJpaRepository articleJpaRepository,
                                ArticleTagJpaRepository articleTagJpaRepository) {
        this.articleJpaRepository = articleJpaRepository;
        this.articleTagJpaRepository = articleTagJpaRepository;
    }
    
    @Override
    @Transactional
    public ArticleAggregate save(ArticleAggregate article) {
        // 转换为PO并保存
        ArticlePO articlePO = ArticleConverter.toArticlePO(article);
        ArticlePO savedArticlePO = articleJpaRepository.save(articlePO);
        
        // 处理标签关联
        if (article.getTagIds() != null && !article.getTagIds().isEmpty()) {
            // 如果是更新操作，先删除旧的关联
            if (savedArticlePO.getId() != null) {
                articleTagJpaRepository.deleteByArticleId(savedArticlePO.getId());
            }
            
            // 创建新的关联
            List<ArticleTagPO> articleTagPOs = ArticleConverter.toArticleTagPOs(
                savedArticlePO.getId(), article.getTagIds());
            articleTagJpaRepository.saveAll(articleTagPOs);
        }
        
        // 转换回聚合并返回
        return ArticleConverter.toArticleAggregate(savedArticlePO, article.getTagIds());
    }
    
    @Override
    public Optional<ArticleAggregate> findById(ArticleId id) {
        Optional<ArticlePO> articlePOOpt = articleJpaRepository.findById(Long.valueOf(id.getValue()));
        if (articlePOOpt.isEmpty()) {
            return Optional.empty();
        }
        
        ArticlePO articlePO = articlePOOpt.get();
        // 获取标签ID集合
        List<Long> tagIdValues = articleTagJpaRepository.findTagIdsByArticleId(articlePO.getId());
        Set<TagId> tagIds = ArticleConverter.toTagIdSet(tagIdValues);
        
        return Optional.of(ArticleConverter.toArticleAggregate(articlePO, tagIds));
    }
    
    @Override
    @Transactional
    public void deleteById(ArticleId id) {
        // 先删除标签关联
        articleTagJpaRepository.deleteByArticleId(Long.valueOf(id.getValue()));
        // 再删除文章
        articleJpaRepository.deleteById(Long.valueOf(id.getValue()));
    }
    
    @Override
    public boolean existsById(ArticleId id) {
        return articleJpaRepository.existsById(Long.valueOf(id.getValue()));
    }
    
    @Override
    public List<ArticleAggregate> findByAuthorId(AuthorId authorId) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByAuthorId(authorId.getValue());
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByCategoryId(CategoryId categoryId) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByCategoryId(categoryId.getValue());
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByTagId(TagId tagId) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByTagId(tagId.getValue());
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByStatus(ArticleStatus status) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByStatus(status.name());
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByAuthorIdAndStatus(AuthorId authorId, ArticleStatus status) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByAuthorIdAndStatus(
            authorId.getValue(), status.name());
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByCategoryIdAndStatus(CategoryId categoryId, ArticleStatus status) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByCategoryIdAndStatus(
            categoryId.getValue(), status.name());
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByTitleContaining(String keyword) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByTitleContainingIgnoreCase(keyword);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByContentContaining(String keyword) {
        List<ArticlePO> articlePOs = articleJpaRepository.findByContentContainingIgnoreCase(keyword);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用优化查询，不查询content字段
        List<ArticlePO> articlePOs = articleJpaRepository.findByStatusOrderByCreatedAtDescOptimized(
            ArticleStatus.PUBLISHED.name(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByAuthorId(AuthorId authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用优化查询，不查询content字段
        List<ArticlePO> articlePOs = articleJpaRepository.findByAuthorIdOrderByCreatedAtDesc(
            authorId.getValue(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findPublishedByCategoryId(CategoryId categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用优化查询，不查询content字段
        List<ArticlePO> articlePOs = articleJpaRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(
            categoryId.getValue(), ArticleStatus.PUBLISHED.name(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public long countByAuthorId(AuthorId authorId) {
        return articleJpaRepository.countByAuthorId(authorId.getValue());
    }
    
    @Override
    public long countByCategoryId(CategoryId categoryId) {
        return articleJpaRepository.countByCategoryId(categoryId.getValue());
    }
    
    @Override
    public long countByStatus(ArticleStatus status) {
        return articleJpaRepository.countByStatus(status.name());
    }
    
    @Override
    public List<ArticleAggregate> findPublishedByTagId(TagId tagId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用优化查询，不查询content字段
        List<ArticlePO> articlePOs = articleJpaRepository.findByTagIdAndStatusOrderByCreatedAtDesc(
            Long.valueOf(tagId.getValue()), ArticleStatus.PUBLISHED.name(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public long countByTagId(TagId tagId) {
        return articleJpaRepository.countByTagId(tagId.getValue());
    }
    
    @Override
    public long countPublishedArticles() {
        return articleJpaRepository.countByStatus(ArticleStatus.PUBLISHED.name());
    }
    
    @Override
    public List<ArticleAggregate> findAllArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用优化查询，不查询content字段
        List<ArticlePO> articlePOs = articleJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByStatus(ArticleStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用优化查询，不查询content字段
        List<ArticlePO> articlePOs = articleJpaRepository.findByStatusOrderByCreatedAtDesc(
            status.name(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByCategoryId(CategoryId categoryId, ArticleStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ArticlePO> articlePOs;
        if (status != null) {
            articlePOs = articleJpaRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(
                categoryId.getValue(), status.name(), pageable);
        } else {
            articlePOs = articleJpaRepository.findByCategoryIdOrderByCreatedAtDesc(
                categoryId.getValue(), pageable);
        }
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findByTagId(TagId tagId, ArticleStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ArticlePO> articlePOs;
        if (status != null) {
            articlePOs = articleJpaRepository.findByTagIdAndStatusOrderByCreatedAtDesc(
                Long.valueOf(tagId.getValue()), status.name(), pageable);
        } else {
            articlePOs = articleJpaRepository.findByTagIdOrderByCreatedAtDesc(
                Long.valueOf(tagId.getValue()), pageable);
        }
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public long countAllArticles() {
        return articleJpaRepository.count();
    }
    
    @Override
    public long countByCategoryId(CategoryId categoryId, ArticleStatus status) {
        if (status != null) {
            return articleJpaRepository.countByCategoryIdAndStatus(categoryId.getValue(), status.name());
        } else {
            return articleJpaRepository.countByCategoryId(categoryId.getValue());
        }
    }
    
    @Override
    public long countByTagId(TagId tagId, ArticleStatus status) {
        if (status != null) {
            return articleJpaRepository.countByTagIdAndStatus(tagId.getValue(), status.name());
        } else {
            return articleJpaRepository.countByTagId(tagId.getValue());
        }
    }
    
    @Override
    public List<ArticleAggregate> findPublishedByCategoryAndTag(CategoryId categoryId, TagId tagId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ArticlePO> articlePOs = articleJpaRepository.findByCategoryIdAndTagIdAndStatusOrderByCreatedAtDesc(
            categoryId.getValue(), tagId.getValue(), ArticleStatus.PUBLISHED.name(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public long countByCategoryAndTag(CategoryId categoryId, TagId tagId) {
        return articleJpaRepository.countByCategoryIdAndTagId(categoryId.getValue(), tagId.getValue());
    }
    
    @Override
    public List<ArticleAggregate> findRecentlyPublished(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<ArticlePO> articlePOs = articleJpaRepository.findRecentlyPublished(
            ArticleStatus.PUBLISHED.name(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public List<ArticleAggregate> findPopularArticles(int limit) {
        // 这里简单实现为最近发布的文章，实际可以根据阅读量、点赞数等指标排序
        return findRecentlyPublished(limit);
    }
    
    @Override
    public List<ArticleAggregate> findByCategoryAndTagAndStatus(CategoryId categoryId, TagId tagId, ArticleStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ArticlePO> articlePOs = articleJpaRepository.findByCategoryIdAndTagIdAndStatusOrderByCreatedAtDesc(
            categoryId.getValue(), tagId.getValue(), status.name(), pageable);
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public long countByCategoryAndTagAndStatus(CategoryId categoryId, TagId tagId, ArticleStatus status) {
        return articleJpaRepository.countByCategoryIdAndTagIdAndStatus(categoryId.getValue(), tagId.getValue(), status);
    }
    
    @Override
    public List<ArticleAggregate> findByCategoryAndTag(CategoryId categoryId, TagId tagId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ArticlePO> articlePOs = articleJpaRepository.findByCategoryIdAndTagIdOrderByCreatedAtDesc(
            categoryId.getValue(), 
            tagId.getValue(), 
            pageable
        );
        
        return convertToAggregates(articlePOs);
    }
    
    @Override
    public long countByCategoryAndTagAllStatuses(CategoryId categoryId, TagId tagId) {
        return articleJpaRepository.countByCategoryIdAndTagIdAllStatuses(
            categoryId.getValue(), 
            tagId.getValue()
        );
    }
    
    /**
     * 将ArticlePO列表转换为ArticleAggregate列表
     * 优化版本：使用批量查询避免N+1问题
     * 
     * @param articlePOs 文章PO列表
     * @return 文章聚合列表
     */
    private List<ArticleAggregate> convertToAggregates(List<ArticlePO> articlePOs) {
        if (articlePOs.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 提取所有文章ID
        List<Long> articleIds = articlePOs.stream()
            .map(ArticlePO::getId)
            .collect(Collectors.toList());
        
        // 批量查询所有文章的标签关联信息
        List<ArticleTagPO> articleTagPOs = articleTagJpaRepository.findByArticleIdIn(articleIds);
        
        // 按文章ID分组标签ID
        Map<Long, List<Long>> articleTagMap = articleTagPOs.stream()
            .collect(Collectors.groupingBy(
                ArticleTagPO::getArticleId,
                Collectors.mapping(ArticleTagPO::getTagId, Collectors.toList())
            ));
        
        // 转换为聚合对象
        return articlePOs.stream()
            .map(articlePO -> {
                List<Long> tagIdValues = articleTagMap.getOrDefault(articlePO.getId(), Collections.emptyList());
                Set<TagId> tagIds = ArticleConverter.toTagIdSet(tagIdValues);
                return ArticleConverter.toArticleAggregate(articlePO, tagIds);
            })
            .collect(Collectors.toList());
    }
}