package com.cleveronion.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.cleveronion.domain.entity.*

class DatabaseConfig(private val config: ApplicationConfig) {
    
    companion object {
        lateinit var database: Database
            private set
    }
    
    fun init(): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.property("database.url").getString()
            driverClassName = config.property("database.driver").getString()
            username = config.property("database.user").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = config.property("database.maxPoolSize").getString().toInt()
            minimumIdle = config.property("database.minPoolSize").getString().toInt()
            
            // 连接池优化配置
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            leakDetectionThreshold = 60000
        }
        
        val dataSource = HikariDataSource(hikariConfig)
        database = Database.connect(dataSource)
        
        // 创建表结构
        transaction(database) {
            SchemaUtils.create(
                Users,
                Articles,
                Tags,
                ArticleTags,
                Comments
            )
        }
        
        return database
    }
}

fun Application.configureDatabases() {
    val databaseConfig = DatabaseConfig(environment.config)
    databaseConfig.init()
}