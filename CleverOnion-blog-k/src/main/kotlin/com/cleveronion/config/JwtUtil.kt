package com.cleveronion.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.*

class JwtUtil(private val jwtConfig: JwtConfig) {
    
    private val algorithm = Algorithm.HMAC256(jwtConfig.secret)
    
    /**
     * 生成访问Token
     */
    fun generateAccessToken(userId: Long, email: String, username: String): String {
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("username", username)
            .withClaim("type", "access")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.expirationTime))
            .sign(algorithm)
    }
    
    /**
     * 生成刷新Token
     */
    fun generateRefreshToken(userId: Long): String {
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(userId.toString())
            .withClaim("type", "refresh")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.refreshExpirationTime))
            .sign(algorithm)
    }
    
    /**
     * 验证Token
     */
    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(jwtConfig.issuer)
                .withAudience(jwtConfig.audience)
                .build()
            verifier.verify(token)
        } catch (e: JWTVerificationException) {
            null
        }
    }
    
    /**
     * 从Token中提取用户ID
     */
    fun extractUserId(token: String): Long? {
        return verifyToken(token)?.subject?.toLongOrNull()
    }
    
    /**
     * 从Token中提取用户信息
     */
    fun extractUserInfo(token: String): UserTokenInfo? {
        val decodedJWT = verifyToken(token) ?: return null
        return try {
            UserTokenInfo(
                userId = decodedJWT.subject.toLong(),
                email = decodedJWT.getClaim("email").asString(),
                username = decodedJWT.getClaim("username").asString(),
                type = decodedJWT.getClaim("type").asString()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 检查Token是否即将过期（30分钟内）
     */
    fun isTokenExpiringSoon(token: String): Boolean {
        val decodedJWT = verifyToken(token) ?: return true
        val expiresAt = decodedJWT.expiresAt
        val thirtyMinutesFromNow = Date(System.currentTimeMillis() + 30 * 60 * 1000)
        return expiresAt.before(thirtyMinutesFromNow)
    }
}

data class UserTokenInfo(
    val userId: Long,
    val email: String,
    val username: String,
    val type: String
)

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)