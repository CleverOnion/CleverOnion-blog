package com.cleveronion.service

import com.cleveronion.config.JwtUtil
import com.cleveronion.config.TokenPair
import com.cleveronion.domain.entity.User
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

class AuthService(
    private val userService: UserService,
    private val gitHubOAuthService: GitHubOAuthService,
    private val jwtUtil: JwtUtil
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)
    
    /**
     * 处理GitHub OAuth回调
     */
    suspend fun handleGitHubCallback(accessToken: String): AuthResult? {
        try {
            logger.info("开始处理GitHub OAuth回调，访问令牌长度: ${accessToken.length}")
            
            // 获取GitHub用户信息
            logger.info("正在获取GitHub用户信息...")
            val githubUser = gitHubOAuthService.getUserInfo(accessToken)
            if (githubUser == null) {
                logger.error("获取GitHub用户信息失败")
                return AuthResult.Error("Failed to fetch GitHub user info")
            }
            logger.info("成功获取GitHub用户信息: ${githubUser.login} (ID: ${githubUser.id})")
            
            // 获取用户主要邮箱
            logger.info("正在获取用户主要邮箱...")
            val primaryEmail = gitHubOAuthService.getPrimaryEmail(accessToken)
            logger.info("获取到主要邮箱: ${primaryEmail ?: "无邮箱"}")
            
            // 创建或更新用户
            logger.info("正在创建或更新用户...")
            val user = userService.createOrUpdateUser(githubUser, primaryEmail)
            logger.info("用户创建/更新成功: ${user.githubLogin} (数据库ID: ${user.id})")
            
            // 生成JWT Token
            logger.info("正在生成JWT令牌...")
            val tokenPair = generateTokenPair(user)
            logger.info("JWT令牌生成成功")
            
            return AuthResult.Success(
                user = user,
                accessToken = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken
            )
        } catch (e: Exception) {
            logger.error("GitHub OAuth回调处理失败", e)
            return AuthResult.Error("Authentication failed: ${e.message}")
        }
    }
    
    /**
     * 刷新访问Token
     */
    fun refreshAccessToken(refreshToken: String): RefreshResult? {
        try {
            logger.info("开始刷新访问令牌")
            
            // 验证刷新Token
            val userInfo = jwtUtil.extractUserInfo(refreshToken)
            if (userInfo?.type != "refresh") {
                logger.error("无效的刷新令牌类型: ${userInfo?.type}")
                return RefreshResult.Error("Invalid refresh token")
            }
            logger.info("刷新令牌验证成功，用户ID: ${userInfo.userId}")
            
            // 获取用户信息
            val user = userService.findById(userInfo.userId)
            if (user == null) {
                logger.error("用户不存在，ID: ${userInfo.userId}")
                return RefreshResult.Error("User not found")
            }
            logger.info("找到用户: ${user.githubLogin}")
            
            // 生成新的访问Token
            val newAccessToken = jwtUtil.generateAccessToken(
                userId = user.id,
                email = user.email ?: "",
                username = user.githubLogin
            )
            logger.info("新访问令牌生成成功")
            
            return RefreshResult.Success(
                accessToken = newAccessToken,
                user = user
            )
        } catch (e: Exception) {
            logger.error("令牌刷新失败", e)
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