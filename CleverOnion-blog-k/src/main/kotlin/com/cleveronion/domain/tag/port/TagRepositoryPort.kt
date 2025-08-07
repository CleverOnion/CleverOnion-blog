package com.cleveronion.domain.tag.port

import com.cleveronion.domain.tag.aggregate.Tag
import com.cleveronion.domain.tag.valueobject.TagId
import com.cleveronion.domain.tag.valueobject.TagName
import com.cleveronion.domain.shared.valueobject.Pagination

/**
 * 标签仓储端口
 * 
 * 定义标签聚合的持久化操作接口，遵循六边形架构的端口模式。
 * 具体实现由基础设施层的适配器提供。
 */
interface TagRepositoryPort {
    
    /**
     * 保存标签
     * 
     * @param tag 要保存的标签聚合
     * @return 保存后的标签聚合
     */
    suspend fun save(tag: Tag): Tag
    
    /**
     * 根据ID查找标签
     * 
     * @param id 标签ID
     * @return 找到的标签聚合，如果不存在则返回null
     */
    suspend fun findById(id: TagId): Tag?
    
    /**
     * 根据名称查找标签
     * 
     * @param name 标签名称
     * @return 找到的标签聚合，如果不存在则返回null
     */
    suspend fun findByName(name: TagName): Tag?
    
    /**
     * 根据ID删除标签
     * 
     * @param id 标签ID
     * @return 如果删除成功返回true，如果标签不存在返回false
     */
    suspend fun deleteById(id: TagId): Boolean
    
    /**
     * 检查标签是否存在
     * 
     * @param id 标签ID
     * @return 如果标签存在返回true，否则返回false
     */
    suspend fun existsById(id: TagId): Boolean
    
    /**
     * 检查标签名称是否已存在
     * 
     * @param name 标签名称
     * @return 如果标签名称已存在返回true，否则返回false
     */
    suspend fun existsByName(name: TagName): Boolean
    
    /**
     * 获取所有活跃标签
     * 
     * @param pagination 分页参数
     * @return 活跃标签列表，按名称排序
     */
    suspend fun findActiveTags(pagination: Pagination): List<Tag>
    
    /**
     * 获取所有非活跃标签
     * 
     * @param pagination 分页参数
     * @return 非活跃标签列表，按名称排序
     */
    suspend fun findInactiveTags(pagination: Pagination): List<Tag>
    
    /**
     * 根据名称搜索标签
     * 
     * @param nameKeyword 名称关键词
     * @param pagination 分页参数
     * @param activeOnly 是否只搜索活跃标签，默认为true
     * @return 标签列表，按相关性排序
     */
    suspend fun searchByName(
        nameKeyword: String,
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<Tag>
    
    /**
     * 获取热门标签列表
     * 
     * @param pagination 分页参数
     * @param minArticleCount 最小文章数量，默认为1
     * @param activeOnly 是否只包含活跃标签，默认为true
     * @return 标签列表，按文章数量降序排列
     */
    suspend fun findPopularTags(
        pagination: Pagination,
        minArticleCount: Int = 1,
        activeOnly: Boolean = true
    ): List<Tag>
    
    /**
     * 获取最新创建的标签列表
     * 
     * @param pagination 分页参数
     * @param activeOnly 是否只包含活跃标签，默认为true
     * @return 标签列表，按创建时间降序排列
     */
    suspend fun findLatestTags(
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<Tag>
    
    /**
     * 获取空标签列表（没有关联文章的标签）
     * 
     * @param pagination 分页参数
     * @param activeOnly 是否只包含活跃标签，默认为true
     * @return 空标签列表，按创建时间降序排列
     */
    suspend fun findEmptyTags(
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<Tag>
    
    /**
     * 获取需要清理的标签列表
     * 
     * @param daysThreshold 天数阈值
     * @param pagination 分页参数
     * @return 需要清理的标签列表
     */
    suspend fun findTagsNeedingCleanup(
        daysThreshold: Long,
        pagination: Pagination
    ): List<Tag>  
  
    /**
     * 获取系统标签列表
     * 
     * @param pagination 分页参数
     * @return 系统标签列表，按名称排序
     */
    suspend fun findSystemTags(pagination: Pagination): List<Tag>
    
    /**
     * 获取用户创建的标签列表
     * 
     * @param pagination 分页参数
     * @param activeOnly 是否只包含活跃标签，默认为true
     * @return 用户创建的标签列表，按创建时间降序排列
     */
    suspend fun findUserCreatedTags(
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<Tag>
    
    /**
     * 根据文章数量范围查找标签
     * 
     * @param minCount 最小文章数量
     * @param maxCount 最大文章数量
     * @param pagination 分页参数
     * @param activeOnly 是否只包含活跃标签，默认为true
     * @return 标签列表，按文章数量降序排列
     */
    suspend fun findByArticleCountRange(
        minCount: Int,
        maxCount: Int,
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<Tag>
    
    /**
     * 获取标签总数
     * 
     * @param activeOnly 是否只统计活跃标签，默认为false
     * @return 标签总数
     */
    suspend fun count(activeOnly: Boolean = false): Long
    
    /**
     * 获取空标签总数
     * 
     * @param activeOnly 是否只统计活跃标签，默认为false
     * @return 空标签总数
     */
    suspend fun countEmptyTags(activeOnly: Boolean = false): Long
    
    /**
     * 获取热门标签总数
     * 
     * @param minArticleCount 最小文章数量
     * @param activeOnly 是否只统计活跃标签，默认为false
     * @return 热门标签总数
     */
    suspend fun countPopularTags(minArticleCount: Int, activeOnly: Boolean = false): Long
    
    /**
     * 获取系统标签总数
     * 
     * @return 系统标签总数
     */
    suspend fun countSystemTags(): Long
    
    /**
     * 批量保存标签
     * 
     * @param tags 要保存的标签列表
     * @return 保存后的标签列表
     */
    suspend fun saveAll(tags: List<Tag>): List<Tag>
    
    /**
     * 批量删除标签
     * 
     * @param ids 要删除的标签ID列表
     * @return 实际删除的标签数量
     */
    suspend fun deleteByIds(ids: List<TagId>): Int
    
    /**
     * 批量激活标签
     * 
     * @param ids 要激活的标签ID列表
     * @return 实际激活的标签数量
     */
    suspend fun activateTags(ids: List<TagId>): Int
    
    /**
     * 批量停用标签
     * 
     * @param ids 要停用的标签ID列表
     * @return 实际停用的标签数量
     */
    suspend fun deactivateTags(ids: List<TagId>): Int
    
    /**
     * 批量更新标签的文章计数
     * 
     * @param tagCounts 标签ID和对应文章数量的映射
     * @return 实际更新的标签数量
     */
    suspend fun updateArticleCounts(tagCounts: Map<TagId, Int>): Int
    
    /**
     * 查找相似的标签
     * 
     * @param name 标签名称
     * @param pagination 分页参数
     * @param activeOnly 是否只搜索活跃标签，默认为true
     * @return 相似标签列表
     */
    suspend fun findSimilarTags(
        name: TagName,
        pagination: Pagination,
        activeOnly: Boolean = true
    ): List<Tag>
    
    /**
     * 获取标签使用统计
     * 
     * @param startTime 开始时间（ISO字符串格式）
     * @param endTime 结束时间（ISO字符串格式）
     * @param pagination 分页参数
     * @return 标签列表，按使用频率降序排列
     */
    suspend fun getTagUsageStatistics(
        startTime: String,
        endTime: String,
        pagination: Pagination
    ): List<Tag>
}