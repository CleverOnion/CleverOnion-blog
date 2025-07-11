package com.cleveronion

import com.cleveronion.controller.authRoutes
import com.cleveronion.controller.userRoutes
import com.cleveronion.controller.articleRoutes
import com.cleveronion.controller.tagRoutes
import com.cleveronion.controller.commentRoutes
import com.cleveronion.controller.uploadRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // 健康检查端点
        get("/") {
            call.respondText("CleverOnion Blog API is running!")
        }
        
        get("/health") {
            call.respondText("OK")
        }
        
        // API路由
        route("/api/v1") {
            // 认证相关路由
            authRoutes()
            
            // 用户管理路由
            userRoutes()
            
            // 文章管理路由
            articleRoutes()
            
            // 标签管理路由
            tagRoutes()
            
            // 评论管理路由
            commentRoutes()
            
            // 文件上传路由
            uploadRoutes()
        }
    }
}