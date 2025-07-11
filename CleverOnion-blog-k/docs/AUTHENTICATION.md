# CleverOnion Blog - 认证系统文档

## 概述

第二阶段实现了完整的OAuth2 GitHub集成认证系统，包括JWT Token管理、认证中间件和用户管理API。

## 功能特性

### 1. OAuth2 GitHub集成
- GitHub OAuth2登录流程
- 自动获取用户信息和邮箱
- 用户信息同步和更新

### 2. JWT Token管理
- 访问Token（1小时有效期）
- 刷新Token（7天有效期）
- Token验证和解析
- Token过期检测

### 3. 认证中间件
- JWT认证保护
- OAuth2认证流程
- 自动Token验证
- 错误处理和响应

### 4. 用户管理API
- 用户信息查询
- 用户列表（分页）
- 用户搜索（待实现）
- 用户删除（管理员功能）

## API端点

### 认证相关

#### GitHub OAuth登录
```
GET /api/v1/auth/github
```
重定向到GitHub授权页面

#### OAuth回调处理
```
GET /api/v1/auth/github/callback
```
处理GitHub OAuth回调，返回JWT Token

响应示例：
```json
{
  "user": {
    "id": 1,
    "githubId": 12345,
    "githubLogin": "username",
    "email": "user@example.com",
    "name": "User Name",
    "avatarUrl": "https://avatars.githubusercontent.com/u/12345"
  },
  "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600000
}
```

#### 刷新Token
```
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

#### 登出
```
POST /api/v1/auth/logout
Authorization: Bearer <access_token>
```

#### 获取当前用户信息
```
GET /api/v1/auth/me
Authorization: Bearer <access_token>
```

### 用户管理

#### 获取用户列表
```
GET /api/v1/users?page=1&pageSize=20
Authorization: Bearer <access_token>
```

#### 获取指定用户
```
GET /api/v1/users/{id}
Authorization: Bearer <access_token>
```

#### 更新当前用户信息
```
PUT /api/v1/users/me
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "New Name",
  "bio": "New Bio"
}
```

#### 删除用户
```
DELETE /api/v1/users/{id}
Authorization: Bearer <access_token>
```

#### 搜索用户
```
GET /api/v1/users/search?q=keyword
Authorization: Bearer <access_token>
```

## 配置说明

### 环境变量

需要在环境变量中设置以下值：

```bash
# GitHub OAuth2配置
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# JWT密钥
JWT_SECRET=your_jwt_secret_key_here
```

### GitHub OAuth应用设置

1. 在GitHub上创建OAuth应用
2. 设置Authorization callback URL为：`http://localhost:8080/api/v1/auth/github/callback`
3. 获取Client ID和Client Secret

### application.yaml配置

```yaml
oauth2:
  github:
    clientId: "${GITHUB_CLIENT_ID:your_github_client_id}"
    clientSecret: "${GITHUB_CLIENT_SECRET:your_github_client_secret}"
    redirectUri: "http://localhost:8080/api/v1/auth/github/callback"
    scope: "user:email"

jwt:
  secret: "${JWT_SECRET:your_jwt_secret_key_here}"
  issuer: "cleveronion-blog"
  audience: "cleveronion-blog-users"
  expirationTime: 3600000 # 1小时
  refreshExpirationTime: 604800000 # 7天
```

## 安全特性

### Token安全
- HMAC256算法签名
- 短期访问Token（1小时）
- 长期刷新Token（7天）
- Token类型验证

### API保护
- JWT中间件保护
- 用户身份验证
- 权限检查（部分实现）

### OAuth2安全
- State参数防CSRF
- 安全的回调处理
- GitHub官方API集成

## 使用流程

### 1. 用户登录
1. 前端重定向到 `/api/v1/auth/github`
2. 用户在GitHub授权
3. GitHub回调到 `/api/v1/auth/github/callback`
4. 系统返回JWT Token
5. 前端保存Token用于后续请求

### 2. API调用
1. 在请求头中添加：`Authorization: Bearer <access_token>`
2. 系统自动验证Token
3. 提取用户信息进行业务处理

### 3. Token刷新
1. 检测访问Token即将过期
2. 使用刷新Token获取新的访问Token
3. 更新本地存储的Token

## 下一步计划

- [ ] 实现角色权限系统
- [ ] 添加Token黑名单机制
- [ ] 实现用户搜索功能
- [ ] 添加用户配置表
- [ ] 实现管理员权限检查
- [ ] 添加登录日志记录
- [ ] 实现多OAuth提供商支持