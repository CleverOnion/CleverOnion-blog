package com.cleveronion.application.article.command

/**
 * 删除文章命令
 * 
 * 包含删除文章所需的信息
 */
data class DeleteArticleCommand(
    val articleId: Long,
    val userId: Long
) {
    init {
        require(articleId > 0) { "Article ID must be positive" }
        require(userId > 0) { "User ID must be positive" }
    }
}