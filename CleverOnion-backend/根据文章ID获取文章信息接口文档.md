# 根据文章ID获取文章信息接口文档

## 接口概述

**接口地址：** `GET /api/articles/{id}`

**接口描述：** 根据文章ID查询文章详细信息，包括文章内容、作者信息、分类信息、标签信息等完整数据

**接口版本：** v1.0

---

## 请求参数

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 文章ID，必须为正整数 |

### 请求示例

```http
GET /api/articles/123
Host: localhost:8080
Content-Type: application/json
```

---

## 响应格式

### 成功响应

**HTTP状态码：** 200 OK

**响应体结构：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "123",
    "title": "Spring Boot 实战指南",
    "content": "这是一篇关于Spring Boot的详细教程...",
    "summary": "本文介绍了Spring Boot的核心概念和实战应用",
    "status": "PUBLISHED",
    "category": {
      "id": 1,
      "name": "技术分享",
      "icon": "tech-icon"
    },
    "author": {
      "id": "456",
      "githubId": "john_doe",
      "username": "johndoe",
      "avatarUrl": "https://avatars.githubusercontent.com/u/123456"
    },
    "tags": [
      {
        "id": 1,
        "name": "Spring Boot"
      },
      {
        "id": 2,
        "name": "Java"
      }
    ],
    "created_at": "2024-01-15 10:30:00",
    "updated_at": "2024-01-16 14:20:00",
    "published_at": "2024-01-15 15:00:00"
  },
  "timestamp": 1705312200000
}
```

### 错误响应

#### 文章不存在

**HTTP状态码：** 200 OK（业务错误）

```json
{
  "code": 500,
  "message": "文章不存在",
  "data": null,
  "timestamp": 1705312200000
}
```

#### 参数错误

**HTTP状态码：** 400 Bad Request

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": 1705312200000
}
```

#### 服务器内部错误

**HTTP状态码：** 500 Internal Server Error

```json
{
  "code": 500,
  "message": "查询文章失败",
  "data": null,
  "timestamp": 1705312200000
}
```

---

## 数据模型

### ArticleResponse（文章响应对象）

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| id | String | 文章ID | "123" |
| title | String | 文章标题 | "Spring Boot 实战指南" |
| content | String | 文章内容 | "这是一篇关于Spring Boot的详细教程..." |
| summary | String | 文章摘要 | "本文介绍了Spring Boot的核心概念" |
| status | String | 文章状态 | "PUBLISHED"、"DRAFT"、"ARCHIVED" |
| category | CategoryResponse | 分类信息 | 见CategoryResponse |
| author | UserResponse | 作者信息 | 见UserResponse |
| tags | Array<TagResponse> | 标签列表 | 见TagResponse |
| created_at | String | 创建时间 | "2024-01-15 10:30:00" |
| updated_at | String | 更新时间 | "2024-01-16 14:20:00" |
| published_at | String | 发布时间 | "2024-01-15 15:00:00" |

### CategoryResponse（分类响应对象）

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| id | Long | 分类ID | 1 |
| name | String | 分类名称 | "技术分享" |
| icon | String | 分类图标 | "tech-icon" |

### UserResponse（用户响应对象）

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| id | String | 用户ID | "456" |
| githubId | String | GitHub用户ID | "john_doe" |
| username | String | 用户名 | "johndoe" |
| avatarUrl | String | 头像URL | "https://avatars.githubusercontent.com/u/123456" |

### TagResponse（标签响应对象）

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| id | Long | 标签ID | 1 |
| name | String | 标签名称 | "Spring Boot" |

---

## 状态码说明

| HTTP状态码 | 业务状态码 | 说明 |
|------------|------------|------|
| 200 | 200 | 查询成功 |
| 200 | 500 | 文章不存在 |
| 400 | 400 | 请求参数错误 |
| 500 | 500 | 服务器内部错误 |

---

## 使用示例

### cURL 示例

```bash
# 查询ID为123的文章详情
curl -X GET "http://localhost:8080/api/articles/123" \
     -H "Content-Type: application/json"
```

### JavaScript 示例

```javascript
// 使用 fetch API
async function getArticleById(articleId) {
  try {
    const response = await fetch(`/api/articles/${articleId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('文章详情:', result.data);
      return result.data;
    } else {
      console.error('获取文章失败:', result.message);
      return null;
    }
  } catch (error) {
    console.error('请求失败:', error);
    return null;
  }
}

// 使用示例
getArticleById(123).then(article => {
  if (article) {
    console.log('文章标题:', article.title);
    console.log('作者:', article.author.username);
    console.log('分类:', article.category.name);
    console.log('标签:', article.tags.map(tag => tag.name).join(', '));
  }
});
```

### Java 示例（Spring RestTemplate）

```java
@Service
public class ArticleService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public ArticleResponse getArticleById(Long articleId) {
        String url = "http://localhost:8080/api/articles/" + articleId;
        
        try {
            ResponseEntity<Result<ArticleResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<ArticleResponse>>() {}
            );
            
            Result<ArticleResponse> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                return result.getData();
            } else {
                throw new RuntimeException("获取文章失败: " + (result != null ? result.getMessage() : "未知错误"));
            }
        } catch (Exception e) {
            throw new RuntimeException("请求文章接口失败", e);
        }
    }
}
```

---

## 注意事项

1. **文章状态说明：**
   - `DRAFT`: 草稿状态，未发布
   - `PUBLISHED`: 已发布状态
   - `ARCHIVED`: 已归档状态

2. **时间格式：** 所有时间字段均采用 `yyyy-MM-dd HH:mm:ss` 格式

3. **关联数据：** 接口会自动加载文章的关联数据（作者、分类、标签），如果关联数据不存在或加载失败，对应字段可能为 `null`

4. **性能考虑：** 该接口会查询多个关联表，建议在高并发场景下考虑缓存策略

5. **权限控制：** 当前接口无权限限制，所有用户都可以访问已发布的文章详情

---

## 技术实现

### 核心流程

1. **参数验证：** 验证文章ID是否为有效的Long类型
2. **数据查询：** 通过ArticleApplicationService查询文章聚合根
3. **关联查询：** 并行查询作者、分类、标签信息
4. **响应构建：** 将领域对象转换为DTO响应对象
5. **异常处理：** 统一处理各种异常情况

### 架构特点

- **DDD架构：** 采用领域驱动设计，保持领域层纯净
- **分层解耦：** 表现层、应用层、领域层、基础设施层职责清晰
- **统一响应：** 使用Result包装器统一API响应格式
- **异常处理：** 全局异常处理器统一处理各种异常
- **参数校验：** 使用Bean Validation进行参数校验
- **API文档：** 集成Swagger自动生成API文档