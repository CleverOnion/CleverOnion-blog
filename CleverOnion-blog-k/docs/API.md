# CleverOnion Blog API 文档

本文档描述了 CleverOnion Blog 系统的所有 API 接口。

## 基础信息

- **Base URL**: `http://localhost:8080/api/v1`
- **认证方式**: JWT Bearer Token
- **内容类型**: `application/json`

## 认证说明

需要认证的接口需要在请求头中包含 JWT Token：

```
Authorization: Bearer <your-jwt-token>
```

## 响应格式

### 成功响应

```json
{
  "data": {},
  "message": "操作成功"
}
```

### 错误响应

```json
{
  "error": "错误信息"
}
```

### 分页响应

```json
{
  "data": [],
  "pagination": {
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 100,
    "totalPages": 5
  }
}
```

---

## 文章管理 API

### 1. 获取已发布文章列表

**GET** `/articles`

**查询参数**:
- `page` (可选): 页码，默认 1
- `pageSize` (可选): 每页大小，默认 20，最大 100
- `authorId` (可选): 按作者ID过滤

**响应示例**:
```json
{
  "articles": [
    {
      "id": 1,
      "authorId": 1,
      "title": "文章标题",
      "contentMd": "Markdown内容",
      "contentHtml": "HTML内容",
      "status": "PUBLISHED",
      "viewCount": 100,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ],
  "pagination": {
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 50,
    "totalPages": 3
  }
}
```

### 2. 获取文章详情

**GET** `/articles/{id}`

**路径参数**:
- `id`: 文章ID

**响应**: 文章详情对象

### 3. 搜索文章

**GET** `/articles/search`

**查询参数**:
- `keyword`: 搜索关键词（必需）
- `limit`: 结果数量限制，默认 20

### 4. 创建文章 🔒

**POST** `/articles`

**请求体**:
```json
{
  "title": "文章标题",
  "contentMd": "Markdown内容",
  "status": "DRAFT",
  "tags": ["标签1", "标签2"]
}
```

### 5. 更新文章 🔒

**PUT** `/articles/{id}`

**请求体**:
```json
{
  "title": "新标题",
  "contentMd": "新内容",
  "status": "PUBLISHED",
  "tags": ["新标签"]
}
```

### 6. 删除文章 🔒

**DELETE** `/articles/{id}`

### 7. 获取当前用户文章列表 🔒

**GET** `/articles/my`

**查询参数**:
- `page`: 页码
- `pageSize`: 每页大小
- `status`: 按状态过滤（DRAFT, PUBLISHED）

### 8. 预览文章详情 🔒

**GET** `/articles/{id}/preview`

---

## 标签管理 API

### 1. 获取所有标签

**GET** `/tags`

**查询参数**:
- `page`: 页码，默认 1
- `pageSize`: 每页大小，默认 50

**响应示例**:
```json
{
  "tags": [
    {
      "id": 1,
      "name": "技术",
      "articleCount": 10
    }
  ],
  "pagination": {
    "currentPage": 1,
    "pageSize": 50,
    "totalCount": 20,
    "totalPages": 1
  }
}
```

### 2. 获取标签详情

**GET** `/tags/{id}`

### 3. 搜索标签

**GET** `/tags/search`

**查询参数**:
- `keyword`: 搜索关键词（必需）
- `limit`: 结果数量限制，默认 20

### 4. 获取热门标签

**GET** `/tags/popular`

**查询参数**:
- `limit`: 数量限制，默认 10

### 5. 创建标签 🔒

**POST** `/tags`

**请求体**:
```json
{
  "name": "标签名称"
}
```

### 6. 更新标签 🔒

**PUT** `/tags/{id}`

**请求体**:
```json
{
  "name": "新标签名称"
}
```

### 7. 删除标签 🔒

**DELETE** `/tags/{id}`

### 8. 批量创建标签 🔒

**POST** `/tags/batch`

**请求体**:
```json
{
  "names": ["标签1", "标签2", "标签3"]
}
```

---

## 评论管理 API

### 1. 获取文章评论列表

**GET** `/comments/article/{articleId}`

**查询参数**:
- `page`: 页码，默认 1
- `pageSize`: 每页大小，默认 20

