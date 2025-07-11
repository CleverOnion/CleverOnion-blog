package com.cleveronion.service

import com.cleveronion.config.JwtUtil
import com.cleveronion.config.TokenPair
import com.cleveronion.domain.entity.User
import kotlinx.serialization.Serializable

class AuthService(
    private val userService: UserService,
    private val gitHubOAuthService: GitHubOAuthService,
    private val jwtUtil: JwtUtil
) {
    
    /**
     * 处理GitHub OAuth回调
     */
    suspend fun handleGitHubCallback(accessToken: String): AuthResult? {
        try {
            // 获取GitHub用户信息
            val githubUser = gitHubOAuthService.getUserInfo(accessToken)
                ?: return AuthResult.Error("Failed to fetch GitHub user info")
            
            // 获取用户主要邮箱
            val primaryEmail = gitHubOAuthService.getPrimaryEmail(accessToken)
            
            // 创建或更新用户
            val user = userService.createOrUpdateUser(githubUser, primaryEmail)
            
            // 生成JWT Token
            val tokenPair = generateTokenPair(user)
            
            return AuthResult.Success(
                user = user,
                accessToken = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken
            )
        } catch (e: Exception) {
            return AuthResult.Error("Authentication failed: ${e.message}")
        }
    }
    
    /**
     * 刷新访问Token
     */
    fun refreshAccessToken(refreshToken: String): RefreshResult? {
        try {
            // 验证刷新Token
            val userInfo = jwtUtil.extractUserInfo(refreshToken)
            if (userInfo?.type != "refresh") {
                return RefreshResult.Error("Invalid refresh token")
            }
            
            // 获取用户信息
            val user = userService.findById(userInfo.userId)
                ?: return RefreshResult.Error("User not found")
            
            // 生成新的访问Token
            val newAccessToken = jwtUtil.generateAccessToken(
                userId = user.id,
                email = user.email ?: "",
                username = user.githubLogin
            )
            
            return RefreshResult.Success(
                accessToken = newAccessToken,
                user = user
            )
        } catch (e: Exception) {
            return RefreshResult.Error("Token refresh failed: ${e.message}")
        }
    }
    
    /**
     * 验证访问Token并获取用户信息
     */
    fun validateAccessToken(accessToken: String): User? {
        val userInfo = jwtUtil.extractUserInfo(accessToken)
        if (userInfo?.type != "access") return null
        
        return userService.findById(userInfo.userId)
    }
    
    /**
     * 登出用户（在实际应用中可能需要将Token加入黑名单）
     */
    fun logout(accessToken: String): Boolean {
        // 在这里可以实现Token黑名单逻辑
        // 目前只是简单返回true，表示登出成功
        return true
    }
    
    /**
     * 生成Token对
     */
    private fun generateTokenPair(user: User): TokenPair {
        val accessToken = jwtUtil.generateAccessToken(
            userId = user.id,
            email = user.email ?: "",
            username = user.githubLogin
        )
        
        val refreshToken = jwtUtil.generateRefreshToken(user.id)
        
        return TokenPair(accessToken, refreshToken)
    }
    
    /**
     * 检查Token是否需要刷新
     */
    fun shouldRefreshToken(accessToken: String): Boolean {
        return jwtUtil.isTokenExpiringSoon(accessToken)
    }
}

/**
 * 认证结果
 */
sealed class AuthResult {
    @Serializable
    data class Success(
        val user: User,
        val accessToken: String,
        val refreshToken: String
    ) : AuthResult()
    
    @Serializable
    data class Error(val message: String) : AuthResult()
}

/**
 * 刷新Token结果
 */
sealed class RefreshResult {
    @Serializable
    data class Success(
        val accessToken: String,
        val user: User
    ) : RefreshResult()
    
    @Serializable
    data class Error(val message: String) : RefreshResult()
}

/**
 * 登录响应
 */
@Serializable
data class LoginResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

/**
 * 刷新Token响应
 */
@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Long
)