package com.cleveronion

import com.cleveronion.domain.entity.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<Unit>(
                    error = "Internal Server Error",
                    message = cause.localizedMessage ?: "Unknown error occurred"
                )
            )
        }
        
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiResponse.error<Unit>(
                    error = "Not Found",
                    message = "The requested resource was not found"
                )
            )
        }
    }
}