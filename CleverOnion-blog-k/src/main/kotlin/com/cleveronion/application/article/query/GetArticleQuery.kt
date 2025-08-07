package com.cleveronion.application.article.query

/**
 * 获取文章查询
 * 
 * 包含获取单个文章所需的查询条件
 */
data class GetArticleQuery(
    val articleId: Long,
    val includeUnpublished: Boolean = false,
    val requestUserId: Long? = null
) {
    init {
        require(articleId > 0) { "Article ID must be positive" }
        requestUserId?.let { require(it > 0) { "Request user ID must be positive" } }
    }
}