package com.cleveronion.application.article.command

/**
 * 更新文章命令
 * 
 * 包含更新文章所需的信息
 */
data class UpdateArticleCommand(
    val articleId: Long,
    val userId: Long,
    val title: String? = null,
    val contentMd: String? = null,
    val tags: List<String>? = null
) {
    init {
        require(articleId > 0) { "Article ID must be positive" }
        require(userId > 0) { "User ID must be positive" }
        title?.let { require(it.isNotBlank()) { "Title cannot be blank" } }
        contentMd?.let { require(it.isNotBlank()) { "Content cannot be blank" } }
        tags?.let { require(it.all { tag -> tag.isNotBlank() }) { "Tag names cannot be blank" } }
    }
}