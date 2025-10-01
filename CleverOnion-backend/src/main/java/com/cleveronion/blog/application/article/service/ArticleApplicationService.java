package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.valueobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 文章应用服务
 * 负责文章相关的业务流程编排
 * 
 * @author CleverOnion
 * @since 1.0.0
 * @deprecated 此服务已被CQRS架构替代，请使用：
 *             命令操作 → {@link ArticleCommandService}
 *             查询操作 → {@link ArticleQueryService}
 *             预计在 v2.1.0 版本移除
 */
@Deprecated
@Service
@Transactional
public class ArticleApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleApplicationService.class);
    
    private final ArticleRepository articleRepository;
    
    public ArticleApplicationService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }
    
    /**
     * 创建文章草稿
     * 
     * @param content 文章内容
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 创建的文章聚合
     */
    public ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
        if (content == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始创建文章草稿，作者ID: {}, 分类ID: {}, 标题: {}", 
            authorId.getValue(), categoryId.getValue(), content.getTitle());
        
        // 创建文章草稿
        ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功创建文章草稿，文章ID: {}, 标题: {}", 
            savedArticle.getId().getValue(), savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 更新文章内容
     * 
     * @param articleId 文章ID
     * @param newContent 新的文章内容
     * @param authorId 作者ID（用于权限验证）
     * @return 更新后的文章聚合
     */
    public ArticleAggregate updateContent(ArticleId articleId, ArticleContent newContent, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (newContent == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始更新文章内容，文章ID: {}, 作者ID: {}", articleId.getValue(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能更新文章内容");
        }
        
        // 更新内容
        article.updateContent(newContent);
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功更新文章内容，文章ID: {}, 标题: {}", 
            savedArticle.getId().getValue(), savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 直接创建并发布文章
     * 
     * @param content 文章内容
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 发布的文章聚合
     */
    public ArticleAggregate createAndPublishArticle(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
        if (content == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始创建并发布文章，作者ID: {}, 分类ID: {}, 标题: {}", 
            authorId.getValue(), categoryId.getValue(), content.getTitle());
        
        // 创建文章草稿
        ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);
        
        // 立即发布
        article.publish();
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功创建并发布文章，文章ID: {}, 标题: {}", 
            savedArticle.getId().getValue(), savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 发布文章
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID（用于权限验证）
     * @return 发布后的文章聚合
     */
    public ArticleAggregate publishArticle(ArticleId articleId, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始发布文章，文章ID: {}, 作者ID: {}", articleId.getValue(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能发布文章");
        }
        
        // 发布文章
        article.publish();
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功发布文章，文章ID: {}, 标题: {}", 
            savedArticle.getId().getValue(), savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 归档文章
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID（用于权限验证）
     * @return 归档后的文章聚合
     */
    public ArticleAggregate archiveArticle(ArticleId articleId, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始归档文章，文章ID: {}, 作者ID: {}", articleId.getValue(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能归档文章");
        }
        
        // 归档文章
        article.archive();
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功归档文章，文章ID: {}, 标题: {}", 
            savedArticle.getId().getValue(), savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 撤回文章到草稿状态
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID（用于权限验证）
     * @return 撤回后的文章聚合
     */
    public ArticleAggregate revertToDraft(ArticleId articleId, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始撤回文章到草稿状态，文章ID: {}, 作者ID: {}", articleId.getValue(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能撤回文章");
        }
        
        // 撤回到草稿状态
        article.revertToDraft();
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功撤回文章到草稿状态，文章ID: {}, 标题: {}", 
            savedArticle.getId().getValue(), savedArticle.getContent().getTitle());
        
        return savedArticle;
    }
    
    /**
     * 更新文章分类
     * 
     * @param articleId 文章ID
     * @param categoryId 新的分类ID
     * @param authorId 作者ID（用于权限验证）
     * @return 更新后的文章聚合
     */
    public ArticleAggregate updateCategory(ArticleId articleId, CategoryId categoryId, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始更新文章分类，文章ID: {}, 新分类ID: {}, 作者ID: {}", 
            articleId.getValue(), categoryId.getValue(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能更新文章分类");
        }
        
        // 更新分类
        article.updateCategory(categoryId);
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功更新文章分类，文章ID: {}, 新分类ID: {}", 
            savedArticle.getId().getValue(), categoryId.getValue());
        
        return savedArticle;
    }
    
    /**
     * 为文章添加标签
     * 
     * @param articleId 文章ID
     * @param tagIds 标签ID集合
     * @param authorId 作者ID（用于权限验证）
     * @return 更新后的文章聚合
     */
    public ArticleAggregate addTags(ArticleId articleId, Set<TagId> tagIds, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始为文章添加标签，文章ID: {}, 标签数量: {}, 作者ID: {}", 
            articleId.getValue(), tagIds.size(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能为文章添加标签");
        }
        
        // 添加标签
        for (TagId tagId : tagIds) {
            article.addTag(tagId);
        }
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功为文章添加标签，文章ID: {}, 当前标签数量: {}", 
            savedArticle.getId().getValue(), savedArticle.getTagCount());
        
        return savedArticle;
    }
    
    /**
     * 移除文章标签
     * 
     * @param articleId 文章ID
     * @param tagIds 要移除的标签ID集合
     * @param authorId 作者ID（用于权限验证）
     * @return 更新后的文章聚合
     */
    public ArticleAggregate removeTags(ArticleId articleId, Set<TagId> tagIds, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("标签ID集合不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始移除文章标签，文章ID: {}, 要移除的标签数量: {}, 作者ID: {}", 
            articleId.getValue(), tagIds.size(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能移除文章标签");
        }
        
        // 移除标签
        for (TagId tagId : tagIds) {
            article.removeTag(tagId);
        }
        
        // 保存文章
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        logger.info("成功移除文章标签，文章ID: {}, 当前标签数量: {}", 
            savedArticle.getId().getValue(), savedArticle.getTagCount());
        
        return savedArticle;
    }
    
    /**
     * 删除文章
     * 
     * @param articleId 文章ID
     * @param authorId 作者ID（用于权限验证）
     */
    public void deleteArticle(ArticleId articleId, AuthorId authorId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("开始删除文章，文章ID: {}, 作者ID: {}", articleId.getValue(), authorId.getValue());
        
        // 查找文章
        Optional<ArticleAggregate> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 验证作者权限
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能删除文章");
        }
        
        // 删除文章
        articleRepository.deleteById(articleId);
        
        logger.info("成功删除文章，文章ID: {}, 标题: {}", 
            articleId.getValue(), article.getContent().getTitle());
    }
    
    /**
     * 根据ID查找文章
     * 
     * @param articleId 文章ID
     * @return 文章聚合（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        return articleRepository.findById(articleId);
    }
    
    /**
     * 查找指定作者的所有文章
     * 
     * @param authorId 作者ID
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByAuthorId(AuthorId authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        return articleRepository.findByAuthorId(authorId);
    }
    
    /**
     * 查找指定分类的文章
     * 
     * @param categoryId 分类ID
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByCategoryId(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return articleRepository.findByCategoryId(categoryId);
    }
    
    /**
     * 查找包含指定标签的文章
     * 
     * @param tagId 标签ID
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByTagId(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return articleRepository.findByTagId(tagId);
    }
    
    /**
     * 查找指定状态的文章
     * 
     * @param status 文章状态
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByStatus(ArticleStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }
        
        return articleRepository.findByStatus(status);
    }
    
    /**
     * 分页查找已发布的文章
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findPublishedArticles(page, size);
    }
    
    /**
     * 分页查找指定作者的文章
     * 
     * @param authorId 作者ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByAuthorId(AuthorId authorId, int page, int size) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findByAuthorId(authorId, page, size);
    }
    
    /**
     * 根据标题关键词搜索文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> searchByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        return articleRepository.findByTitleContaining(keyword.trim());
    }
    
    /**
     * 根据内容关键词搜索文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> searchByContent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        return articleRepository.findByContentContaining(keyword.trim());
    }
    
    /**
     * 统计指定作者的文章数量
     * 
     * @param authorId 作者ID
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByAuthorId(AuthorId authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        return articleRepository.countByAuthorId(authorId);
    }
    
    /**
     * 统计指定分类的文章数量
     * 
     * @param categoryId 分类ID
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByCategoryId(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return articleRepository.countByCategoryId(categoryId);
    }
    
    /**
     * 分页查找指定分类的已发布文章
     * 
     * @param categoryId 分类ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findPublishedByCategoryId(CategoryId categoryId, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findPublishedByCategoryId(categoryId, page, size);
    }
    
    /**
     * 分页查找指定标签的已发布文章
     * 
     * @param tagId 标签ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findPublishedByTagId(TagId tagId, int page, int size) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findPublishedByTagId(tagId, page, size);
    }
    
    /**
     * 统计指定标签的文章数量
     * 
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByTagId(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return articleRepository.countByTagId(tagId);
    }
    
    /**
     * 统计已发布文章的总数量
     * 
     * @return 已发布文章数量
     */
    @Transactional(readOnly = true)
    public long countPublishedArticles() {
        return articleRepository.countPublishedArticles();
    }
    
    /**
     * 同时按分类和标签分页查找已发布文章
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findPublishedByCategoryAndTag(CategoryId categoryId, TagId tagId, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findPublishedByCategoryAndTag(categoryId, tagId, page, size);
    }
    
    /**
     * 统计同时属于指定分类和标签的已发布文章数量
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByCategoryAndTag(CategoryId categoryId, TagId tagId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return articleRepository.countByCategoryAndTag(categoryId, tagId);
    }
    
    /**
     * 同时按分类和标签分页查找文章（所有状态）
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByCategoryAndTag(CategoryId categoryId, TagId tagId, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findByCategoryAndTag(categoryId, tagId, page, size);
    }
    
    /**
     * 统计同时属于指定分类和标签的文章数量（所有状态）
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByCategoryAndTagAllStatuses(CategoryId categoryId, TagId tagId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return articleRepository.countByCategoryAndTagAllStatuses(categoryId, tagId);
    }
    
    /**
     * 查找最近发布的文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findRecentlyPublished(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        return articleRepository.findRecentlyPublished(limit);
    }
    
    /**
     * 查找热门文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findPopularArticles(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        return articleRepository.findPopularArticles(limit);
    }
    
    /**
     * 分页查找所有文章（不限制状态）
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findAllArticles(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findAllArticles(page, size);
    }
    
    /**
     * 分页查找指定状态的文章
     * 
     * @param status 文章状态
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByStatus(ArticleStatus status, int page, int size) {
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findByStatus(status, page, size);
    }
    
    /**
     * 分页查找指定分类的文章（支持状态筛选）
     * 
     * @param categoryId 分类ID
     * @param status 文章状态（可选，为null时查询所有状态）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByCategoryId(CategoryId categoryId, ArticleStatus status, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findByCategoryId(categoryId, status, page, size);
    }
    
    /**
     * 分页查找指定标签的文章（支持状态筛选）
     * 
     * @param tagId 标签ID
     * @param status 文章状态（可选，为null时查询所有状态）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByTagId(TagId tagId, ArticleStatus status, int page, int size) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findByTagId(tagId, status, page, size);
    }
    
    /**
     * 统计所有文章数量
     * 
     * @return 文章总数量
     */
    @Transactional(readOnly = true)
    public long countAllArticles() {
        return articleRepository.countAllArticles();
    }
    
    /**
     * 统计指定状态的文章数量
     * 
     * @param status 文章状态
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByStatus(ArticleStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }
        
        return articleRepository.countByStatus(status);
    }
    
    /**
     * 统计指定分类的文章数量（支持状态筛选）
     * 
     * @param categoryId 分类ID
     * @param status 文章状态（可选，为null时统计所有状态）
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByCategoryId(CategoryId categoryId, ArticleStatus status) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return articleRepository.countByCategoryId(categoryId, status);
    }
    
    /**
     * 统计指定标签的文章数量（支持状态筛选）
     * 
     * @param tagId 标签ID
     * @param status 文章状态（可选，为null时统计所有状态）
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByTagId(TagId tagId, ArticleStatus status) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return articleRepository.countByTagId(tagId, status);
    }
    
    /**
     * 同时按分类、标签和状态分页查找文章
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param status 文章状态
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @Transactional(readOnly = true)
    public List<ArticleAggregate> findByCategoryAndTagAndStatus(CategoryId categoryId, TagId tagId, ArticleStatus status, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        return articleRepository.findByCategoryAndTagAndStatus(categoryId, tagId, status, page, size);
    }
    
    /**
     * 统计同时属于指定分类、标签和状态的文章数量
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param status 文章状态
     * @return 文章数量
     */
    @Transactional(readOnly = true)
    public long countByCategoryAndTagAndStatus(CategoryId categoryId, TagId tagId, ArticleStatus status) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }
        
        return articleRepository.countByCategoryAndTagAndStatus(categoryId, tagId, status);
    }
}