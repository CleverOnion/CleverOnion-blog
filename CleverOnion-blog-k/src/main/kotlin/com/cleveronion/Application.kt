package com.cleveronion

import com.cleveronion.config.configureDatabases
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureHTTP()
    configureSerialization()
    configureAuthentication()
    configureMonitoring()
    configureStatusPages()
    configureRouting()
}