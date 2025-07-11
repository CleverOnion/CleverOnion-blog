package com.cleveronion.domain.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Users : LongIdTable("users") {
    val githubId = long("github_id").uniqueIndex()
    val githubLogin = varchar("github_login", 100)
    val avatarUrl = text("avatar_url").nullable()
    val email = varchar("email", 255).nullable()
    val name = varchar("name", 100).nullable()
    val bio = text("bio").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

@Serializable
data class User(
    val id: Long,
    val githubId: Long,
    val githubLogin: String,
    val avatarUrl: String?,
    val email: String?,
    val name: String?,
    val bio: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateUserRequest(
    val githubId: Long,
    val githubLogin: String,
    val avatarUrl: String?,
    val email: String?,
    val name: String?
)