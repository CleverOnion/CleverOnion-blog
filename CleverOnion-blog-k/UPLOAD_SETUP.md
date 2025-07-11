# 图片上传功能配置指南

## 概述

本指南将帮助您在 CleverOnion Blog 项目中配置和使用图片上传功能。该功能基于阿里云 OSS 存储服务，提供安全、高效的图片存储和访问。

## 功能特性

✅ **高质量代码设计**
- 遵循 SOLID 原则
- 使用策略模式和依赖注入
- 完整的单元测试和集成测试
- 详细的文档注释

✅ **安全性**
- JWT 认证保护
- 文件类型和大小验证
- 唯一文件名生成
- 防止路径遍历攻击

✅ **性能优化**
- 异步文件处理
- 协程支持
- 合理的缓存策略

✅ **易于扩展**
- 接口抽象设计
- 支持多种存储后端
- 配置化管理

## 快速开始

### 1. 依赖配置

项目已自动添加必要的依赖：

```kotlin
// build.gradle.kts
implementation("io.ktor:ktor-server-multipart:$ktor_version")
implementation("com.aliyun.oss:aliyun-sdk-oss:3.17.4")
```

### 2. 阿里云 OSS 配置

#### 2.1 创建 OSS Bucket

1. 登录 [阿里云控制台](https://oss.console.aliyun.com/)
2. 创建新的 Bucket
3. 配置访问权限（建议：公共读，私有写）
4. 记录 Bucket 名称和地域节点

#### 2.2 获取访问密钥

1. 访问 [RAM 控制台](https://ram.console.aliyun.com/)
2. 创建新用户或使用现有用户
3. 为用户添加 OSS 相关权限
4. 获取 AccessKey ID 和 AccessKey Secret

### 3. 环境变量配置

创建 `.env` 文件或设置系统环境变量：

```bash
# 阿里云 OSS 配置
OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
OSS_ACCESS_KEY_ID=your_access_key_id
OSS_ACCESS_KEY_SECRET=your_access_key_secret
OSS_BUCKET_NAME=your_bucket_name
OSS_URL_PREFIX=https://your_bucket_name.oss-cn-hangzhou.aliyuncs.com
```

### 4. 应用配置

`application.yaml` 已包含 OSS 配置模板：

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

### 5. 启动应用

```bash
# 构建项目
./gradlew build

# 运行应用
./gradlew run
```

## API 使用示例

### 获取上传配置

```bash
curl -X GET http://localhost:8080/api/v1/upload/config
```

### 上传图片

```bash
curl -X POST \
  http://localhost:8080/api/v1/upload/image \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -F 'file=@/path/to/image.jpg'
```

### 删除图片

```bash
curl -X DELETE \
  http://localhost:8080/api/v1/upload/image/images/2024/01/15/filename.jpg \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## 代码架构说明

### 核心组件

```
src/main/kotlin/com/cleveronion/
├── config/
│   └── OssConfig.kt              # OSS 配置管理
├── domain/entity/
│   └── Upload.kt                 # 上传相关数据类
├── service/
│   ├── FileUploadService.kt      # 文件上传服务接口
│   └── OssFileUploadService.kt   # OSS 实现
├── controller/
│   └── UploadController.kt       # HTTP 接口控制器
└── Routing.kt                    # 路由配置
```

### 设计模式应用

1. **策略模式**: `FileUploadService` 接口支持多种存储实现
2. **工厂模式**: `OssConfig.fromApplicationConfig()` 创建配置对象
3. **单一职责**: 每个类专注于特定功能
4. **依赖注入**: 通过接口抽象依赖关系

### 文件命名规则

上传的文件会按以下格式重命名：
```
images/yyyy/MM/dd/uuid.extension
```

例如：`images/2024/01/15/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6.jpg`

## 测试

### 运行单元测试

```bash
./gradlew test
```

### 测试覆盖的功能

- ✅ OSS 配置验证
- ✅ 文件类型和大小验证
- ✅ 文件名生成和 URL 构建
- ✅ HTTP 接口响应格式
- ✅ 错误处理和异常情况

## 生产环境部署

### 1. 安全配置

```bash
# 使用强密码和定期轮换
export OSS_ACCESS_KEY_SECRET="your_strong_secret_key"

# 配置 HTTPS
export OSS_URL_PREFIX="https://your_bucket_name.oss-cn-hangzhou.aliyuncs.com"
```

### 2. 性能优化

- 配置 CDN 加速
- 启用 OSS 图片处理服务
- 设置合适的缓存策略

### 3. 监控和日志

- 配置应用日志级别
- 设置 OSS 访问日志
- 配置告警规则

## 故障排除

### 常见问题

1. **上传失败 - 配置错误**
   ```
   检查 OSS 配置是否正确
   验证 AccessKey 权限
   确认 Bucket 存在且可访问
   ```

2. **文件类型不支持**
   ```
   检查 allowedExtensions 配置
   确认文件扩展名正确
   ```

3. **文件大小超限**
   ```
   检查 maxFileSize 配置
   确认文件大小在允许范围内
   ```

4. **认证失败**
   ```
   检查 JWT Token 是否有效
   确认 Authorization 头格式正确
   ```

### 调试模式

在 `application.yaml` 中启用调试日志：

```yaml
ktor:
  development: true
  
logging:
  level:
    com.cleveronion: DEBUG
```

## 扩展功能

### 添加新的存储后端

1. 实现 `FileUploadService` 接口
2. 创建对应的配置类
3. 在控制器中注入新的实现

### 添加图片处理功能

可以扩展 `OssFileUploadService` 添加：
- 图片压缩
- 缩略图生成
- 水印添加
- 格式转换

### 添加文件管理功能

- 文件列表查询
- 批量删除
- 文件信息统计
- 访问权限控制

## 相关文档

- [API 详细文档](docs/UPLOAD_API.md)
- [阿里云 OSS 文档](https://help.aliyun.com/product/31815.html)
- [Ktor 文档](https://ktor.io/docs/)

## 技术支持

如果您在使用过程中遇到问题，请：

1. 查看应用日志
2. 检查配置是否正确
3. 参考故障排除指南
4. 提交 Issue 或联系开发团队

---

**注意**: 请确保在生产环境中妥善保管 OSS 访问密钥，不要将其提交到代码仓库中。