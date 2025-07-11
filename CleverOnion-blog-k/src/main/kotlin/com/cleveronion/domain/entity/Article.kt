package com.cleveronion.domain.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

enum class ArticleStatus {
    DRAFT, PUBLISHED, ARCHIVED
}

object Articles : LongIdTable("articles") {
    val authorId = long("author_id").references(Users.id)
    val title = varchar("title", 255)
    val contentMd = text("content_md")
    val contentHtml = text("content_html").nullable()
    val status = enumeration("status", ArticleStatus::class).default(ArticleStatus.DRAFT)
    val viewCount = integer("view_count").default(0)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

@Serializable
data class Article(
    val id: Long,
    val authorId: Long,
    val title: String,
    val contentMd: String,
    val contentHtml: String?,
    val status: String,
    val viewCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val tags: List<Tag> = emptyList()
)

@Serializable
data class CreateArticleRequest(
    val title: String,
    val contentMd: String,
    val tagIds: List<Long> = emptyList()
)

@Serializable
data class UpdateArticleRequest(
    val title: String?,
    val contentMd: String?,
    val status: String?,
    val tagIds: List<Long>?
)