**响应示例**:
```json
{
  "comments": [
    {
      "comment": {
        "id": 1,
        "articleId": 1,
        "userId": 1,
        "content": "评论内容",
        "parentId": null,
        "createdAt": "2024-01-01T00:00:00"
      },
      "replies": [
        {
          "id": 2,
          "articleId": 1,
          "userId": 2,
          "content": "回复内容",
          "parentId": 1,
          "createdAt": "2024-01-01T01:00:00"
        }
      ]
    }
  ],
  "pagination": {
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 10,
    "totalPages": 1
  }
}
```

### 2. 获取评论详情

**GET** `/comments/{id}`

### 3. 获取最新评论

**GET** `/comments/latest`

**查询参数**:
- `limit`: 数量限制，默认 10

### 4. 创建评论 🔒

**POST** `/comments`

**请求体**:
```json
{
  "articleId": 1,
  "content": "评论内容",
  "parentId": null
}
```

### 5. 更新评论 🔒

**PUT** `/comments/{id}`

**请求体**:
```json
{
  "content": "新的评论内容"
}
```

### 6. 删除评论 🔒

**DELETE** `/comments/{id}`

### 7. 获取当前用户评论列表 🔒

**GET** `/comments/my`

**查询参数**:
- `page`: 页码
- `pageSize`: 每页大小

### 8. 管理员删除评论 🔒

**DELETE** `/comments/{id}/admin`

---

## 用户管理 API

### 1. 获取所有用户 🔒

**GET** `/users`

**查询参数**:
- `page`: 页码
- `pageSize`: 每页大小

### 2. 获取用户详情

**GET** `/users/{id}`

---

## 认证 API

### 1. GitHub OAuth 登录

**GET** `/auth/github`

重定向到 GitHub OAuth 授权页面

### 2. GitHub OAuth 回调

**GET** `/auth/github/callback`

**查询参数**:
- `code`: GitHub 授权码

**响应**:
```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "user": {
    "id": 1,
    "githubId": 12345,
    "githubLogin": "username",
    "email": "user@example.com",
    "name": "User Name",
    "avatarUrl": "https://avatar.url"
  }
}
```

### 3. 刷新访问令牌

**POST** `/auth/refresh`

**请求体**:
```json
{
  "refreshToken": "jwt-refresh-token"
}
```

---

## 错误码说明

| HTTP 状态码 | 说明 |
|------------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 数据模型

### Article（文章）

```json
{
  "id": 1,
  "authorId": 1,
  "title": "文章标题",
  "contentMd": "Markdown内容",
  "contentHtml": "HTML内容",
  "status": "PUBLISHED",
  "viewCount": 100,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### Tag（标签）

```json
{
  "id": 1,
  "name": "标签名称"
}
```

### Comment（评论）

```json
{
  "id": 1,
  "articleId": 1,
  "userId": 1,
  "content": "评论内容",
  "parentId": null,
  "createdAt": "2024-01-01T00:00:00"
}
```

### User（用户）

```json
{
  "id": 1,
  "githubId": 12345,
  "githubLogin": "username",
  "email": "user@example.com",
  "name": "User Name",
  "bio": "用户简介",
  "avatarUrl": "https://avatar.url",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

---

## 使用示例

### 创建文章

```bash
curl -X POST http://localhost:8080/api/v1/articles \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "我的第一篇文章",
    "contentMd": "# 标题\n\n这是文章内容",
    "status": "PUBLISHED",
    "tags": ["技术", "编程"]
  }'
```

### 获取文章列表

```bash
curl http://localhost:8080/api/v1/articles?page=1&pageSize=10
```

### 创建评论

```bash
curl -X POST http://localhost:8080/api/v1/comments \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "articleId": 1,
    "content": "这是一个很好的文章！",
    "parentId": null
  }'
```

---

## 注意事项

1. **认证**: 标记为 🔒 的接口需要 JWT 认证
2. **分页**: 所有列表接口都支持分页，默认每页 20 条记录
3. **参数验证**: 所有接口都会验证输入参数的有效性
4. **错误处理**: 所有接口都有统一的错误处理和响应格式
5. **内容长度**: 文章内容最大 50000 字符，评论内容最大 1000 字符
6. **标签限制**: 每篇文章最多 10 个标签
7. **搜索功能**: 支持文章标题和内容的模糊搜索
8. **评论嵌套**: 支持一级回复，不支持多级嵌套

---

*最后更新时间: 2024-01-01*