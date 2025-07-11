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
                        UserListResponse(
                            users = users,
                            pagination = PaginationInfo(
                                currentPage = page,
                                pageSize = validPageSize,
                                totalCount = totalCount,
                                totalPages = totalPages.toInt()
                            )
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to fetch users: ${e.message}")
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
                            mapOf("error" to "Invalid user ID")
                        )
                    
                    val user = userService.findById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "User not found")
                        )
                    
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to fetch user: ${e.message}")
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
                            mapOf("error" to "Invalid token")
                        )
                    
                    val currentUserId = principal.getUserId()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID in token")
                        )
                    
                    val updateRequest = call.receive<UpdateUserRequest>()
                    
                    // 验证当前用户存在
                    val currentUser = userService.findById(currentUserId)
                        ?: return@put call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "User not found")
                        )
                    
                    // 这里可以添加更新用户信息的逻辑
                    // 由于当前的User实体主要来自GitHub，大部分信息不应该直接修改
                    // 可以考虑添加额外的用户配置表来存储可修改的信息
                    
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "User profile update feature coming soon")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request format")
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
                            mapOf("error" to "Invalid token")
                        )
                    
                    val currentUserId = principal.getUserId()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID in token")
                        )
                    
                    val targetUserId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID")
                        )
                    
                    // 防止用户删除自己
                    if (currentUserId == targetUserId) {
                        return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Cannot delete your own account")
                        )
                    }
                    
                    // 这里应该添加管理员权限检查
                    // 目前暂时允许任何认证用户删除其他用户（仅用于开发测试）
                    
                    val deleted = userService.deleteUser(targetUserId)
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            mapOf("message" to "User deleted successfully")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "User not found")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to delete user: ${e.message}")
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
                            mapOf("error" to "Search query is required")
                        )
                    
                    if (query.length < 2) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Search query must be at least 2 characters")
                        )
                    }
                    
                    // 这里可以实现用户搜索逻辑
                    // 目前返回空结果
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "users" to emptyList<Any>(),
                            "message" to "User search feature coming soon"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Search failed: ${e.message}")
                    )
                }
            }
        }
    }
}

@Serializable
data class UserListResponse(
    val users: List<com.cleveronion.domain.entity.User>,
    val pagination: PaginationInfo
)



@Serializable
data class UpdateUserRequest(
    val name: String?,
    val bio: String?
)