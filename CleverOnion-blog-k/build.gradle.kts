val kotlin_version: String by project
val ktor_version: String by project
val logback_version: String by project
val exposed_version: String by project
val postgresql_version: String by project
val hikari_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("io.ktor.plugin") version "3.2.1"
}

group = "com.cleveronion"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor服务器核心
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    
    // 序列化
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    
    // 认证和安全
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    
    // 状态页面和异常处理
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    
//    // 文件上传支持
//    implementation("io.ktor:ktor-server-multipart:$ktor_version")
    
    // 阿里云OSS SDK
    implementation("com.aliyun.oss:aliyun-sdk-oss:3.17.4")
    
    // 数据库相关
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:$postgresql_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    
    // 日志
    implementation("ch.qos.logback:logback-classic:$logback_version")
    
    // Markdown处理
    implementation("org.jetbrains:markdown:0.5.2")
    
    // 验证
    implementation("io.konform:konform:0.4.0")
    
    // 测试
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.mockk:mockk:1.13.8")
}