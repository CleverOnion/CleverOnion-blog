package com.cleveronion.controller

import com.cleveronion.config.SecurityConfig
import com.cleveronion.service.*
import com.cleveronion.domain.entity.ApiResponse
import com.cleveronion.domain.entity.User
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
                        ?: return@get call.respondRedirect(
                            "http://localhost:5173/auth/callback?error=oauth_failed&error_description=OAuth authentication failed"
                        )
                    
                    val accessToken = principal.accessToken
                    val authResult = authService.handleGitHubCallback(accessToken)
                    
                    when (authResult) {
                        is AuthResult.Success -> {
                            // 将token信息编码后重定向到前端
                            val redirectUrl = buildString {
                                append("http://localhost:5173/auth/callback")
                                append("?success=true")
                                append("&access_token=${authResult.accessToken}")
                                append("&refresh_token=${authResult.refreshToken}")
                                append("&expires_in=${securityConfig.jwt.expirationTime}")
                                append("&user_id=${authResult.user.id}")
                                append("&user_name=${authResult.user.name ?: authResult.user.githubLogin}")
                                append("&user_avatar=${authResult.user.avatarUrl}")
                            }
                            call.respondRedirect(redirectUrl)
                        }
                        is AuthResult.Error -> {
                            call.respondRedirect(
                                "http://localhost:5173/auth/callback?error=auth_failed&error_description=${authResult.message}"
                            )
                        }
                        null -> {
                            call.respondRedirect(
                                "http://localhost:5173/auth/callback?error=processing_failed&error_description=Authentication processing failed"
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respondRedirect(
                        "http://localhost:5173/auth/callback?error=server_error&error_description=Internal server error: ${e.message}"
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
                            ApiResponse.success(
                                RefreshTokenResponse(
                                    accessToken = refreshResult.accessToken,
                                    expiresIn = securityConfig.jwt.expirationTime
                                ),
                                "Token刷新成功"
                            )
                        )
                    }
                    is RefreshResult.Error -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<RefreshTokenResponse>(refreshResult.message)
                        )
                    }
                    null -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<RefreshTokenResponse>("无效的刷新令牌")
                        )
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<RefreshTokenResponse>("请求格式无效")
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
                            ApiResponse.error<Unit>("无效的令牌")
                        )
                    
                    // 这里可以实现Token黑名单逻辑
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success(Unit, "登出成功")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Unit>("登出失败")
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
                            ApiResponse.error<User>("无效的令牌")
                        )
                    
                    val userId = principal.payload.subject?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<User>("令牌中的用户ID无效")
                        )
                    
                    val user = userService.findById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<User>("用户不存在")
                        )
                    
                    call.respond(HttpStatusCode.OK, ApiResponse.success(user, "获取用户信息成功"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<User>("获取用户信息失败")
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