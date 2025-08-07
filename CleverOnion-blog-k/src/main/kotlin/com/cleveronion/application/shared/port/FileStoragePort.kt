package com.cleveronion.application.shared.port

/**
 * 文件存储端口
 * 
 * 定义文件存储操作的接口，由基础设施层实现具体的存储机制
 */
interface FileStoragePort {
    
    /**
     * 上传文件
     * 
     * @param fileName 文件名
     * @param fileContent 文件内容
     * @param contentType 内容类型
     * @return 文件的访问URL
     */
    suspend fun uploadFile(
        fileName: String,
        fileContent: ByteArray,
        contentType: String
    ): String
    
    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 删除是否成功
     */
    suspend fun deleteFile(fileUrl: String): Boolean
    
    /**
     * 检查文件是否存在
     * 
     * @param fileUrl 文件URL
     * @return 文件是否存在
     */
    suspend fun fileExists(fileUrl: String): Boolean
    
    /**
     * 获取文件信息
     * 
     * @param fileUrl 文件URL
     * @return 文件信息，如果文件不存在则返回null
     */
    suspend fun getFileInfo(fileUrl: String): FileInfo?
    
    /**
     * 生成预签名上传URL
     * 
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param expirationMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    suspend fun generatePresignedUploadUrl(
        fileName: String,
        contentType: String,
        expirationMinutes: Int = 60
    ): String
    
    /**
     * 生成预签名下载URL
     * 
     * @param fileUrl 文件URL
     * @param expirationMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    suspend fun generatePresignedDownloadUrl(
        fileUrl: String,
        expirationMinutes: Int = 60
    ): String
}

/**
 * 文件信息
 */
data class FileInfo(
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
    val lastModified: String,
    val url: String
)