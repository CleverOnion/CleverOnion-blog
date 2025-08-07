package com.cleveronion.application.article.dto

import com.cleveronion.domain.article.aggregate.Article
import com.cleveronion.domain.tag.aggregate.Tag

/**
 * 文章数据传输对象
 * 
 * 用于在应用层和外部层之间传输文章数据
 */
data class ArticleDto(
    val id: Long,
    val authorId: Long,
    val title: String,
    val contentMd: String,
    val contentHtml: String,
    val status: String,
    val tags: List<TagDto>,
    val viewCount: Int,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        /**
         * 从领域模型创建DTO
         */
        fun fromDomain(article: Article, tags: List<Tag> = emptyList()): ArticleDto {
            return ArticleDto(
                id = article.getId().value,
                authorId = article.getAuthorId().value,
                title = article.getTitle().value,
                contentMd = article.getContent().markdown,
                contentHtml = article.getContent().html,
                status = article.getStatus().name,
                tags = tags.map { TagDto.fromDomain(it) },
                viewCount = article.getViewCount(),
                createdAt = article.getCreatedAt().value.toString(),
                updatedAt = article.getUpdatedAt().value.toString()
            )
        }
    }
}

/**
 * 文章列表数据传输对象
 * 
 * 用于传输文章列表和分页信息
 */
data class ArticleListDto(
    val articles: List<ArticleDto>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
) {
    companion object {
        /**
         * 创建文章列表DTO
         */
        fun create(
            articles: List<ArticleDto>,
            totalCount: Long,
            page: Int,
            pageSize: Int
        ): ArticleListDto {
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pageSize + 1).toInt()
            return ArticleListDto(
                articles = articles,
                totalCount = totalCount,
                page = page,
                pageSize = pageSize,
                totalPages = totalPages
            )
        }
    }
}

/**
 * 标签数据传输对象
 * 
 * 用于在应用层和外部层之间传输标签数据
 */
data class TagDto(
    val id: Long,
    val name: String,
    val articleCount: Int
) {
    companion object {
        /**
         * 从领域模型创建DTO
         */
        fun fromDomain(tag: Tag): TagDto {
            return TagDto(
                id = tag.getId().value,
                name = tag.getName().value,
                articleCount = tag.getArticleCount()
            )
        }
    }
}