package com.cleveronion.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class GitHubOAuthService(private val httpClient: HttpClient) {
    private val logger = LoggerFactory.getLogger(GitHubOAuthService::class.java)
    
    /**
     * 获取GitHub用户信息
     */
    suspend fun getUserInfo(accessToken: String): GitHubUser? {
        return try {
            logger.info("正在调用GitHub API获取用户信息")
            val response: HttpResponse = httpClient.get("https://api.github.com/user") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                }
            }
            
            logger.info("GitHub API响应状态: ${response.status}")
            if (response.status == HttpStatusCode.OK) {
                val user = response.body<GitHubUser>()
                logger.info("成功获取GitHub用户信息: ${user.login} (ID: ${user.id})")
                user
            } else {
                logger.error("GitHub API返回错误状态: ${response.status}")
                logger.error("响应内容: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            logger.error("调用GitHub API获取用户信息失败", e)
            null
        }
    }
    
    /**
     * 获取GitHub用户邮箱信息
     */
    suspend fun getUserEmails(accessToken: String): List<GitHubEmail>? {
        return try {
            logger.info("正在调用GitHub API获取用户邮箱信息")
            val response: HttpResponse = httpClient.get("https://api.github.com/user/emails") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                }
            }
            
            logger.info("GitHub邮箱API响应状态: ${response.status}")
            if (response.status == HttpStatusCode.OK) {
                val emails = response.body<List<GitHubEmail>>()
                logger.info("成功获取${emails.size}个邮箱地址")
                emails.forEach { email ->
                    logger.info("邮箱: ${email.email}, 主要: ${email.primary}, 验证: ${email.verified}")
                }
                emails
            } else {
                logger.error("GitHub邮箱API返回错误状态: ${response.status}")
                logger.error("响应内容: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            logger.error("调用GitHub API获取用户邮箱失败", e)
            null
        }
    }
    
    /**
     * 获取用户的主要邮箱
     */
    suspend fun getPrimaryEmail(accessToken: String): String? {
        logger.info("正在获取用户主要邮箱")
        val emails = getUserEmails(accessToken) ?: return null
        val primaryEmail = emails.find { it.primary }?.email ?: emails.firstOrNull()?.email
        logger.info("确定主要邮箱: ${primaryEmail ?: "未找到邮箱"}")
        return primaryEmail
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