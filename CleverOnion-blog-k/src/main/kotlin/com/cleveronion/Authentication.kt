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

fun Application.configureAuthentication() {
    val securityConfig = SecurityConfig(environment.config)
    val jwtUtil = JwtUtil(securityConfig.jwt)
    
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
                // 验证Token类型为access token
                val tokenType = credential.payload.getClaim("type").asString()
                if (tokenType == "access") {
                    val userId = credential.payload.subject?.toLongOrNull()
                    val email = credential.payload.getClaim("email").asString()
                    val username = credential.payload.getClaim("username").asString()
                    
                    if (userId != null && email != null && username != null) {
                        JWTPrincipal(credential.payload)
                    } else null
                } else null
            }
            
            challenge { defaultScheme, realm ->
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
            urlProvider = { securityConfig.oauth2.redirectUri }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    requestMethod = HttpMethod.Post,
                    clientId = securityConfig.oauth2.clientId,
                    clientSecret = securityConfig.oauth2.clientSecret,
                    defaultScopes = listOf(securityConfig.oauth2.scope)
                )
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