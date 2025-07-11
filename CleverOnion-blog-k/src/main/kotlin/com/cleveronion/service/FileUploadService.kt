package com.cleveronion.service

import com.cleveronion.domain.entity.FileInfo
import com.cleveronion.domain.entity.UploadData

/**
 * 文件上传服务接口
 * 定义文件上传的抽象契约，遵循依赖倒置原则
 * 支持策略模式，可以有多种实现（OSS、本地存储等）
 */
interface FileUploadService {
    
    /**
     * 上传文件
     * @param fileInfo 文件信息
     * @return 上传结果数据
     * @throws UploadException 上传失败时抛出异常
     */
    suspend fun uploadFile(fileInfo: FileInfo): UploadData
    
    /**
     * 删除文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    suspend fun deleteFile(fileName: String): Boolean
    
    /**
     * 检查文件是否存在
     * @param fileName 文件名
     * @return 文件是否存在
     */
    suspend fun fileExists(fileName: String): Boolean
    
    /**
     * 获取文件访问URL
     * @param fileName 文件名
     * @return 文件访问URL
     */
    fun getFileUrl(fileName: String): String
    
    /**
     * 验证文件信息
     * @param fileInfo 文件信息
     * @return 验证是否通过
     */
    fun validateFile(fileInfo: FileInfo): Boolean
}