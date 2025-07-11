# 图片上传 API 文档

## 概述

本文档描述了 CleverOnion Blog 系统的图片上传功能，该功能基于阿里云 OSS 存储服务实现，支持安全、高效的图片文件上传和管理。

## 技术架构

### 设计原则
- **单一职责原则**：每个类专注于特定的功能
- **依赖倒置原则**：通过接口抽象依赖关系
- **开闭原则**：支持扩展新的存储服务实现
- **策略模式**：可以轻松切换不同的文件存储策略

### 核心组件

1. **FileUploadService** - 文件上传服务接口
2. **OssFileUploadService** - 阿里云 OSS 实现
3. **UploadController** - HTTP 接口控制器
4. **OssConfig** - OSS 配置管理

## API 接口

### 基础信息

- **Base URL**: `http://localhost:8080/api/v1`
- **认证方式**: JWT Bearer Token
- **内容类型**: `multipart/form-data` (上传), `application/json` (响应)

### 1. 上传图片

**接口地址**: `POST /upload/image`

**认证要求**: 需要 JWT Token

**请求格式**: `multipart/form-data`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 图片文件 |

**支持的文件格式**:
- JPG/JPEG
- PNG
- GIF
- WebP

**文件大小限制**: 最大 10MB

**请求示例**:

```bash
curl -X POST \
  http://localhost:8080/api/v1/upload/image \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/your/image.jpg'
```

**成功响应** (200 OK):

```json
{
  "success": true,
  "message": "图片上传成功",
  "data": {
    "fileName": "images/2024/01/15/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6.jpg",
    "originalFileName": "my-photo.jpg",
    "fileSize": 1048576,
    "fileType": "image/jpeg",
    "url": "https://your-bucket.oss-cn-hangzhou.aliyuncs.com/images/2024/01/15/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6.jpg",
    "uploadTime": "2024-01-15T10:30:45Z"
  }
}
```

**错误响应**:

```json
{
  "success": false,
  "message": "文件大小超过限制，最大允许: 10MB"
}
```

### 2. 删除图片

**接口地址**: `DELETE /upload/image/{fileName}`

**认证要求**: 需要 JWT Token

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileName | String | 是 | 文件名（从上传响应中获取） |

**请求示例**:

```bash
curl -X DELETE \
  http://localhost:8080/api/v1/upload/image/images/2024/01/15/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6.jpg \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

**成功响应** (200 OK):

```json
{
  "success": true,
  "message": "文件删除成功"
}
```

### 3. 获取上传配置

**接口地址**: `GET /upload/config`

**认证要求**: 无（公开接口）

**请求示例**:

```bash
curl -X GET http://localhost:8080/api/v1/upload/config
```

**响应示例**:

```json
{
  "maxFileSize": 10485760,
  "allowedExtensions": ["jpg", "jpeg", "png", "gif", "webp"],
  "maxFileSizeMB": 10
}
```

## 错误码说明

| 错误码 | HTTP状态码 | 说明 |
|--------|------------|------|
| FILE_TOO_LARGE | 400 | 文件大小超过限制 |
| INVALID_FILE_TYPE | 400 | 不支持的文件类型 |
| NO_FILE_PROVIDED | 400 | 未提供文件 |
| INVALID_FILE_NAME | 400 | 无效的文件名 |
| UPLOAD_FAILED | 400 | 文件上传失败 |
| OSS_CONFIG_ERROR | 500 | OSS配置错误 |
| NETWORK_ERROR | 500 | 网络连接错误 |

## 配置说明

### 环境变量

在生产环境中，需要设置以下环境变量：

```bash
# 阿里云OSS配置
export OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"
export OSS_ACCESS_KEY_ID="your_access_key_id"
export OSS_ACCESS_KEY_SECRET="your_access_key_secret"
export OSS_BUCKET_NAME="your_bucket_name"
export OSS_URL_PREFIX="https://your_bucket_name.oss-cn-hangzhou.aliyuncs.com"
```

### application.yaml 配置

```yaml
oss:
  endpoint: "${OSS_ENDPOINT:oss-cn-hangzhou.aliyuncs.com}"
  accessKeyId: "${OSS_ACCESS_KEY_ID:your_access_key_id}"
  accessKeySecret: "${OSS_ACCESS_KEY_SECRET:your_access_key_secret}"
  bucketName: "${OSS_BUCKET_NAME:your_bucket_name}"
  maxFileSize: 10485760 # 10MB
  allowedExtensions: ["jpg", "jpeg", "png", "gif", "webp"]
  urlPrefix: "${OSS_URL_PREFIX:https://your_bucket_name.oss-cn-hangzhou.aliyuncs.com}"
