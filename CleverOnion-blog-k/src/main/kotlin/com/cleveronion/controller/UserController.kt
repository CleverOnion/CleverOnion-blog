package com.cleveronion.controller

import com.cleveronion.domain.entity.*
import com.cleveronion.service.UserService
import com.cleveronion.getUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

fun Route.userRoutes() {
    val userService = UserService()
    
    route("/users") {
        
        // 获取所有用户（分页）- 需要认证
        authenticate("auth-jwt") {
            get {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                    
                    // 限制页面大小
                    val validPageSize = when {
                        pageSize < 1 -> 20
                        pageSize > 100 -> 100
                        else -> pageSize
                    }
                    
                    val users = userService.getAllUsers(page, validPageSize)
                    val totalCount = userService.getUserCount()
                    val totalPages = (totalCount + validPageSize - 1) / validPageSize
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.paginated(
                            data = users,
                            pagination = PaginationInfo(
                                currentPage = page,
                                pageSize = validPageSize,
                                totalCount = totalCount,
                                totalPages = totalPages.toInt()
                            ),
                            message = "获取用户列表成功"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<PaginatedData<com.cleveronion.domain.entity.User>>("获取用户列表失败: ${e.message}")
                    )
                }
            }
        }
        
        // 根据ID获取用户信息
        authenticate("auth-jwt") {
            get("/{id}") {
                try {
                    val userId = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<com.cleveronion.domain.entity.User>("无效的用户ID")
                        )
                    
                    val user = userService.findById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<com.cleveronion.domain.entity.User>("用户不存在")
                        )
                    
                    call.respond(HttpStatusCode.OK, ApiResponse.success(user, "获取用户信息成功"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<com.cleveronion.domain.entity.User>("获取用户信息失败: ${e.message}")
                    )
                }
            }
        }
        
        // 更新当前用户信息
        authenticate("auth-jwt") {
            put("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<String>("用户未认证")
                        )
                    
                    val currentUserId = principal.getUserId()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<String>("令牌中的用户ID无效")
                        )
                    
                    val updateRequest = call.receive<UpdateUserRequest>()
                    
                    // 验证当前用户存在
                    val currentUser = userService.findById(currentUserId)
                        ?: return@put call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<String>("用户不存在")
                        )
                    
                    // 这里可以添加更新用户信息的逻辑
                    // 由于当前的User实体主要来自GitHub，大部分信息不应该直接修改
                    // 可以考虑添加额外的用户配置表来存储可修改的信息
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success("用户资料更新功能即将推出")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<String>("请求格式无效")
                    )
                }
            }
        }
        
        // 删除用户（仅管理员）
        authenticate("auth-jwt") {
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@delete call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>("用户未认证")
                        )
                    
                    val currentUserId = principal.getUserId()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>("令牌中的用户ID无效")
                        )
                    
                    val targetUserId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>("无效的用户ID")
                        )
                    
                    // 防止用户删除自己
                    if (currentUserId == targetUserId) {
                        return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>("不能删除自己的账户")
                        )
                    }
                    
                    // 这里应该添加管理员权限检查
                    // 目前暂时允许任何认证用户删除其他用户（仅用于开发测试）
                    
                    val deleted = userService.deleteUser(targetUserId)
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse.success("用户删除成功")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>("用户不存在")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Unit>("删除用户失败: ${e.message}")
                    )
                }
            }
        }
        
        // 搜索用户
        authenticate("auth-jwt") {
            get("/search") {
                try {
                    val query = call.request.queryParameters["q"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<SearchData<com.cleveronion.domain.entity.User>>("搜索关键词不能为空")
                        )
                    
                    if (query.length < 2) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<SearchData<com.cleveronion.domain.entity.User>>("搜索关键词至少需要2个字符")
                        )
                    }
                    
                    // 这里可以实现用户搜索逻辑
                    // 目前返回空结果
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success(
                            SearchData(
                                keyword = query,
                                items = emptyList<com.cleveronion.domain.entity.User>(),
                                total = 0
                            ),
                            "用户搜索功能即将推出"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<SearchData<com.cleveronion.domain.entity.User>>("搜索失败: ${e.message}")
                    )
                }
            }
        }
    }
}


@Serializable
data class UpdateUserRequest(
    val name: String?,
    val bio: String?
)