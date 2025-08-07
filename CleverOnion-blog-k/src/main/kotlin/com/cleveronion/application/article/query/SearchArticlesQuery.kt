package com.cleveronion.application.article.query

/**
 * 搜索文章查询
 * 
 * 包含搜索文章所需的查询条件
 */
data class SearchArticlesQuery(
    val keyword: String? = null,
    val authorId: Long? = null,
    val status: String? = null,
    val tags: List<String>? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
) {
    init {
        require(page > 0) { "Page must be positive" }
        require(pageSize > 0 && pageSize <= 100) { "Page size must be between 1 and 100" }
        authorId?.let { require(it > 0) { "Author ID must be positive" } }
        keyword?.let { require(it.isNotBlank()) { "Keyword cannot be blank" } }
        tags?.let { require(it.all { tag -> tag.isNotBlank() }) { "Tag names cannot be blank" } }
        require(sortBy in listOf("createdAt", "updatedAt", "viewCount", "title")) { 
            "Sort by must be one of: createdAt, updatedAt, viewCount, title" 
        }
        require(sortOrder in listOf("asc", "desc")) { "Sort order must be 'asc' or 'desc'" }
    }
}