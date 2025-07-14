package com.cleveronion.controller

import com.cleveronion.domain.entity.*
import com.cleveronion.service.TagService
import com.cleveronion.getUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * 标签控制器
 * 提供标签管理的RESTful API接口
 * 遵循RESTful设计原则和单一职责原则
 */
fun Route.tagRoutes() {
    val tagService = TagService()
    
    route("/tags") {
        
        // ========== 公开接口（无需认证） ==========
        
        /**
         * 获取所有标签（分页）
         * GET /api/v1/tags?page=1&pageSize=50
         */
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 50
                
                val validPage = maxOf(1, page)
                val validPageSize = when {
                    pageSize < 1 -> 50
                    pageSize > 100 -> 100
                    else -> pageSize
                }
                
                val tags = tagService.getAllTags(validPage, validPageSize)
                val totalCount = tagService.getTagCount()
                val totalPages = (totalCount + validPageSize - 1) / validPageSize
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.paginated(
                        data = tags,
                        pagination = PaginationInfo(
                            currentPage = validPage,
                            pageSize = validPageSize,
                            totalCount = totalCount,
                            totalPages = totalPages.toInt()
                        ),
                        message = "获取标签列表成功"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<PaginatedData<Tag>>(
                        error = "获取标签列表失败",
                        message = e.message
                    )
                )
            }
        }
        
        /**
         * 根据ID获取标签详情
         * GET /api/v1/tags/{id}
         */
        get("/{id}") {
            try {
                val tagId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Tag>("无效的标签ID")
                    )
                
                val tag = tagService.findById(tagId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse.error<Tag>("标签不存在")
                    )
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(tag, "获取标签详情成功")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<Tag>(
                        error = "获取标签详情失败",
                        message = e.message
                    )
                )
            }
        }
        
        /**
         * 搜索标签
         * GET /api/v1/tags/search?keyword=关键词&limit=20
         */
        get("/search") {
            try {
                val keyword = call.request.queryParameters["keyword"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<SearchData<Tag>>("搜索关键词不能为空")
                    )
                
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val validLimit = when {
                    limit < 1 -> 20
                    limit > 100 -> 100
                    else -> limit
                }
                
                val tags = tagService.searchTags(keyword.trim(), validLimit)
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(
                        SearchData(
                            keyword = keyword,
                            items = tags,
                            total = tags.size
                        ),
                        "搜索完成"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<SearchData<Tag>>(
                        error = "搜索标签失败",
                        message = e.message
                    )
                )
            }
        }
        
        /**
         * 获取热门标签
         * GET /api/v1/tags/popular?limit=10
         */
        get("/popular") {
            try {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val validLimit = when {
                    limit < 1 -> 10
                    limit > 50 -> 50
                    else -> limit
                }
                
                val popularTags = tagService.getPopularTags(validLimit)
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(popularTags, "获取热门标签成功")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<List<Tag>>(
                        error = "获取热门标签失败",
                        message = e.message
                    )
                )
            }
        }
        
        // ========== 需要认证的接口 ==========
        
        authenticate("auth-jwt") {
            
            /**
             * 创建标签
             * POST /api/v1/tags
             */
            post {
                try {
                    val userId = call.getUserId()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Tag>("用户未认证")
                        )
                    
                    val request = call.receive<CreateTagRequest>()
                    
                    val tag = tagService.createTag(request)
                    
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse.success(tag, "标签创建成功")
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Tag>(
                            error = "请求参数无效",
                            message = e.message
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Tag>(
                            error = "创建标签失败",
                            message = e.message
                        )
                    )
                }
            }
            
            /**
             * 更新标签
             * PUT /api/v1/tags/{id}
             */
            put("/{id}") {
                try {
                    val userId = call.getUserId()
                        ?: return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Tag>("用户未认证")
                        )
                    
                    val tagId = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Tag>("无效的标签ID")
                        )
                    
                    val request = call.receive<UpdateTagRequest>()
                    
                    val updatedTag = tagService.updateTag(tagId, request.name)
                        ?: return@put call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Tag>("标签不存在")
                        )
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success(updatedTag, "标签更新成功")
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Tag>(
                            error = "请求参数无效",
                            message = e.message
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Tag>(
                            error = "更新标签失败",
                            message = e.message
                        )
                    )
                }
            }
            
            /**
             * 删除标签
             * DELETE /api/v1/tags/{id}
             */
            delete("/{id}") {
                try {
                    val userId = call.getUserId()
                        ?: return@delete call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>("用户未认证")
                        )
                    
                    val tagId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>("无效的标签ID")
                        )
                    
                    val deleted = tagService.deleteTag(tagId)
                    
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse.success("标签删除成功")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>("标签不存在")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Unit>(
                            error = "删除标签失败",
                            message = e.message
                        )
                    )
                }
            }
            
            /**
             * 批量创建标签
             * POST /api/v1/tags/batch
             */
            post("/batch") {
                try {
                    val userId = call.getUserId()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<BatchOperationData<Tag>>("用户未认证")
                        )
                    
                    val request = call.receive<BatchCreateTagsRequest>()
                    
                    if (request.names.isEmpty()) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<BatchOperationData<Tag>>("标签名称列表不能为空")
                        )
                    }
                    
                    if (request.names.size > 50) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<BatchOperationData<Tag>>("一次最多只能创建50个标签")
                        )
                    }
                    
                    val tags = tagService.createTagsIfNotExists(request.names)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success(
                            BatchOperationData(
                                items = tags,
                                total = tags.size,
                                success = tags.size
                            ),
                            "批量创建标签成功"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<BatchOperationData<Tag>>(
                            error = "批量创建标签失败",
                            message = e.message
                        )
                    )
                }
            }
        }
    }
}

// ========== 请求数据类 ==========

@Serializable
data class UpdateTagRequest(
    val name: String
)

@Serializable
data class BatchCreateTagsRequest(
    val names: List<String>
)