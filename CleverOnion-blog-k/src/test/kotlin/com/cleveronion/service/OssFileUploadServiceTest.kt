package com.cleveronion.service

import com.cleveronion.config.OssConfig
import com.cleveronion.domain.entity.FileInfo
import com.cleveronion.domain.entity.UploadError
import com.cleveronion.domain.entity.UploadException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * OSS文件上传服务测试类
 * 测试文件上传、验证等核心功能
 * 使用模拟配置进行单元测试
 */
class OssFileUploadServiceTest {
    
    private lateinit var ossConfig: OssConfig
    private lateinit var uploadService: OssFileUploadService
    
    @BeforeEach
    fun setUp() {
        // 创建测试用的OSS配置
        ossConfig = OssConfig(
            endpoint = "oss-cn-hangzhou.aliyuncs.com",
            accessKeyId = "test_access_key",
            accessKeySecret = "test_access_secret",
            bucketName = "test_bucket",
            maxFileSize = 10 * 1024 * 1024, // 10MB
            allowedExtensions = listOf("jpg", "jpeg", "png", "gif", "webp"),
            urlPrefix = "https://test_bucket.oss-cn-hangzhou.aliyuncs.com"
        )
        
        uploadService = OssFileUploadService(ossConfig)
    }
    
    @Test
    fun `测试OSS配置验证`() {
        // 测试有效配置
        assertTrue(ossConfig.validate())
        
        // 测试无效配置
        val invalidConfig = OssConfig(
            endpoint = "",
            accessKeyId = "test",
            accessKeySecret = "test",
            bucketName = "test",
            maxFileSize = 0,
            allowedExtensions = emptyList(),
            urlPrefix = ""
        )
        assertFalse(invalidConfig.validate())
    }
    
    @Test
    fun `测试文件扩展名验证`() {
        assertTrue(ossConfig.isExtensionAllowed("jpg"))
        assertTrue(ossConfig.isExtensionAllowed("PNG")) // 大小写不敏感
        assertFalse(ossConfig.isExtensionAllowed("txt"))
        assertFalse(ossConfig.isExtensionAllowed("exe"))
    }
    
    @Test
    fun `测试文件大小验证`() {
        assertTrue(ossConfig.isFileSizeAllowed(1024)) // 1KB
        assertTrue(ossConfig.isFileSizeAllowed(5 * 1024 * 1024)) // 5MB
        assertFalse(ossConfig.isFileSizeAllowed(15 * 1024 * 1024)) // 15MB
    }
    
    @Test
    fun `测试文件信息验证 - 有效文件`() {
        val validFileInfo = FileInfo(
            originalFileName = "test.jpg",
            fileName = "test.jpg",
            fileSize = 1024,
            contentType = "image/jpeg",
            extension = "jpg",
            content = ByteArray(1024)
        )
        
        assertTrue(uploadService.validateFile(validFileInfo))
    }
    
    @Test
    fun `测试文件信息验证 - 文件过大`() {
        val largeFileInfo = FileInfo(
            originalFileName = "large.jpg",
            fileName = "large.jpg",
            fileSize = 15 * 1024 * 1024, // 15MB
            contentType = "image/jpeg",
            extension = "jpg",
            content = ByteArray(1024)
        )
        
        assertFalse(uploadService.validateFile(largeFileInfo))
    }
    
    @Test
    fun `测试文件信息验证 - 不支持的文件类型`() {
        val invalidTypeFileInfo = FileInfo(
            originalFileName = "test.txt",
            fileName = "test.txt",
            fileSize = 1024,
            contentType = "text/plain",
            extension = "txt",
            content = ByteArray(1024)
        )
        
        assertFalse(uploadService.validateFile(invalidTypeFileInfo))
    }
    
    @Test
    fun `测试文件信息验证 - 空文件名`() {
        val emptyNameFileInfo = FileInfo(
            originalFileName = "",
            fileName = "",
            fileSize = 1024,
            contentType = "image/jpeg",
            extension = "jpg",
            content = ByteArray(1024)
        )
        
        assertFalse(uploadService.validateFile(emptyNameFileInfo))
    }
    
    @Test
    fun `测试获取文件URL`() {
        val fileName = "images/2024/01/01/test.jpg"
        val expectedUrl = "https://test_bucket.oss-cn-hangzhou.aliyuncs.com/images/2024/01/01/test.jpg"
        
        assertEquals(expectedUrl, uploadService.getFileUrl(fileName))
    }
    
    @Test
    fun `测试获取文件URL - URL前缀带斜杠`() {
        val configWithSlash = ossConfig.copy(
            urlPrefix = "https://test_bucket.oss-cn-hangzhou.aliyuncs.com/"
        )
        val serviceWithSlash = OssFileUploadService(configWithSlash)
        
        val fileName = "images/test.jpg"
        val expectedUrl = "https://test_bucket.oss-cn-hangzhou.aliyuncs.com/images/test.jpg"
        
        assertEquals(expectedUrl, serviceWithSlash.getFileUrl(fileName))
    }
    
    // 注意：以下测试需要真实的OSS环境，在CI/CD中可能需要跳过
    // 或者使用Mock来模拟OSS客户端的行为
    
    /*
    @Test
    fun `测试文件上传 - 成功场景`() = runBlocking {
        val fileInfo = FileInfo(
            originalFileName = "test.jpg",
            fileName = "test.jpg",
            fileSize = 1024,
            contentType = "image/jpeg",
            extension = "jpg",
            content = ByteArray(1024) { it.toByte() }
        )
        
        val result = uploadService.uploadFile(fileInfo)
        
        assertEquals(fileInfo.originalFileName, result.originalFileName)
        assertEquals(fileInfo.fileSize, result.fileSize)
        assertEquals(fileInfo.contentType, result.fileType)
        assertTrue(result.url.isNotBlank())
        assertTrue(result.fileName.isNotBlank())
    }
    
    @Test
    fun `测试文件上传 - 无效文件`() = runBlocking {
        val invalidFileInfo = FileInfo(
            originalFileName = "test.txt",
            fileName = "test.txt",
            fileSize = 1024,
            contentType = "text/plain",
            extension = "txt",
            content = ByteArray(1024)
        )
        
        assertThrows<UploadException> {
            uploadService.uploadFile(invalidFileInfo)
        }
    }
    */
}