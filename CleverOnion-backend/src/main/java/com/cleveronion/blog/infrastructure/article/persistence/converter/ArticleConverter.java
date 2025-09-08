package com.cleveronion.blog.infrastructure.article.persistence.converter;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.valueobject.*;
import com.cleveronion.blog.infrastructure.article.persistence.po.ArticlePO;
import com.cleveronion.blog.infrastructure.article.persistence.po.ArticleTagPO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章聚合与持久化对象转换器
 * 负责ArticleAggregate和ArticlePO之间的相互转换
 * 
 * @author CleverOnion
 */
public class ArticleConverter {
    
    /**
     * 将ArticleAggregate转换为ArticlePO
     * 
     * @param articleAggregate 文章聚合
     * @return 文章持久化对象
     */
    public static ArticlePO toArticlePO(ArticleAggregate articleAggregate) {
        if (articleAggregate == null) {
            return null;
        }
        
        ArticlePO articlePO = new ArticlePO(
            articleAggregate.getContent().getTitle(),
            articleAggregate.getContent().getSummary(),
            articleAggregate.getContent().getContent(),
            articleAggregate.getStatus().name(),
            articleAggregate.getCategoryId() != null ? articleAggregate.getCategoryId().getValue() : null,
            articleAggregate.getAuthorId().getValue()
        );
        
        // 如果聚合有ID，说明是已存在的文章（更新操作）
        if (articleAggregate.getId() != null) {
            articlePO.setId(Long.valueOf(articleAggregate.getId().getValue()));
        }
        // 新创建的文章ID为null，不设置ID，让数据库自动生成
        
        // 时间戳由基础设施层的@CreationTimestamp和@UpdateTimestamp注解自动管理
        
        return articlePO;
    }
    
    /**
     * 将ArticlePO转换为ArticleAggregate
     * 
     * @param articlePO 文章持久化对象
     * @param tagIds 标签ID集合
     * @return 文章聚合
     */
    public static ArticleAggregate toArticleAggregate(ArticlePO articlePO, Set<TagId> tagIds) {
        if (articlePO == null) {
            return null;
        }
        
        // 构建文章内容值对象
        ArticleContent content = new ArticleContent(
            articlePO.getTitle(),
            articlePO.getContent(),
            articlePO.getSummary()
        );
        
        // 构建分类ID值对象
        CategoryId categoryId = articlePO.getCategoryId() != null ? 
            CategoryId.of(articlePO.getCategoryId()) : null;
        
        // 使用rebuild方法重建聚合
        ArticleAggregate articleAggregate = ArticleAggregate.rebuild(
            ArticleId.of(articlePO.getId().toString()),
            content,
            ArticleStatus.valueOf(articlePO.getStatus()),
            categoryId,
            AuthorId.of(articlePO.getAuthorId()),
            tagIds,
            articlePO.getCreatedAt() // 使用createdAt作为发布时间
        );
        
        return articleAggregate;
    }
    
    /**
     * 更新ArticlePO的字段（用于更新操作）
     * 
     * @param articlePO 要更新的持久化对象
     * @param articleAggregate 包含新数据的聚合
     */
    public static void updateArticlePO(ArticlePO articlePO, ArticleAggregate articleAggregate) {
        if (articlePO == null || articleAggregate == null) {
            return;
        }
        
        articlePO.setTitle(articleAggregate.getContent().getTitle());
        articlePO.setSummary(articleAggregate.getContent().getSummary());
        articlePO.setContent(articleAggregate.getContent().getContent());
        articlePO.setStatus(articleAggregate.getStatus().name());
        articlePO.setCategoryId(articleAggregate.getCategoryId() != null ? 
            articleAggregate.getCategoryId().getValue() : null);
        // updatedAt会由@UpdateTimestamp自动更新
    }
    
    /**
     * 将标签ID集合转换为ArticleTagPO列表
     * 
     * @param articleId 文章ID
     * @param tagIds 标签ID集合
     * @return ArticleTagPO列表
     */
    public static List<ArticleTagPO> toArticleTagPOs(Long articleId, Set<TagId> tagIds) {
        if (articleId == null || tagIds == null) {
            return List.of();
        }
        
        return tagIds.stream()
            .map(tagId -> new ArticleTagPO(articleId, tagId.getValue()))
            .collect(Collectors.toList());
    }
    
    /**
     * 将Long类型的标签ID列表转换为TagId集合
     * 
     * @param tagIdValues Long类型的标签ID列表
     * @return TagId集合
     */
    public static Set<TagId> toTagIdSet(List<Long> tagIdValues) {
        if (tagIdValues == null) {
            return Set.of();
        }
        
        return tagIdValues.stream()
            .map(TagId::of)
            .collect(Collectors.toSet());
    }
}