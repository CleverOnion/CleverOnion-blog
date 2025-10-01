package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.application.article.command.CreateArticleDraftCommand;
import com.cleveronion.blog.application.article.command.PublishArticleCommand;
import com.cleveronion.blog.application.article.command.UpdateArticleCommand;
import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.article.valueobject.AuthorId;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * 文章命令服务
 * 负责处理所有改变文章状态的操作（写操作）
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
@Service
@Transactional
public class ArticleCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleCommandService.class);
    
    private final ArticleRepository articleRepository;
    private final DomainEventPublisher eventPublisher;
    
    public ArticleCommandService(
            ArticleRepository articleRepository,
            DomainEventPublisher eventPublisher) {
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 创建文章草稿
     * 
     * @param command 创建文章草稿命令
     * @return 创建的文章聚合
     */
    @CacheEvict(value = "article-lists", allEntries = true)
    public ArticleAggregate createDraft(CreateArticleDraftCommand command) {
        logger.debug("执行创建文章草稿命令: authorId={}, categoryId={}, title={}", 
            command.getAuthorId().getValue(), 
            command.getCategoryId().getValue(),
            command.getContent().getTitle());
        
        // 创建聚合
        ArticleAggregate article = ArticleAggregate.createDraft(
            command.getContent(),
            command.getCategoryId(),
            command.getAuthorId()
        );
        
        // 添加标签
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            command.getTagIds().forEach(article::addTag);
        }
        
        // 保存
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        // 发布领域事件
        publishDomainEvents(article);
        
        logger.info("文章草稿创建成功: articleId={}, title={}", 
            savedArticle.getId().getValue(), 
            savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 创建并发布文章
     * 
     * @param command 发布文章命令
     * @return 发布的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate createAndPublish(PublishArticleCommand command) {
        logger.debug("执行创建并发布文章命令: authorId={}, title={}", 
            command.getAuthorId().getValue(),
            command.getContent().getTitle());
        
        // 创建草稿
        ArticleAggregate article = ArticleAggregate.createDraft(
            command.getContent(),
            command.getCategoryId(),
            command.getAuthorId()
        );
        
        // 添加标签
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            command.getTagIds().forEach(article::addTag);
        }
        
        // 立即发布
        article.publish();
        
        // 保存
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        // 发布领域事件
        publishDomainEvents(article);
        
        logger.info("文章创建并发布成功: articleId={}, title={}", 
            savedArticle.getId().getValue(),
            savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 更新文章内容
     * 
     * @param command 更新文章命令
     * @return 更新后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate updateContent(UpdateArticleCommand command) {
        logger.debug("执行更新文章命令: articleId={}", command.getArticleId().getValue());
        
        // 查找文章
        ArticleAggregate article = findArticleOrThrow(command.getArticleId());
        
        // 验证权限
        checkAuthorPermission(article, command.getAuthorId());
        
        // 更新内容
        article.updateContent(command.getNewContent());
        
        // 更新分类（如果提供）
        if (command.getNewCategoryId() != null) {
            article.updateCategory(command.getNewCategoryId());
        }
        
        // 更新标签（如果提供）
        if (command.getNewTagIds() != null) {
            article.clearTags();
            command.getNewTagIds().forEach(article::addTag);
        }
        
        // 保存
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        // 发布领域事件
        publishDomainEvents(article);
        
        logger.info("文章更新成功: articleId={}", savedArticle.getId().getValue());
        
        return savedArticle;
    }
    
    /**
     * 添加标签到文章
     * 
     * @param articleId 文章ID
     * @param tagIds 标签ID集合
     * @param authorId 作者ID
     * @return 更新后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate addTags(ArticleId articleId, Set<TagId> tagIds, AuthorId authorId) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        
        logger.debug("为文章添加标签: articleId={}, tagCount={}", 
            articleId.getValue(), tagIds.size());
        
        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);
        
        tagIds.forEach(article::addTag);
        
        ArticleAggregate savedArticle = articleRepository.save(article);
        publishDomainEvents(article);
        
        logger.info("成功为文章添加标签: articleId={}, tagCount={}", 
            savedArticle.getId().getValue(), savedArticle.getTagCount());
        
        return savedArticle;
    }
    
    /**
     * 从文章移除标签
     * 
     * @param articleId 文章ID
     * @param tagIds 标签ID集合
     * @param authorId 作者ID
     * @return 更新后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate removeTags(ArticleId articleId, Set<TagId> tagIds, AuthorId authorId) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        
        logger.debug("移除文章标签: articleId={}, tagCount={}", 
            articleId.getValue(), tagIds.size());
        
        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);
        
        tagIds.forEach(article::removeTag);
        
        ArticleAggregate savedArticle = articleRepository.save(article);
        publishDomainEvents(article);
        
        logger.info("成功移除文章标签: articleId={}, remainingTagCount={}", 
            savedArticle.getId().getValue(), savedArticle.getTagCount());
        
        return savedArticle;
    }
    
    /**
     * 发布文章
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID
     * @return 发布后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate publish(ArticleId articleId, AuthorId authorId) {
        logger.debug("执行发布文章命令: articleId={}", articleId.getValue());
        
        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);
        
        article.publish();
        
        ArticleAggregate savedArticle = articleRepository.save(article);
        publishDomainEvents(article);
        
        logger.info("文章发布成功: articleId={}", savedArticle.getId().getValue());
        
        return savedArticle;
    }
    
    /**
     * 归档文章
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID
     * @return 归档后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate archive(ArticleId articleId, AuthorId authorId) {
        logger.debug("执行归档文章命令: articleId={}", articleId.getValue());
        
        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);
        
        article.archive();
        
        ArticleAggregate savedArticle = articleRepository.save(article);
        publishDomainEvents(article);
        
        logger.info("文章归档成功: articleId={}", savedArticle.getId().getValue());
        
        return savedArticle;
    }
    
    /**
     * 撤回文章到草稿状态
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID
     * @return 撤回后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate revertToDraft(ArticleId articleId, AuthorId authorId) {
        logger.debug("执行撤回文章到草稿命令: articleId={}", articleId.getValue());
        
        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);
        
        article.revertToDraft();
        
        ArticleAggregate savedArticle = articleRepository.save(article);
        publishDomainEvents(article);
        
        logger.info("文章撤回到草稿成功: articleId={}", savedArticle.getId().getValue());
        
        return savedArticle;
    }
    
    /**
     * 删除文章
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public void delete(ArticleId articleId, AuthorId authorId) {
        logger.debug("执行删除文章命令: articleId={}", articleId.getValue());
        
        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);
        
        articleRepository.deleteById(articleId);
        
        logger.info("文章删除成功: articleId={}", articleId.getValue());
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 查找文章，不存在则抛出异常
     */
    private ArticleAggregate findArticleOrThrow(ArticleId articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId.getValue()));
    }
    
    /**
     * 检查作者权限
     */
    private void checkAuthorPermission(ArticleAggregate article, AuthorId authorId) {
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能执行此操作");
        }
    }
    
    /**
     * 发布聚合的领域事件
     */
    private void publishDomainEvents(ArticleAggregate article) {
        if (article.hasDomainEvents()) {
            article.getDomainEvents().forEach(eventPublisher::publish);
            article.clearDomainEvents();
        }
    }
}