```

## 前端集成示例

### JavaScript/TypeScript

```javascript
// 上传图片
async function uploadImage(file, token) {
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const response = await fetch('/api/v1/upload/image', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log('上传成功:', result.data.url);
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('上传失败:', error.message);
    throw error;
  }
}

// 获取上传配置
async function getUploadConfig() {
  try {
    const response = await fetch('/api/v1/upload/config');
    const config = await response.json();
    return config;
  } catch (error) {
    console.error('获取配置失败:', error);
    throw error;
  }
}

// 使用示例
const fileInput = document.getElementById('file-input');
fileInput.addEventListener('change', async (event) => {
  const file = event.target.files[0];
  if (file) {
    try {
      const config = await getUploadConfig();
      
      // 验证文件大小
      if (file.size > config.maxFileSize) {
        alert(`文件大小不能超过 ${config.maxFileSizeMB}MB`);
        return;
      }
      
      // 验证文件类型
      const extension = file.name.split('.').pop().toLowerCase();
      if (!config.allowedExtensions.includes(extension)) {
        alert(`不支持的文件类型，支持: ${config.allowedExtensions.join(', ')}`);
        return;
      }
      
      // 上传文件
      const result = await uploadImage(file, 'your_jwt_token');
      console.log('图片URL:', result.url);
      
    } catch (error) {
      alert('上传失败: ' + error.message);
    }
  }
});
```

### React 组件示例

```jsx
import React, { useState, useCallback } from 'react';

const ImageUpload = ({ onUploadSuccess, token }) => {
  const [uploading, setUploading] = useState(false);
  const [config, setConfig] = useState(null);
  
  // 获取上传配置
  useEffect(() => {
    fetch('/api/v1/upload/config')
      .then(res => res.json())
      .then(setConfig)
      .catch(console.error);
  }, []);
  
  const handleFileChange = useCallback(async (event) => {
    const file = event.target.files[0];
    if (!file || !config) return;
    
    // 文件验证
    if (file.size > config.maxFileSize) {
      alert(`文件大小不能超过 ${config.maxFileSizeMB}MB`);
      return;
    }
    
    const extension = file.name.split('.').pop().toLowerCase();
    if (!config.allowedExtensions.includes(extension)) {
      alert(`不支持的文件类型`);
      return;
    }
    
    setUploading(true);
    
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await fetch('/api/v1/upload/image', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });
      
      const result = await response.json();
      
      if (result.success) {
        onUploadSuccess(result.data);
      } else {
        alert(result.message);
      }
    } catch (error) {
      alert('上传失败: ' + error.message);
    } finally {
      setUploading(false);
    }
  }, [config, token, onUploadSuccess]);
  
  return (
    <div>
      <input
        type="file"
        accept="image/*"
        onChange={handleFileChange}
        disabled={uploading}
      />
      {uploading && <p>上传中...</p>}
    </div>
  );
};

export default ImageUpload;
```

## 安全考虑

1. **认证授权**: 所有上传和删除操作都需要有效的 JWT Token
2. **文件类型验证**: 严格限制允许上传的文件类型
3. **文件大小限制**: 防止大文件攻击
4. **文件名安全**: 使用 UUID 生成唯一文件名，避免路径遍历攻击
5. **访问控制**: OSS Bucket 配置适当的访问权限

## 性能优化

1. **CDN 加速**: 配置阿里云 CDN 加速图片访问
2. **图片压缩**: 可以在上传前进行客户端压缩
3. **缓存策略**: 设置适当的 HTTP 缓存头
4. **异步处理**: 使用协程处理文件上传，避免阻塞

## 监控和日志

系统会记录以下关键事件：
- 文件上传成功/失败
- 文件删除操作
- 配置错误
- 网络异常

建议在生产环境中配置适当的日志级别和监控告警。