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

/**
 * 文章查询服务
 * 负责处理所有文章查询操作（读操作）
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleQueryService.class);
    
    private final ArticleRepository articleRepository;
    
    public ArticleQueryService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }
    
    // ========== 基础查询方法 ==========
    
    /**
     * 根据ID查询文章
     * 
     * @param articleId 文章ID
     * @return 文章聚合（如果存在）
     */
    // 注意：不在Service层缓存领域对象，建议在Controller层缓存DTO
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        logger.debug("查询文章: articleId={}", articleId.getValue());
        return articleRepository.findById(articleId);
    }
    
    /**
     * 查询指定作者的所有文章
     * 
     * @param authorId 作者ID
     * @return 文章列表
     */
    public List<ArticleAggregate> findByAuthorId(AuthorId authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        logger.debug("查询作者文章: authorId={}", authorId.getValue());
        return articleRepository.findByAuthorId(authorId);
    }
    
    /**
     * 查询指定分类的文章
     * 
     * @param categoryId 分类ID
     * @return 文章列表
     */
    public List<ArticleAggregate> findByCategoryId(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        logger.debug("查询分类文章: categoryId={}", categoryId.getValue());
        return articleRepository.findByCategoryId(categoryId);
    }
    
    /**
     * 查询包含指定标签的文章
     * 
     * @param tagId 标签ID
     * @return 文章列表
     */
    public List<ArticleAggregate> findByTagId(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        logger.debug("查询标签文章: tagId={}", tagId.getValue());
        return articleRepository.findByTagId(tagId);
    }
    
    // ========== 分页查询方法 ==========
    
    /**
     * 分页查找已发布的文章
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        validatePageParams(page, size);
        
        logger.debug("查询已发布文章列表: page={}, size={}", page, size);
        return articleRepository.findPublishedArticles(page, size);
    }
    
    /**
     * 分页查找所有文章
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    public List<ArticleAggregate> findAllArticles(int page, int size) {
        validatePageParams(page, size);
        
        logger.debug("查询所有文章: page={}, size={}", page, size);
        return articleRepository.findAllArticles(page, size);
    }
    
    /**
     * 分页查找指定作者的文章
     * 
     * @param authorId 作者ID
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    public List<ArticleAggregate> findByAuthorId(AuthorId authorId, int page, int size) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        validatePageParams(page, size);
        
        logger.debug("查询作者文章: authorId={}, page={}, size={}", authorId.getValue(), page, size);
        return articleRepository.findByAuthorId(authorId, page, size);
    }
    
    /**
     * 分页查找指定分类的已发布文章
     * 
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    public List<ArticleAggregate> findPublishedByCategoryId(CategoryId categoryId, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        validatePageParams(page, size);
        
        logger.debug("查询分类文章: categoryId={}, page={}, size={}", categoryId.getValue(), page, size);
        return articleRepository.findPublishedByCategoryId(categoryId, page, size);
    }
    
    /**
     * 分页查找指定标签的已发布文章
     * 
     * @param tagId 标签ID
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    public List<ArticleAggregate> findPublishedByTagId(TagId tagId, int page, int size) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        validatePageParams(page, size);
        
        logger.debug("查询标签文章: tagId={}, page={}, size={}", tagId.getValue(), page, size);
        return articleRepository.findPublishedByTagId(tagId, page, size);
    }
    
    /**
     * 分页查找指定状态的文章
     * 
     * @param status 文章状态
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    public List<ArticleAggregate> findByStatus(ArticleStatus status, int page, int size) {
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }
        validatePageParams(page, size);
        
        logger.debug("查询状态文章: status={}, page={}, size={}", status, page, size);
        return articleRepository.findByStatus(status, page, size);
    }
    
    /**
     * 分页查找指定分类和标签的已发布文章
     */
    public List<ArticleAggregate> findPublishedByCategoryAndTag(CategoryId categoryId, TagId tagId, int page, int size) {
        if (categoryId == null || tagId == null) {
            throw new IllegalArgumentException("分类ID和标签ID不能为空");
        }
        validatePageParams(page, size);
        
        return articleRepository.findPublishedByCategoryAndTag(categoryId, tagId, page, size);
    }
    
    // ========== 搜索方法 ==========
    
    /**
     * 根据标题关键词搜索文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    public List<ArticleAggregate> searchByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        logger.debug("搜索文章（标题）: keyword={}", keyword);
        return articleRepository.findByTitleContaining(keyword.trim());
    }
    
    /**
     * 根据内容关键词搜索文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    public List<ArticleAggregate> searchByContent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        logger.debug("搜索文章（内容）: keyword={}", keyword);
        return articleRepository.findByContentContaining(keyword.trim());
    }
    
    // ========== 统计方法 ==========
    
    /**
     * 统计已发布文章总数
     */
    public long countPublishedArticles() {
        logger.debug("统计已发布文章数量");
        return articleRepository.countPublishedArticles();
    }
    
    /**
     * 统计指定作者的文章数量
     */
    public long countByAuthorId(AuthorId authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        return articleRepository.countByAuthorId(authorId);
    }
    
    /**
     * 统计指定分类的文章数量
     */
    public long countByCategoryId(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return articleRepository.countByCategoryId(categoryId);
    }
    
    /**
     * 统计指定标签的文章数量
     */
    public long countByTagId(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        
        return articleRepository.countByTagId(tagId);
    }
    
    /**
     * 统计所有文章数量
     */
    public long countAllArticles() {
        return articleRepository.countAllArticles();
    }
    
    /**
     * 统计指定状态的文章数量
     */
    public long countByStatus(ArticleStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }
        
        return articleRepository.countByStatus(status);
    }
    
    // ========== 特殊查询方法 ==========
    
    /**
     * 查询最近发布的文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    public List<ArticleAggregate> findRecentlyPublished(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        logger.debug("查询最近发布文章: limit={}", limit);
        return articleRepository.findRecentlyPublished(limit);
    }
    
    /**
     * 查询热门文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    public List<ArticleAggregate> findPopularArticles(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        logger.debug("查询热门文章: limit={}", limit);
        return articleRepository.findPopularArticles(limit);
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 验证分页参数
     */
    private void validatePageParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
    }
}


