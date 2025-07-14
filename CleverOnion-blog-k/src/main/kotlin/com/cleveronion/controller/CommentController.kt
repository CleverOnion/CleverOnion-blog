package com.cleveronion.controller

import com.cleveronion.domain.entity.*
import com.cleveronion.service.CommentService
import com.cleveronion.service.CommentWithReplies
import com.cleveronion.getUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * 评论控制器
 * 提供评论管理的RESTful API接口
 * 遵循RESTful设计原则和单一职责原则
 */
fun Route.commentRoutes() {
    val commentService = CommentService()
    
    route("/comments") {
        
        // ========== 公开接口（无需认证） ==========
        
        /**
         * 获取文章的评论列表（分页）
         * GET /api/v1/comments/article/{articleId}?page=1&pageSize=20
         */
        get("/article/{articleId}") {
            try {
                val articleId = call.parameters["articleId"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<PaginatedData<CommentWithReplies>>("无效的文章ID")
                    )
                
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                
                val validPage = maxOf(1, page)
                val validPageSize = when {
                    pageSize < 1 -> 20
                    pageSize > 100 -> 100
                    else -> pageSize
                }
                
                val comments = commentService.getCommentsByArticleId(articleId, validPage, validPageSize)
                val totalCount = commentService.getCommentCountByArticleId(articleId)
                val totalPages = (totalCount + validPageSize - 1) / validPageSize
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.paginated(
                        data = comments,
                        pagination = PaginationInfo(
                            currentPage = validPage,
                            pageSize = validPageSize,
                            totalCount = totalCount,
                            totalPages = totalPages.toInt()
                        ),
                        message = "获取评论列表成功"
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<PaginatedData<CommentWithReplies>>(e.message ?: "请求参数无效")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<PaginatedData<CommentWithReplies>>("获取评论列表失败: ${e.message}")
                )
            }
        }
        
        /**
         * 根据ID获取评论详情
         * GET /api/v1/comments/{id}
         */
        get("/{id}") {
            try {
                val commentId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<CommentWithReplies>("无效的评论ID")
                    )
                
                val comment = commentService.getCommentById(commentId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse.error<CommentWithReplies>("评论不存在")
                    )
                
                call.respond(HttpStatusCode.OK, ApiResponse.success(comment, "获取评论详情成功"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<CommentWithReplies>("获取评论详情失败: ${e.message}")
                )
            }
        }
        
        /**
         * 获取最新评论列表
         * GET /api/v1/comments/latest?limit=10
         */
        get("/latest") {
            try {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val validLimit = when {
                    limit < 1 -> 10
                    limit > 100 -> 100
                    else -> limit
                }
                
                val latestComments = commentService.getLatestComments(validLimit)
                
                call.respond(HttpStatusCode.OK, ApiResponse.success(latestComments, "获取最新评论成功"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<List<Comment>>("获取最新评论失败: ${e.message}")
                )
            }
        }
        
        // ========== 需要认证的接口 ==========
        
        authenticate("auth-jwt") {
            
            /**
             * 创建评论
             * POST /api/v1/comments
             */
            post {
                try {
                    val userId = call.getUserId()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Comment>("用户未认证")
                        )
                    
                    val request = call.receive<CreateCommentRequest>()
                    
                    val comment = commentService.createComment(request, userId)
                    
                    call.respond(HttpStatusCode.Created, ApiResponse.success(comment, "评论创建成功"))
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Comment>(e.message ?: "请求参数无效")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Comment>("创建评论失败: ${e.message}")
                    )
                }
            }
            
            /**
             * 更新评论
             * PUT /api/v1/comments/{id}
             */
            put("/{id}") {
                try {
                    val userId = call.getUserId()
                        ?: return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Comment>("用户未认证")
                        )
                    
                    val commentId = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Comment>("无效的评论ID")
                        )
                    
                    val request = call.receive<UpdateCommentRequest>()
                    
                    val updatedComment = commentService.updateComment(commentId, request.content, userId)
                        ?: return@put call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Comment>("评论不存在或无权限修改")
                        )
                    
                    call.respond(HttpStatusCode.OK, ApiResponse.success(updatedComment, "评论更新成功"))
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Comment>(e.message ?: "请求参数无效")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Comment>("更新评论失败: ${e.message}")
                    )
                }
            }
            
            /**
             * 删除评论
             * DELETE /api/v1/comments/{id}
             */
            delete("/{id}") {
                try {
                    val userId = call.getUserId()
                        ?: return@delete call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>("用户未认证")
                        )
                    
                    val commentId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>("无效的评论ID")
                        )
                    
                    val deleted = commentService.deleteComment(commentId, userId)
                    
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse.success("评论删除成功")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>("评论不存在或无权限删除")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Unit>("删除评论失败: ${e.message}")
                    )
                }
            }
            
            /**
             * 获取当前用户的评论列表
             * GET /api/v1/comments/my?page=1&pageSize=20
             */
            get("/my") {
                try {
                    val userId = call.getUserId()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<PaginatedData<Comment>>("用户未认证")
                        )
                    
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                    
                    val validPage = maxOf(1, page)
                    val validPageSize = when {
                        pageSize < 1 -> 20
                        pageSize > 100 -> 100
                        else -> pageSize
                    }
                    
                    val comments = commentService.getCommentsByUserId(userId, validPage, validPageSize)
                    val totalCount = commentService.getCommentCountByUserId(userId)
                    val totalPages = (totalCount + validPageSize - 1) / validPageSize
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.paginated(
                            data = comments,
                            pagination = PaginationInfo(
                                currentPage = validPage,
                                pageSize = validPageSize,
                                totalCount = totalCount,
                                totalPages = totalPages.toInt()
                            ),
                            message = "获取用户评论列表成功"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<PaginatedData<Comment>>("获取用户评论列表失败: ${e.message}")
                    )
                }
            }
            
            /**
             * 管理员删除评论
             * DELETE /api/v1/comments/{id}/admin
             * 注意：这里简化处理，实际项目中应该有角色权限验证
             */
            delete("/{id}/admin") {
                try {
                    val userId = call.getUserId()
                        ?: return@delete call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>("用户未认证")
                        )
                    
                    // TODO: 添加管理员权限验证
                    // 这里简化处理，实际项目中应该验证用户是否为管理员
                    
                    val commentId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>("无效的评论ID")
                        )
                    
                    val deleted = commentService.adminDeleteComment(commentId)
                    
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse.success("评论删除成功")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>("评论不存在")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Unit>("删除评论失败: ${e.message}")
                    )
                }
            }
        }
    }
}

// ========== 请求和响应数据类 ==========

@Serializable
data class UpdateCommentRequest(
    val content: String
)