package com.cleveronion.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GitHubOAuthService(private val httpClient: HttpClient) {
    
    /**
     * 获取GitHub用户信息
     */
    suspend fun getUserInfo(accessToken: String): GitHubUser? {
        return try {
            val response: HttpResponse = httpClient.get("https://api.github.com/user") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                }
            }
            
            if (response.status == HttpStatusCode.OK) {
                response.body<GitHubUser>()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching GitHub user info: ${e.message}")
            null
        }
    }
    
    /**
     * 获取GitHub用户邮箱信息
     */
    suspend fun getUserEmails(accessToken: String): List<GitHubEmail>? {
        return try {
            val response: HttpResponse = httpClient.get("https://api.github.com/user/emails") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                }
            }
            
            if (response.status == HttpStatusCode.OK) {
                response.body<List<GitHubEmail>>()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching GitHub user emails: ${e.message}")
            null
        }
    }
    
    /**
     * 获取用户的主要邮箱
     */
    suspend fun getPrimaryEmail(accessToken: String): String? {
        val emails = getUserEmails(accessToken) ?: return null
        return emails.find { it.primary }?.email ?: emails.firstOrNull()?.email
    }
}

@Serializable
data class GitHubUser(
    val id: Long,
    val login: String,
    val name: String?,
    val email: String?,
    val avatar_url: String?,
    val bio: String?,
    val blog: String?,
    val location: String?,
    val company: String?,
    val public_repos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class GitHubEmail(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
    val visibility: String?
)