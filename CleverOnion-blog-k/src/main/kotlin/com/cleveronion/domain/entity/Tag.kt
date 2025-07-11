package com.cleveronion.domain.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable

object Tags : LongIdTable("tags") {
    val name = varchar("name", 50).uniqueIndex()
}

object ArticleTags : LongIdTable("article_tags") {
    val articleId = long("article_id").references(Articles.id)
    val tagId = long("tag_id").references(Tags.id)
    
    init {
        uniqueIndex(articleId, tagId)
    }
}

@Serializable
data class Tag(
    val id: Long,
    val name: String
)

@Serializable
data class CreateTagRequest(
    val name: String
)