package com.cleveronion

import com.cleveronion.config.SecurityConfig
import com.cleveronion.config.JwtUtil
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.slf4j.LoggerFactory

fun Application.configureAuthentication() {
    val logger = LoggerFactory.getLogger("Authentication")
    val securityConfig = SecurityConfig(environment.config)
    val jwtUtil = JwtUtil(securityConfig.jwt)
    
    logger.info("正在配置认证系统...")
    logger.info("OAuth2配置 - ClientId: ${securityConfig.oauth2.clientId}")
    logger.info("OAuth2配置 - RedirectUri: ${securityConfig.oauth2.redirectUri}")
    logger.info("OAuth2配置 - Scope: ${securityConfig.oauth2.scope}")
    
    install(Authentication) {
        // JWT认证配置
        jwt("auth-jwt") {
            realm = securityConfig.jwt.issuer
            
            verifier(
                JWT.require(Algorithm.HMAC256(securityConfig.jwt.secret))
                    .withIssuer(securityConfig.jwt.issuer)
                    .withAudience(securityConfig.jwt.audience)
                    .build()
            )
            
            validate { credential ->
                logger.debug("正在验证JWT令牌...")
                // 验证Token类型为access token
                val tokenType = credential.payload.getClaim("type").asString()
                logger.debug("JWT令牌类型: $tokenType")
                
                if (tokenType == "access") {
                    val userId = credential.payload.subject?.toLongOrNull()
                    val email = credential.payload.getClaim("email").asString()
                    val username = credential.payload.getClaim("username").asString()
                    
                    logger.debug("JWT令牌信息 - 用户ID: $userId, 邮箱: $email, 用户名: $username")
                    
                    if (userId != null && email != null && username != null) {
                        logger.debug("JWT令牌验证成功")
                        JWTPrincipal(credential.payload)
                    } else {
                        logger.warn("JWT令牌缺少必要信息")
                        null
                    }
                } else {
                    logger.warn("JWT令牌类型不正确: $tokenType")
                    null
                }
            }
            
            challenge { defaultScheme, realm ->
                logger.warn("JWT认证失败，返回401错误")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "error" to "invalid_token",
                        "message" to "Token is invalid or expired"
                    )
                )
            }
        }
        
        // OAuth2认证配置（用于GitHub登录）
        oauth("auth-oauth-github") {
            urlProvider = { 
                logger.info("OAuth2 URL Provider调用，返回: ${securityConfig.oauth2.redirectUri}")
                securityConfig.oauth2.redirectUri 
            }
            providerLookup = {
                logger.info("OAuth2 Provider Lookup调用")
                val settings = OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    requestMethod = HttpMethod.Post,
                    clientId = securityConfig.oauth2.clientId,
                    clientSecret = securityConfig.oauth2.clientSecret,
                    defaultScopes = listOf(securityConfig.oauth2.scope)
                )
                logger.info("OAuth2设置创建完成: ${settings.name}")
                settings
            }
            client = HttpClient(CIO)
        }
    }
}

/**
 * 从JWT Principal中提取用户信息
 */
fun JWTPrincipal.getUserId(): Long? {
    return payload.subject?.toLongOrNull()
}

fun JWTPrincipal.getEmail(): String? {
    return payload.getClaim("email").asString()
}

fun JWTPrincipal.getUsername(): String? {
    return payload.getClaim("username").asString()
}