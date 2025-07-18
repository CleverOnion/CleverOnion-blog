package com.cleveronion.controller

import com.cleveronion.domain.entity.*
import com.cleveronion.service.ArticleService
import com.cleveronion.getUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * 文章控制器
 * 提供RESTful API接口，处理HTTP请求和响应
 * 遵循单一职责原则，专注于HTTP层的处理
 */
fun Route.articleRoutes() {
    val articleService = ArticleService()
    
    route("/articles") {
        
        // ========== 公开接口（无需认证） ==========
        
        /**
         * 获取已发布文章列表（分页）
         * GET /api/v1/articles?page=1&pageSize=20&authorId=123
         */
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                val authorId = call.request.queryParameters["authorId"]?.toLongOrNull()
                
                // 参数验证
                val validPage = maxOf(1, page)
                val validPageSize = when {
                    pageSize < 1 -> 20
                    pageSize > 100 -> 100
                    else -> pageSize
                }
                
                // 只返回已发布的文章
                val articles = articleService.getArticles(
                    page = validPage,
                    pageSize = validPageSize,
                    status = ArticleStatus.PUBLISHED,
                    authorId = authorId
                )
                
                val totalCount = articleService.getArticleCount(
                    status = ArticleStatus.PUBLISHED,
                    authorId = authorId
                )
                val totalPages = (totalCount + validPageSize - 1) / validPageSize
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.paginated(
                        data = articles,
                        pagination = PaginationInfo(
                            currentPage = validPage,
                            pageSize = validPageSize,
                            totalCount = totalCount,
                            totalPages = totalPages.toInt()
                        ),
                        message = "获取文章列表成功"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<PaginatedData<Article>>(
                        error = "获取文章列表失败",
                        message = e.message
                    )
                )
            }
        }
        
        /**
         * 根据ID获取文章详情（自动增加浏览量）
         * GET /api/v1/articles/{id}
         */
        get("/{id}") {
            try {
                val articleId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Article>("无效的文章ID")
                    )
                
                val article = articleService.findById(articleId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse.error<Article>("文章不存在")
                    )
                
                // 只有已发布的文章才能被公开访问
                if (article.status != ArticleStatus.PUBLISHED.name) {
                    return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse.error<Article>("文章不存在")
                    )
                }
                
                // 增加浏览量
                articleService.incrementViewCount(articleId)
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(article, "获取文章详情成功")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<Article>(
                        error = "获取文章详情失败",
                        message = e.message
                    )
                )
            }
        }
        
        /**
         * 搜索文章
         * GET /api/v1/articles/search?keyword=关键词&page=1&pageSize=20
         */
        get("/search") {
            try {
                val keyword = call.request.queryParameters["keyword"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<SearchData<Article>>("搜索关键词不能为空")
                    )
                
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                
                val validPage = maxOf(1, page)
                val validPageSize = when {
                    pageSize < 1 -> 20
                    pageSize > 100 -> 100
                    else -> pageSize
                }
                
                val articles = articleService.searchArticles(
                    keyword = keyword.trim(),
                    page = validPage,
                    pageSize = validPageSize
                )
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(
                        SearchData(
                            keyword = keyword,
                            items = articles,
                            total = articles.size
                        ),
                        "搜索完成"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<SearchData<Article>>(
                        error = "搜索失败",
                        message = e.message
                    )
                )
            }
        }
        
        // ========== 需要认证的接口 ==========
        
        authenticate("auth-jwt") {
            
            /**
             * 创建文章
             * POST /api/v1/articles
             */
            post {
                try {
                    val userId = call.getUserId()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Article>("用户未认证")
                        )
                    
                    val request = call.receive<CreateArticleRequest>()
                    
                    val article = articleService.createArticle(userId, request)
                    
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse.success(article, "文章创建成功")
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Article>(
                            error = "请求参数无效",
                            message = e.message
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Article>(
                            error = "创建文章失败",
                            message = e.message
                        )
                    )
                }
            }
            
            /**
             * 更新文章
             * PUT /api/v1/articles/{id}
             */
            put("/{id}") {
                try {
                    val userId = call.getUserId()
                        ?: return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Article>("用户未认证")
                        )
                    
                    val articleId = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Article>("无效的文章ID")
                        )
                    
                    val request = call.receive<UpdateArticleRequest>()
                    
                    val updatedArticle = articleService.updateArticle(articleId, userId, request)
                        ?: return@put call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Article>("文章不存在或无权限修改")
                        )
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success(updatedArticle, "文章更新成功")
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Article>(
                            error = "请求参数无效",
                            message = e.message
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Article>(
                            error = "更新文章失败",
                            message = e.message
                        )
                    )
                }
            }
            
            /**
             * 删除文章
             * DELETE /api/v1/articles/{id}
             */
            delete("/{id}") {
                try {
                    val userId = call.getUserId()
                        ?: return@delete call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>("用户未认证")
                        )
                    
                    val articleId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>("无效的文章ID")
                        )
                    
                    val deleted = articleService.deleteArticle(articleId, userId)
                    
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse.success("文章删除成功")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>("文章不存在或无权限删除")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Unit>(
                            error = "删除文章失败",
                            message = e.message
                        )
                    )
                }
            }
            
            /**
             * 获取当前用户的文章列表（包括草稿）
             * GET /api/v1/articles/my?page=1&pageSize=20&status=DRAFT
             */
            get("/my") {
                try {
                    val userId = call.getUserId()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<PaginatedData<Article>>("用户未认证")
                        )
                    
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                    val statusParam = call.request.queryParameters["status"]
                    
                    val validPage = maxOf(1, page)
                    val validPageSize = when {
                        pageSize < 1 -> 20
                        pageSize > 100 -> 100
                        else -> pageSize
                    }
                    
                    val status = statusParam?.let {
                        try {
                            ArticleStatus.valueOf(it.uppercase())
                        } catch (e: IllegalArgumentException) {
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<PaginatedData<Article>>("无效的文章状态: $it")
                            )
                        }
                    }
                    
                    val articles = articleService.getArticles(
                        page = validPage,
                        pageSize = validPageSize,
                        status = status,
                        authorId = userId
                    )
                    
                    val totalCount = articleService.getArticleCount(
                        status = status,
                        authorId = userId
                    )
                    val totalPages = (totalCount + validPageSize - 1) / validPageSize
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.paginated(
                            data = articles,
                            pagination = PaginationInfo(
                                currentPage = validPage,
                                pageSize = validPageSize,
                                totalCount = totalCount,
                                totalPages = totalPages.toInt()
                            ),
                            message = "获取我的文章列表成功"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<PaginatedData<Article>>("获取文章列表失败: ${e.message}")
                    )
                }
            }
            
            /**
             * 获取文章详情（作者可以查看自己的草稿）
             * GET /api/v1/articles/{id}/preview
             */
            get("/{id}/preview") {
                try {
                    val userId = call.getUserId()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Article>("用户未认证")
                        )
                    
                    val articleId = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Article>("无效的文章ID")
                        )
                    
                    val article = articleService.findById(articleId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Article>("文章不存在")
                        )
                    
                    // 只有作者可以预览自己的文章
                    if (article.authorId != userId) {
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            ApiResponse.error<Article>("无权限访问此文章")
                        )
                    }
                    
                    call.respond(HttpStatusCode.OK, ApiResponse.success(article, "获取文章详情成功"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Article>("获取文章详情失败: ${e.message}")
                    )
                }
            }
        }
    }
}