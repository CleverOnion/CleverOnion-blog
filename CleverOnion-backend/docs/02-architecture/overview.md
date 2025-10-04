# CleverOnion 博客系统架构文档

## 1. 系统概述

CleverOnion 是一个基于 Spring Boot 3.5.4 和 Java 21 构建的现代化博客系统后端，采用领域驱动设计（DDD）架构模式，提供完整的博客管理功能。

### 1.1 技术栈

- **核心框架**: Spring Boot 3.5.4
- **Java版本**: Java 21
- **数据库**: PostgreSQL
- **缓存**: Redis
- **认证授权**: Sa-Token
- **API文档**: Swagger/OpenAPI 3
- **构建工具**: Maven
- **ORM**: Spring Data JPA

### 1.2 系统特性

- 基于DDD的分层架构设计
- RESTful API设计
- GitHub OAuth2 登录集成
- 完整的文章生命周期管理
- 分类和标签系统
- 评论系统
- 管理员权限控制

## 2. 架构设计

### 2.1 整体架构

系统采用经典的DDD四层架构模式：

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│                      (表现层)                                │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                         │
│                      (应用层)                                │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                             │
│                      (领域层)                                │
├─────────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                        │
│                    (基础设施层)                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 分层职责

#### 2.2.1 表现层 (Presentation Layer)
- **位置**: `com.cleveronion.blog.presentation`
- **职责**: 
  - 处理HTTP请求和响应
  - 参数验证和格式转换
  - API接口定义
- **主要组件**:
  - Controller: REST API控制器
  - DTO: 数据传输对象

#### 2.2.2 应用层 (Application Layer)
- **位置**: `com.cleveronion.blog.application`
- **职责**:
  - 业务流程编排
  - 事务管理
  - 领域事件处理
- **主要组件**:
  - ApplicationService: 应用服务
  - Command: 命令对象
  - EventHandler: 事件处理器

#### 2.2.3 领域层 (Domain Layer)
- **位置**: `com.cleveronion.blog.domain`
- **职责**:
  - 核心业务逻辑
  - 业务规则实现
  - 领域模型定义
- **主要组件**:
  - Aggregate: 聚合根
  - Entity: 实体
  - ValueObject: 值对象
  - Repository: 仓储接口
  - Event: 领域事件

#### 2.2.4 基础设施层 (Infrastructure Layer)
- **位置**: `com.cleveronion.blog.infrastructure`
- **职责**:
  - 数据持久化
  - 外部服务集成
  - 技术实现细节
- **主要组件**:
  - RepositoryImpl: 仓储实现
  - PO: 持久化对象
  - Client: 外部服务客户端
  - Configuration: 配置类

### 2.3 限界上下文

系统按业务领域划分为以下限界上下文：

#### 2.3.1 文章上下文 (Article Context)
- **聚合根**: ArticleAggregate, CategoryAggregate, TagAggregate
- **核心功能**: 文章创建、编辑、发布、归档、分类管理、标签管理

#### 2.3.2 用户上下文 (User Context)
- **聚合根**: UserAggregate
- **核心功能**: 用户管理、GitHub OAuth2认证

#### 2.3.3 评论上下文 (Comment Context)
- **聚合根**: CommentAggregate
- **核心功能**: 评论管理、回复功能

#### 2.3.4 认证上下文 (Auth Context)
- **核心功能**: 用户认证、授权、GitHub OAuth2集成

#### 2.3.5 管理上下文 (Admin Context)
- **核心功能**: 管理员权限控制、系统管理

## 3. 核心领域模型

### 3.1 文章聚合 (Article Aggregate)

```java
ArticleAggregate
├── ArticleId (值对象)
├── ArticleContent (值对象)
├── ArticleStatus (枚举)
├── CategoryId (值对象)
├── AuthorId (值对象)
├── Set<TagId> (值对象集合)
└── LocalDateTime publishedAt
```

**核心业务规则**:
- 文章状态流转: DRAFT → PUBLISHED → ARCHIVED
- 只有作者可以修改自己的文章
- 发布时自动设置发布时间

### 3.2 用户聚合 (User Aggregate)

