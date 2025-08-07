package com.cleveronion.domain.article.port

import com.cleveronion.domain.article.aggregate.Article
import com.cleveronion.domain.article.valueobject.ArticleId
import com.cleveronion.domain.article.valueobject.ArticleStatus
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.valueobject.TagId
import com.cleveronion.domain.shared.valueobject.Pagination

/**
 * 文章仓储端口
 * 
 * 定义文章聚合的持久化操作接口，遵循六边形架构的端口模式。
 * 具体实现由基础设施层的适配器提供。
 */
interface ArticleRepositoryPort {
    
    /**
     * 保存文章
     * 
     * @param article 要保存的文章聚合
     * @return 保存后的文章聚合
     */
    suspend fun save(article: Article): Article
    
    /**
     * 根据ID查找文章
     * 
     * @param id 文章ID
     * @return 找到的文章聚合，如果不存在则返回null
     */
    suspend fun findById(id: ArticleId): Article?
    
    /**
     * 根据ID删除文章
     * 
     * @param id 文章ID
     * @return 如果删除成功返回true，如果文章不存在返回false
     */
    suspend fun deleteById(id: ArticleId): Boolean
    
    /**
     * 检查文章是否存在
     * 
     * @param id 文章ID
     * @return 如果文章存在返回true，否则返回false
     */
    suspend fun existsById(id: ArticleId): Boolean
    
    /**
     * 根据作者ID查找文章列表
     * 
     * @param authorId 作者ID
     * @param pagination 分页参数
     * @param status 文章状态过滤，如果为null则不过滤
     * @return 文章列表
     */
    suspend fun findByAuthorId(
        authorId: UserId,
        pagination: Pagination,
        status: ArticleStatus? = null
    ): List<Article>
    
    /**
     * 根据状态查找文章列表
     * 
     * @param status 文章状态
     * @param pagination 分页参数
     * @return 文章列表
     */
    suspend fun findByStatus(
        status: ArticleStatus,
        pagination: Pagination
    ): List<Article>
    
    /**
     * 根据标签ID查找文章列表
     * 
     * @param tagId 标签ID
     * @param pagination 分页参数
     * @param status 文章状态过滤，如果为null则不过滤
     * @return 文章列表
     */
    suspend fun findByTagId(
        tagId: TagId,
        pagination: Pagination,
        status: ArticleStatus? = null
    ): List<Article>
    
    /**
     * 根据多个标签ID查找文章列表（包含所有指定标签的文章）
     * 
     * @param tagIds 标签ID集合
     * @param pagination 分页参数
     * @param status 文章状态过滤，如果为null则不过滤
     * @return 文章列表
     */
    suspend fun findByTagIds(
        tagIds: Set<TagId>,
        pagination: Pagination,
        status: ArticleStatus? = null
    ): List<Article>
    
    /**
     * 搜索文章
     * 
     * @param keyword 搜索关键词，在标题和内容中搜索
     * @param pagination 分页参数
     * @param authorId 作者ID过滤，如果为null则不过滤
     * @param status 文章状态过滤，如果为null则不过滤
     * @param tagIds 标签ID过滤，如果为空则不过滤
     * @return 文章列表
     */
    suspend fun search(
        keyword: String,
        pagination: Pagination,
        authorId: UserId? = null,
        status: ArticleStatus? = null,
        tagIds: Set<TagId> = emptySet()
    ): List<Article>
    
    /**
     * 获取文章总数
     * 
     * @param status 文章状态过滤，如果为null则统计所有状态
     * @return 文章总数
     */
    suspend fun countByStatus(status: ArticleStatus? = null): Long
    
    /**
     * 获取指定作者的文章总数
     * 
     * @param authorId 作者ID
     * @param status 文章状态过滤，如果为null则统计所有状态
     * @return 文章总数
     */
    suspend fun countByAuthorId(authorId: UserId, status: ArticleStatus? = null): Long
    
    /**
     * 获取指定标签的文章总数
     * 
     * @param tagId 标签ID
     * @param status 文章状态过滤，如果为null则统计所有状态
     * @return 文章总数
     */
    suspend fun countByTagId(tagId: TagId, status: ArticleStatus? = null): Long
    
    /**
     * 获取最受欢迎的文章列表（按浏览次数排序）
     * 
     * @param pagination 分页参数
     * @param status 文章状态过滤，如果为null则不过滤
     * @return 文章列表，按浏览次数降序排列
     */
    suspend fun findMostPopular(
        pagination: Pagination,
        status: ArticleStatus? = null
    ): List<Article>
    
    /**
     * 获取最新文章列表（按创建时间排序）
     * 
     * @param pagination 分页参数
     * @param status 文章状态过滤，如果为null则不过滤
     * @return 文章列表，按创建时间降序排列
     */
    suspend fun findLatest(
        pagination: Pagination,
        status: ArticleStatus? = null
    ): List<Article>
    
    /**
     * 批量保存文章
     * 
     * @param articles 要保存的文章列表
     * @return 保存后的文章列表
     */
    suspend fun saveAll(articles: List<Article>): List<Article>
    
    /**
     * 批量删除文章
     * 
     * @param ids 要删除的文章ID列表
     * @return 实际删除的文章数量
     */
    suspend fun deleteByIds(ids: List<ArticleId>): Int
}