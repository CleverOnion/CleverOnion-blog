package com.cleveronion.config

import io.ktor.server.application.*
import io.ktor.server.config.*

data class OAuth2Config(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scope: String
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val expirationTime: Long,
    val refreshExpirationTime: Long
)

data class AdminWhitelistConfig(
    val githubIds: List<String>
)

class SecurityConfig(private val config: ApplicationConfig) {
    
    val oauth2: OAuth2Config by lazy {
        OAuth2Config(
            clientId = config.property("oauth2.github.clientId").getString(),
            clientSecret = config.property("oauth2.github.clientSecret").getString(),
            redirectUri = config.property("oauth2.github.redirectUri").getString(),
            scope = config.property("oauth2.github.scope").getString()
        )
    }
    
    val jwt: JwtConfig by lazy {
        JwtConfig(
            secret = config.property("jwt.secret").getString(),
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            expirationTime = config.property("jwt.expirationTime").getString().toLong(),
            refreshExpirationTime = config.property("jwt.refreshExpirationTime").getString().toLong()
        )
    }
    
    val baseUrl: String by lazy {
        config.property("app.baseUrl").getString()
    }
    
    val adminWhitelist: AdminWhitelistConfig by lazy {
        AdminWhitelistConfig(
            githubIds = config.property("app.adminWhitelist.githubIds").getList()
        )
    }
}