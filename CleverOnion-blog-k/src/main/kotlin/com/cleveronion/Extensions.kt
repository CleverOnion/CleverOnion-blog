package com.cleveronion

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/**
 * ApplicationCall扩展函数
 * 提供便捷的方法来获取当前认证用户的信息
 */

/**
 * 从当前请求中获取认证用户的ID
 * @return 用户ID，如果未认证或Token无效则返回null
 */
fun ApplicationCall.getUserId(): Long? {
    return principal<JWTPrincipal>()?.getUserId()
}

/**
 * 从当前请求中获取认证用户的邮箱
 * @return 用户邮箱，如果未认证或Token无效则返回null
 */
fun ApplicationCall.getUserEmail(): String? {
    return principal<JWTPrincipal>()?.getEmail()
}

/**
 * 从当前请求中获取认证用户的用户名
 * @return 用户名，如果未认证或Token无效则返回null
 */
fun ApplicationCall.getUsername(): String? {
    return principal<JWTPrincipal>()?.getUsername()
}

/**
 * 检查当前用户是否已认证
 * @return 是否已认证
 */
fun ApplicationCall.isAuthenticated(): Boolean {
    return principal<JWTPrincipal>() != null
}