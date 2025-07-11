package com.cleveronion.domain.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Comments : LongIdTable("comments") {
    val articleId = long("article_id").references(Articles.id)
    val userId = long("user_id").references(Users.id)
    val content = text("content")
    val parentId = long("parent_id").references(Comments.id).nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
}

@Serializable
data class Comment(
    val id: Long,
    val articleId: Long,
    val userId: Long,
    val content: String,
    val parentId: Long?,
    val createdAt: String,
    val author: User,
    val replies: List<Comment> = emptyList()
)

@Serializable
data class CreateCommentRequest(
    val articleId: Long,
    val content: String,
    val parentId: Long? = null
)