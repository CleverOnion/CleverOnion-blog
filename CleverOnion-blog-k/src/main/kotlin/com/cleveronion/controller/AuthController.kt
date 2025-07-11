package com.cleveronion.controller

import com.cleveronion.config.SecurityConfig
import com.cleveronion.service.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Route.authRoutes() {
    val securityConfig = SecurityConfig(application.environment.config)
    
    // 创建HTTP客户端
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    // 创建服务实例
    val userService = UserService()
    val gitHubOAuthService = GitHubOAuthService(httpClient)
    val authService = AuthService(userService, gitHubOAuthService, com.cleveronion.config.JwtUtil(securityConfig.jwt))
    
    route("/auth") {
        
        // GitHub OAuth登录入口
        get("/github") {
            val state = generateRandomState()
            val githubAuthUrl = buildString {
                append("https://github.com/login/oauth/authorize")
                append("?client_id=${securityConfig.oauth2.clientId}")
                append("&redirect_uri=${securityConfig.oauth2.redirectUri}")
                append("&scope=${securityConfig.oauth2.scope}")
                append("&state=$state")
            }
            
            call.respondRedirect(githubAuthUrl)
        }
        
        // GitHub OAuth回调处理
        authenticate("auth-oauth-github") {
            get("/github/callback") {
                try {
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "OAuth authentication failed")
                        )
                    
                    val accessToken = principal.accessToken
                    val authResult = authService.handleGitHubCallback(accessToken)
                    
                    when (authResult) {
                        is AuthResult.Success -> {
                            call.respond(
                                HttpStatusCode.OK,
                                LoginResponse(
                                    user = authResult.user,
                                    accessToken = authResult.accessToken,
                                    refreshToken = authResult.refreshToken,
                                    expiresIn = securityConfig.jwt.expirationTime
                                )
                            )
                        }
                        is AuthResult.Error -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to authResult.message)
                            )
                        }
                        null -> {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Authentication processing failed")
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Internal server error: ${e.message}")
                    )
                }
            }
        }
        
        // 刷新Token
        post("/refresh") {
            try {
                val request = call.receive<RefreshTokenRequest>()
                val refreshResult = authService.refreshAccessToken(request.refreshToken)
                
                when (refreshResult) {
                    is RefreshResult.Success -> {
                        call.respond(
                            HttpStatusCode.OK,
                            RefreshTokenResponse(
                                accessToken = refreshResult.accessToken,
                                expiresIn = securityConfig.jwt.expirationTime
                            )
                        )
                    }
                    is RefreshResult.Error -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to refreshResult.message)
                        )
                    }
                    null -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid refresh token")
                        )
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid request format")
                )
            }
        }
        
        // 登出
        authenticate("auth-jwt") {
            post("/logout") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Invalid token")
                        )
                    
                    // 这里可以实现Token黑名单逻辑
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "Logged out successfully")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Logout failed")
                    )
                }
            }
        }
        
        // 获取当前用户信息
        authenticate("auth-jwt") {
            get("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Invalid token")
                        )
                    
                    val userId = principal.payload.subject?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID in token")
                        )
                    
                    val user = userService.findById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "User not found")
                        )
                    
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get user info")
                    )
                }
            }
        }
    }
}

/**
 * 生成随机状态字符串用于OAuth2安全验证
 */
private fun generateRandomState(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..32)
        .map { chars.random() }
        .joinToString("")
}

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)