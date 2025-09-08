package com.cleveronion.blog.domain.article.repository;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.article.valueobject.ArticleStatus;
import com.cleveronion.blog.domain.article.valueobject.AuthorId;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.article.valueobject.TagId;

import java.util.List;
import java.util.Optional;

/**
 * 文章仓储接口
 * 定义文章聚合的持久化操作
 * 使用领域模型作为参数和返回值，保持领域层的纯净性
 * 
 * @author CleverOnion
 */
public interface ArticleRepository {
    
    /**
     * 保存文章聚合
     * 
     * @param article 文章聚合根
     * @return 保存后的文章聚合根（包含生成的ID）
     */
    ArticleAggregate save(ArticleAggregate article);
    
    /**
     * 根据ID查找文章
     * 
     * @param id 文章ID
     * @return 文章聚合根（如果存在）
     */
    Optional<ArticleAggregate> findById(ArticleId id);
    
    /**
     * 根据ID删除文章
     * 
     * @param id 文章ID
     */
    void deleteById(ArticleId id);
    
    /**
     * 检查文章是否存在
     * 
     * @param id 文章ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(ArticleId id);
    
    /**
     * 根据作者ID查找文章列表
     * 
     * @param authorId 作者ID
     * @return 文章列表
     */
    List<ArticleAggregate> findByAuthorId(AuthorId authorId);
    
    /**
     * 根据分类ID查找文章列表
     * 
     * @param categoryId 分类ID
     * @return 文章列表
     */
    List<ArticleAggregate> findByCategoryId(CategoryId categoryId);
    
    /**
     * 根据标签ID查找文章列表
     * 
     * @param tagId 标签ID
     * @return 文章列表
     */
    List<ArticleAggregate> findByTagId(TagId tagId);
    
    /**
     * 根据状态查找文章列表
     * 
     * @param status 文章状态
     * @return 文章列表
     */
    List<ArticleAggregate> findByStatus(ArticleStatus status);
    
    /**
     * 根据作者ID和状态查找文章列表
     * 
     * @param authorId 作者ID
     * @param status 文章状态
     * @return 文章列表
     */
    List<ArticleAggregate> findByAuthorIdAndStatus(AuthorId authorId, ArticleStatus status);
    
    /**
     * 根据分类ID和状态查找文章列表
     * 
     * @param categoryId 分类ID
     * @param status 文章状态
     * @return 文章列表
     */
    List<ArticleAggregate> findByCategoryIdAndStatus(CategoryId categoryId, ArticleStatus status);
    
    /**
     * 根据标题关键词查找文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    List<ArticleAggregate> findByTitleContaining(String keyword);
    
    /**
     * 根据内容关键词查找文章
     * 
     * @param keyword 关键词
     * @return 文章列表
     */
    List<ArticleAggregate> findByContentContaining(String keyword);
    
    /**
     * 分页查找已发布的文章
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findPublishedArticles(int page, int size);
    
    /**
     * 分页查找所有文章
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findAllArticles(int page, int size);
    
    /**
     * 分页查找指定状态的文章
     * 
     * @param status 文章状态
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findByStatus(ArticleStatus status, int page, int size);
    
    /**
     * 分页查找指定分类的文章（支持状态筛选）
     * 
     * @param categoryId 分类ID
     * @param status 文章状态（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findByCategoryId(CategoryId categoryId, ArticleStatus status, int page, int size);
    
    /**
     * 分页查找指定标签的文章（支持状态筛选）
     * 
     * @param tagId 标签ID
     * @param status 文章状态（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findByTagId(TagId tagId, ArticleStatus status, int page, int size);
    
    /**
     * 分页查找指定作者的文章
     * 
     * @param authorId 作者ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findByAuthorId(AuthorId authorId, int page, int size);
    
    /**
     * 分页查找指定分类的已发布文章
     * 
     * @param categoryId 分类ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findPublishedByCategoryId(CategoryId categoryId, int page, int size);
    
    /**
     * 分页查找指定标签的已发布文章
     * 
     * @param tagId 标签ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findPublishedByTagId(TagId tagId, int page, int size);
    
    /**
     * 统计指定作者的文章数量
     * 
     * @param authorId 作者ID
     * @return 文章数量
     */
    long countByAuthorId(AuthorId authorId);
    
    /**
     * 统计指定分类的文章数量
     * 
     * @param categoryId 分类ID
     * @return 文章数量
     */
    long countByCategoryId(CategoryId categoryId);
    
    /**
     * 统计指定标签的文章数量
     * 
     * @param tagId 标签ID
     * @return 文章数量
     */
    long countByTagId(TagId tagId);
    
    /**
     * 统计已发布文章的总数量
     * 
     * @return 已发布文章数量
     */
    long countPublishedArticles();
    
    /**
     * 统计所有文章的总数量
     * 
     * @return 所有文章数量
     */
    long countAllArticles();
    
    /**
     * 统计指定分类的文章数量（支持状态筛选）
     * 
     * @param categoryId 分类ID
     * @param status 文章状态（可选）
     * @return 文章数量
     */
    long countByCategoryId(CategoryId categoryId, ArticleStatus status);
    
    /**
     * 统计指定标签的文章数量（支持状态筛选）
     * 
     * @param tagId 标签ID
     * @param status 文章状态（可选）
     * @return 文章数量
     */
    long countByTagId(TagId tagId, ArticleStatus status);
    
    /**
     * 同时按分类和标签分页查找已发布文章
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findPublishedByCategoryAndTag(CategoryId categoryId, TagId tagId, int page, int size);
    
    /**
     * 统计同时属于指定分类和标签的已发布文章数量
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @return 文章数量
     */
    long countByCategoryAndTag(CategoryId categoryId, TagId tagId);
    
    /**
     * 同时按分类和标签分页查找文章（所有状态）
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    List<ArticleAggregate> findByCategoryAndTag(CategoryId categoryId, TagId tagId, int page, int size);
    
    /**
     * 统计同时属于指定分类和标签的文章数量（所有状态）
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @return 文章数量
     */
    long countByCategoryAndTagAllStatuses(CategoryId categoryId, TagId tagId);
    
    /**
     * 统计指定状态的文章数量
     * 
     * @param status 文章状态
     * @return 文章数量
     */
    long countByStatus(ArticleStatus status);
    
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
    List<ArticleAggregate> findByCategoryAndTagAndStatus(CategoryId categoryId, TagId tagId, ArticleStatus status, int page, int size);
    
    /**
     * 统计同时属于指定分类、标签和状态的文章数量
     * 
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param status 文章状态
     * @return 文章数量
     */
    long countByCategoryAndTagAndStatus(CategoryId categoryId, TagId tagId, ArticleStatus status);
    
    /**
     * 查找最近发布的文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    List<ArticleAggregate> findRecentlyPublished(int limit);
    
    /**
     * 查找热门文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    List<ArticleAggregate> findPopularArticles(int limit);
}