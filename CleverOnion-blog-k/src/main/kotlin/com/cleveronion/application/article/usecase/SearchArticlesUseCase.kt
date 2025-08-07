package com.cleveronion.application.article.usecase

import com.cleveronion.application.article.query.SearchArticlesQuery
import com.cleveronion.application.article.dto.ArticleDto
import com.cleveronion.application.article.dto.ArticleListDto
import com.cleveronion.domain.article.port.ArticleRepositoryPort
import com.cleveronion.domain.article.valueobject.ArticleStatus
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.tag.port.TagRepositoryPort
import com.cleveronion.domain.tag.valueobject.TagId
import com.cleveronion.domain.tag.valueobject.TagName
import com.cleveronion.domain.shared.valueobject.Pagination
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.annotation.UseCase

/**
 * 搜索文章用例
 * 
 * 实现文章搜索的业务逻辑，支持多种搜索条件和排序方式
 */
@UseCase
class SearchArticlesUseCase(
    private val articleRepository: ArticleRepositoryPort,
    private val tagRepository: TagRepositoryPort
) {
    
    /**
     * 执行搜索文章用例
     * 
     * @param query 搜索文章查询
     * @return 文章列表DTO
     * @throws ValidationException 当查询参数无效时
     */
    suspend fun execute(query: SearchArticlesQuery): ArticleListDto {
        // 验证和转换查询参数
        val pagination = Pagination.of(query.page, query.pageSize)
        val authorId = query.authorId?.let { UserId.of(it) }
        val status = query.status?.let { parseArticleStatus(it) }
        val tagIds = parseTagIds(query.tags)
        
        // 执行搜索
        val articles = when {
            // 关键词搜索
            !query.keyword.isNullOrBlank() -> {
                articleRepository.search(
                    keyword = query.keyword,
                    pagination = pagination,
                    authorId = authorId,
                    status = status,
                    tagIds = tagIds
                )
            }
            // 按作者搜索
            authorId != null -> {
                articleRepository.findByAuthorId(
                    authorId = authorId,
                    pagination = pagination,
                    status = status
                )
            }
            // 按标签搜索
            tagIds.isNotEmpty() -> {
                articleRepository.findByTagIds(
                    tagIds = tagIds,
                    pagination = pagination,
                    status = status
                )
            }
            // 按状态搜索
            status != null -> {
                articleRepository.findByStatus(
                    status = status,
                    pagination = pagination
                )
            }
            // 获取最新文章
            query.sortBy == "createdAt" -> {
                articleRepository.findLatest(
                    pagination = pagination,
                    status = status
                )
            }
            // 获取热门文章
            query.sortBy == "viewCount" -> {
                articleRepository.findMostPopular(
                    pagination = pagination,
                    status = status
                )
            }
            // 默认获取最新文章
            else -> {
                articleRepository.findLatest(
                    pagination = pagination,
                    status = status ?: ArticleStatus.PUBLISHED
                )
            }
        }
        
        // 获取总数
        val totalCount = when {
            !query.keyword.isNullOrBlank() -> {
                // 对于搜索，我们需要单独计算总数
                // 这里简化处理，实际应该有专门的计数方法
                articles.size.toLong()
            }
            authorId != null -> {
                articleRepository.countByAuthorId(authorId, status)
            }
            tagIds.isNotEmpty() -> {
                // 对于标签搜索，简化处理
                articles.size.toLong()
            }
            status != null -> {
                articleRepository.countByStatus(status)
            }
            else -> {
                articleRepository.countByStatus(status ?: ArticleStatus.PUBLISHED)
            }
        }
        
        // 转换为DTO
        val articleDtos = articles.map { article ->
            val tags = if (article.getTags().isNotEmpty()) {
                article.getTags().mapNotNull { tagId ->
                    tagRepository.findById(tagId)
                }
            } else {
                emptyList()
            }
            ArticleDto.fromDomain(article, tags)
        }
        
        return ArticleListDto.create(
            articles = articleDtos,
            totalCount = totalCount,
            page = query.page,
            pageSize = query.pageSize
        )
    }
    
    /**
     * 解析文章状态
     */
    private fun parseArticleStatus(statusString: String): ArticleStatus {
        return try {
            ArticleStatus.valueOf(statusString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ValidationException("Invalid article status: $statusString")
        }
    }
    
    /**
     * 解析标签ID
     */
    private suspend fun parseTagIds(tagNames: List<String>?): Set<TagId> {
        if (tagNames.isNullOrEmpty()) {
            return emptySet()
        }
        
        return tagNames.mapNotNull { tagName ->
            try {
                val tagNameVO = TagName(tagName.trim())
                tagRepository.findByName(tagNameVO)?.getId()
            } catch (e: Exception) {
                // 忽略无效的标签名
                null
            }
        }.toSet()
    }
}