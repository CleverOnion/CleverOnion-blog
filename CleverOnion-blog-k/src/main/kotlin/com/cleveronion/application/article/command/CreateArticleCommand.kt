package com.cleveronion.application.article.command

/**
 * 创建文章命令
 * 
 * 包含创建文章所需的所有信息
 */
data class CreateArticleCommand(
    val title: String,
    val contentMd: String,
    val authorId: Long,
    val tags: List<String> = emptyList()
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(contentMd.isNotBlank()) { "Content cannot be blank" }
        require(authorId > 0) { "Author ID must be positive" }
        require(tags.all { it.isNotBlank() }) { "Tag names cannot be blank" }
    }
}