```java
UserAggregate
├── UserId (值对象)
├── GitHubId (值对象)
├── UserProfile (值对象)
├── UserRole (枚举)
└── LocalDateTime createdAt
```

### 3.3 分类聚合 (Category Aggregate)

```java
CategoryAggregate
├── CategoryId (值对象)
├── CategoryName (值对象)
├── CategoryDescription (值对象)
└── LocalDateTime createdAt
```

### 3.4 标签聚合 (Tag Aggregate)

```java
TagAggregate
├── TagId (值对象)
├── TagName (值对象)
└── LocalDateTime createdAt
```

## 4. 数据模型

### 4.1 核心表结构

#### 4.1.1 用户表 (users)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT UNIQUE NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    avatar_url TEXT,
    bio TEXT,
    location VARCHAR(255),
    blog_url VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4.1.2 文章表 (articles)
```sql
CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    category_id BIGINT REFERENCES categories(id),
    author_id BIGINT NOT NULL REFERENCES users(id),
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 索引策略

- **性能优化索引**:
  - `idx_articles_status_published_at`: 支持按状态和发布时间查询
  - `idx_articles_author_id`: 支持按作者查询
  - `idx_articles_category_id`: 支持按分类查询
  - `idx_users_github_id`: GitHub ID唯一索引

## 5. API设计

### 5.1 RESTful API规范

- **URL设计**: 使用名词复数形式，如 `/articles`
- **HTTP方法**: 
  - GET: 查询资源
  - POST: 创建资源
  - PUT: 完整更新资源
  - PATCH: 部分更新资源
  - DELETE: 删除资源

### 5.2 统一响应格式

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 5.3 主要API端点

#### 5.3.1 文章管理
- `POST /articles` - 创建文章草稿
- `POST /articles/publish` - 直接发布文章
- `PUT /articles/{id}` - 更新文章
- `POST /articles/{id}/publish` - 发布文章
- `POST /articles/{id}/archive` - 归档文章
- `GET /articles` - 查询文章列表
- `GET /articles/{id}` - 查询文章详情

#### 5.3.2 用户管理
- `GET /users` - 查询用户列表
- `GET /users/{id}` - 查询用户详情

#### 5.3.3 认证授权
- `GET /auth/github/authorize` - 获取GitHub授权URL
- `POST /auth/github/callback` - GitHub回调处理
- `POST /auth/logout` - 用户登出

## 6. 安全设计

### 6.1 认证机制
- 基于Sa-Token的JWT认证
- GitHub OAuth2集成
- Token自动续期机制

### 6.2 授权控制
- 基于角色的访问控制(RBAC)
- 资源级权限控制
- API接口权限注解

### 6.3 安全措施
- 输入参数验证
- SQL注入防护
- XSS防护
- CSRF防护

## 7. 异常处理

### 7.1 异常层次结构

```
Exception
└── RuntimeException
    └── BusinessException
        ├── ResourceNotFoundException
        ├── AccessDeniedException
        ├── DuplicateResourceException
        └── ValidationException
```

### 7.2 全局异常处理

通过`@RestControllerAdvice`实现统一异常处理：
- 业务异常转换为标准错误响应
- 系统异常日志记录和通用错误返回
- 参数验证异常的友好提示

## 8. 配置管理

### 8.1 多环境配置
- `application.yml` - 基础配置
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境

### 8.2 核心配置项
- 数据库连接配置
- Redis缓存配置
- Sa-Token认证配置
- GitHub OAuth2配置
- 日志配置

## 9. 部署架构

### 9.1 应用部署
- Spring Boot内嵌Tomcat
- JAR包部署方式
- 支持Docker容器化

### 9.2 数据存储
- PostgreSQL主数据库
- Redis缓存和会话存储

### 9.3 监控运维
- Spring Boot Actuator健康检查
- 日志文件输出
- JVM监控指标

## 10. 扩展性设计

### 10.1 水平扩展
- 无状态应用设计
- 数据库读写分离支持
- 缓存集群支持

### 10.2 功能扩展
- 插件化架构预留
- 事件驱动架构支持
- 微服务拆分准备

### 10.3 性能优化
- 数据库索引优化
- 缓存策略优化
- 分页查询优化
- 懒加载机制