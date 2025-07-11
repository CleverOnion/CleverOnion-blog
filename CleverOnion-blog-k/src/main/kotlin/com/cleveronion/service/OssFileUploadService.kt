package com.cleveronion.service

import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.model.ObjectMetadata
import com.cleveronion.config.OssConfig
import com.cleveronion.domain.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 阿里云OSS文件上传服务实现
 * 实现FileUploadService接口，提供OSS存储功能
 * 遵循单一职责原则，专注于OSS文件操作
 */
class OssFileUploadService(private val ossConfig: OssConfig) : FileUploadService {
    
    private val ossClient: OSS by lazy {
        OSSClientBuilder().build(ossConfig.endpoint, ossConfig.accessKeyId, ossConfig.accessKeySecret)
    }
    
    /**
     * 上传文件到阿里云OSS
     * 使用协程处理IO操作，避免阻塞主线程
     */
    override suspend fun uploadFile(fileInfo: FileInfo): UploadData = withContext(Dispatchers.IO) {
        try {
            // 验证文件
            if (!validateFile(fileInfo)) {
                throw UploadException(UploadError.INVALID_FILE_TYPE)
            }
            
            // 生成唯一文件名，避免文件名冲突
            val uniqueFileName = generateUniqueFileName(fileInfo.originalFileName, fileInfo.extension)
            
            // 设置文件元数据
            val metadata = ObjectMetadata().apply {
                contentLength = fileInfo.fileSize
                contentType = fileInfo.contentType
                // 设置缓存控制，提高访问性能
                cacheControl = "max-age=31536000" // 1年
                // 设置文件描述信息
                addUserMetadata("original-name", fileInfo.originalFileName)
                addUserMetadata("upload-time", Instant.now().toString())
            }
            
            // 上传文件到OSS
            val inputStream = ByteArrayInputStream(fileInfo.content)
            val putResult = ossClient.putObject(ossConfig.bucketName, uniqueFileName, inputStream, metadata)
            
            // 验证上传结果
            if (putResult.eTag.isNullOrBlank()) {
                throw UploadException(UploadError.UPLOAD_FAILED, "OSS返回的ETag为空")
            }
            
            // 构建返回数据
            UploadData(
                fileName = uniqueFileName,
                originalFileName = fileInfo.originalFileName,
                fileSize = fileInfo.fileSize,
                fileType = fileInfo.contentType,
                url = getFileUrl(uniqueFileName),
                uploadTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
            
        } catch (e: UploadException) {
            throw e
        } catch (e: Exception) {
            throw UploadException(
                UploadError.UPLOAD_FAILED,
                "上传到OSS失败: ${e.message}",
                e
            )
        }
    }
    
    /**
     * 从OSS删除文件
     */
    override suspend fun deleteFile(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ossClient.deleteObject(ossConfig.bucketName, fileName)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查文件是否存在于OSS
     */
    override suspend fun fileExists(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ossClient.doesObjectExist(ossConfig.bucketName, fileName)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取文件的访问URL
     * 使用配置的URL前缀构建完整的访问地址
     */
    override fun getFileUrl(fileName: String): String {
        return "${ossConfig.urlPrefix.trimEnd('/')}/$fileName"
    }
    
    /**
     * 验证文件信息
     * 检查文件大小和类型是否符合要求
     */
    override fun validateFile(fileInfo: FileInfo): Boolean {
        // 检查文件大小
        if (!ossConfig.isFileSizeAllowed(fileInfo.fileSize)) {
            return false
        }
        
        // 检查文件扩展名
        if (!ossConfig.isExtensionAllowed(fileInfo.extension)) {
            return false
        }
        
        // 检查文件名是否有效
        if (fileInfo.originalFileName.isBlank() || fileInfo.fileName.isBlank()) {
            return false
        }
        
        return true
    }
    
    /**
     * 生成唯一文件名
     * 使用时间戳和UUID确保文件名唯一性
     * 格式: images/yyyy/MM/dd/uuid.extension
     */
    private fun generateUniqueFileName(originalFileName: String, extension: String): String {
        val now = Instant.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val datePath = formatter.format(now.atZone(java.time.ZoneId.systemDefault()))
        val uuid = UUID.randomUUID().toString().replace("-", "")
        
        return "images/$datePath/$uuid.$extension"
    }
    
    /**
     * 关闭OSS客户端连接
     * 在应用关闭时调用，释放资源
     */
    fun close() {
        try {
            ossClient.shutdown()
        } catch (e: Exception) {
            // 忽略关闭异常
        }
    }
}