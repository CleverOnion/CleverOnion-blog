# 个人博客系统技术设计文档

## 1. 系统架构

本系统将遵循《开发规范.md》中定义的 **领域驱动设计（DDD）分层架构**。该架构确保了业务逻辑的清晰、代码的解耦和系统的可维护性。

- **Presentation 层**: 负责处理 API 请求和响应，使用 Spring MVC。
- **Application 层**: 负责编排业务流程，调用领域服务，处理 DTO 转换和事务管理。
- **Domain 层**: 包含核心业务逻辑和领域模型，保持纯净，不依赖任何外部框架。
- **Infrastructure 层**: 负责技术实现，如数据库交互（JPA）、缓存（Redis）和外部服务集成（GitHub OAuth2）。

## 2. 技术选型

- **后端框架**: Spring Boot
- **数据库**: PostgreSQL
- **数据持久化**: Spring Data JPA
- **缓存**: Redis
- **鉴权框架**: Sa-Token
- **认证服务**: GitHub OAuth2

## 3. 数据库设计 (PostgreSQL)

数据库表的设计遵循第三范式（3NF），以确保数据的规范性和一致性。主键统一使用 `BIGINT` 类型的自增ID。

### 3.1. `users` 表

存储通过 GitHub 登录的用户信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | 用户唯一ID |
| `github_id` | `BIGINT` | `NOT NULL`, `UNIQUE` | GitHub 用户唯一ID |
| `username` | `VARCHAR(255)` | `NOT NULL` | GitHub 用户名 |
| `avatar_url` | `VARCHAR(255)` | | GitHub 头像链接 |
| `created_at` | `TIMESTAMP` | `NOT NULL` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | 更新时间 |

**索引**: `github_id` 字段上应创建唯一索引以加速查询。

### 3.2. `categories` 表

存储文章分类。

| 字段名 | 数据类型 | 约束 | 描述 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | 分类唯一ID |
| `name` | `VARCHAR(255)` | `NOT NULL`, `UNIQUE` | 分类名称 |
| `created_at` | `TIMESTAMP` | `NOT NULL` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | 更新时间 |

### 3.3. `tags` 表

存储文章标签。

| 字段名 | 数据类型 | 约束 | 描述 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | 标签唯一ID |
| `name` | `VARCHAR(255)` | `NOT NULL`, `UNIQUE` | 标签名称 |
| `created_at` | `TIMESTAMP` | `NOT NULL` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | 更新时间 |

### 3.4. `articles` 表

存储文章核心内容。

| 字段名 | 数据类型 | 约束 | 描述 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | 文章唯一ID |
| `title` | `VARCHAR(255)` | `NOT NULL` | 文章标题 |
| `content` | `TEXT` | `NOT NULL` | 文章正文 (Markdown) |
| `summary` | `TEXT` | | 文章摘要 |
| `status` | `VARCHAR(50)` | `NOT NULL` | 文章状态 (`DRAFT`, `PUBLISHED`, `ARCHIVED`) |
| `category_id` | `BIGINT` | `FK to categories(id)` | 所属分类ID |
| `author_id` | `BIGINT` | `FK to users(id)` | 作者ID |
| `created_at` | `TIMESTAMP` | `NOT NULL` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | 更新时间 |
| `published_at` | `TIMESTAMP` | | 发布时间 |

**索引**: `status`, `category_id`, `author_id` 字段应创建索引以优化查询。

### 3.5. `article_tags` 表 (中间表)

用于建立文章和标签的多对多关系。

| 字段名 | 数据类型 | 约束 | 描述 |
| --- | --- | --- | --- |
| `article_id` | `BIGINT` | `FK to articles(id)` | 文章ID |
| `tag_id` | `BIGINT` | `FK to tags(id)` | 标签ID |

**约束**: `(article_id, tag_id)` 联合主键，确保唯一性。

### 3.6. `comments` 表

存储文章评论。

| 字段名 | 数据类型 | 约束 | 描述 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | 评论唯一ID |
| `content` | `TEXT` | `NOT NULL` | 评论内容 |
| `article_id` | `BIGINT` | `FK to articles(id)` | 所属文章ID |
| `user_id` | `BIGINT` | `FK to users(id)` | 评论用户ID |
| `parent_id` | `BIGINT` | `FK to comments(id)` | 父评论ID (用于层级回复) |
| `created_at` | `TIMESTAMP` | `NOT NULL` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | 更新时间 |

**索引**: `article_id`, `user_id` 字段应创建索引。

## 4. 鉴权流程

### 4.1. GitHub OAuth2 登录流程
1.  **前端**: 用户点击“使用 GitHub 登录”，跳转到 GitHub 授权页。
2.  **GitHub**: 用户同意授权，GitHub 携带一个 `code` 重定向回后端指定的回调 URL。
3.  **后端**: Application 层的服务接收 `code`，并向 GitHub 请求 `access_token`。
4.  **后端**: 使用获取到的 `access_token` 向 GitHub 请求用户信息。
5.  **后端**: 根据返回的 GitHub 用户信息中的 `id`，在 `users` 表中查找或创建新用户。
6.  **后端**: 为该用户生成双 Token（Access Token 和 Refresh Token），并返回给前端。

### 4.2. 双 Token 鉴权机制 (Sa-Token)
1.  **登录**: 登录成功后，后端使用 Sa-Token 生成 Access Token 和 Refresh Token。
2.  **API 请求**: 前端在请求头中携带 Access Token 访问受保护的 API。
3.  **后端校验**: Sa-Token 的拦截器自动校验 Access Token 的有效性。
4.  **Token 刷新**: 
    - 如果 Access Token 过期，后端返回特定状态码。
    - 前端收到该状态码后，使用 Refresh Token 调用刷新接口。
    - 后端校验 Refresh Token，如果有效，则签发新的 Access Token 和 Refresh Token。
    - 如果 Refresh Token 也过期，则要求用户重新登录。

## 5. 缓存策略 (Redis)

为了提升系统性能和响应速度，将使用 Redis 对热点数据进行缓存。

- **文章缓存**: 对已发布的文章内容进行缓存，Key 可以是 `article:{id}`。
- **分类与标签缓存**: 对分类列表和标签列表进行缓存。
- **首页数据缓存**: 对首页需要展示的文章列表、分类等聚合数据进行缓存。

**缓存更新策略**: 采用“缓存+数据库”双写模式。当数据发生变更时（如文章更新、分类修改），在更新数据库的同时，主动删除（或更新）Redis 中对应的缓存，以保证数据一致性。