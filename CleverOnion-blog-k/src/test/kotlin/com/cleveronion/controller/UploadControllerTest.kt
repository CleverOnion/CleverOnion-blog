package com.cleveronion.controller
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 文件上传控制器集成测试
 * 测试HTTP接口的功能和错误处理
 * 注意：这些测试需要配置有效的OSS环境变量或使用Mock
 */
class UploadControllerTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun `测试获取上传配置接口`() = testApplication {
        // 设置测试环境变量
        environment {
            config {
                put("oss.endpoint", "oss-cn-hangzhou.aliyuncs.com")
                put("oss.accessKeyId", "test_key")
                put("oss.accessKeySecret", "test_secret")
                put("oss.bucketName", "test_bucket")
                put("oss.maxFileSize", "10485760")
                put("oss.allowedExtensions", listOf("jpg", "jpeg", "png", "gif", "webp"))
                put("oss.urlPrefix", "https://test_bucket.oss-cn-hangzhou.aliyuncs.com")
            }
        }
        
        val response = client.get("/api/v1/upload/config")
        
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("maxFileSize"))
        assertTrue(responseText.contains("allowedExtensions"))
        assertTrue(responseText.contains("maxFileSizeMB"))
    }
    
    @Test
    fun `测试图片上传接口 - 未认证`() = testApplication {
        val response = client.submitFormWithBinaryData(
            url = "/api/v1/upload/image",
            formData = formData {
                append("file", "test content", Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"test.jpg\"")
                })
            }
        )
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun `测试图片上传接口 - 无文件`() = testApplication {
        // 这里需要添加JWT认证的Mock
        // 实际项目中需要配置测试用的JWT Token
        
        val response = client.submitFormWithBinaryData(
            url = "/api/v1/upload/image",
            formData = formData {
                // 不添加文件
            }
        ) {
            // 添加认证头（需要有效的JWT Token）
            // header(HttpHeaders.Authorization, "Bearer $testJwtToken")
        }
        
        // 由于没有认证，预期返回401
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun `测试删除图片接口 - 未认证`() = testApplication {
        val response = client.delete("/api/v1/upload/image/test.jpg")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    // 以下是一些辅助测试方法，用于验证响应格式
    
    // 注意：UploadResponse已被ApiResponse替代，相关测试已移除
}

/**
 * 测试工具类
 * 提供创建测试文件和JWT Token的辅助方法
 */
object UploadTestUtils {
    
    /**
     * 创建测试用的图片文件内容
     */
    fun createTestImageContent(): ByteArray {
        // 创建一个简单的测试图片内容
        // 实际项目中可以使用真实的图片文件
        return "fake image content".toByteArray()
    }
    
    /**
     * 创建测试用的JWT Token
     * 注意：这需要与实际的JWT配置保持一致
     */
    fun createTestJwtToken(): String {
        // 这里应该使用与应用相同的JWT配置来生成测试Token
        // 实际实现需要依赖JwtUtil类
        return "test.jwt.token"
    }
    
    /**
     * 验证文件名格式
     */
    fun isValidFileName(fileName: String): Boolean {
        val pattern = Regex("^images/\\d{4}/\\d{2}/\\d{2}/[a-f0-9]{32}\\.[a-z]+$")
        return pattern.matches(fileName)
    }
}