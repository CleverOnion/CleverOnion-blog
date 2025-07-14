# GitHub 登录集成说明

## 功能概述

本项目已成功集成GitHub OAuth登录功能，用户可以通过GitHub账号快速登录博客系统。

## 实现的功能

### 前端功能
1. **登录按钮**: 在Header组件中添加了GitHub登录按钮
2. **用户状态管理**: 使用Zustand管理用户登录状态
3. **用户头像显示**: 登录后显示用户头像和信息
4. **登出功能**: 支持用户登出
5. **OAuth回调处理**: 专门的回调页面处理GitHub登录结果

### 后端功能
1. **OAuth2配置**: 配置GitHub OAuth2认证
2. **JWT Token生成**: 生成访问令牌和刷新令牌
3. **用户信息获取**: 从GitHub API获取用户信息
4. **数据库存储**: 自动创建或更新用户信息

## 技术架构

### 前端技术栈
- React 19 + TypeScript
- Zustand (状态管理)
- Axios (HTTP请求)
- React Router (路由管理)
- Tailwind CSS (样式)

### 后端技术栈
- Kotlin + Ktor
- JWT认证
- PostgreSQL数据库
- GitHub OAuth2 API

## 配置说明

### 1. GitHub OAuth应用配置

在GitHub上创建OAuth应用：
1. 访问 GitHub Settings > Developer settings > OAuth Apps
2. 点击 "New OAuth App"
3. 填写应用信息：
   - Application name: `CleverOnion Blog`
   - Homepage URL: `http://localhost:3000`
   - Authorization callback URL: `http://localhost:8080/auth/github/callback`

### 2. 后端配置

在 `application.yaml` 中配置：
```yaml
oauth2:
  github:
    clientId: "你的GitHub Client ID"
    clientSecret: "你的GitHub Client Secret"
    redirectUri: "http://localhost:8080/auth/github/callback"
    scope: "user:email"
```

### 3. 前端配置

在 `src/api/auth.ts` 中确认API基础URL：
```typescript
const API_BASE_URL = 'http://localhost:8080';
```

## 使用流程

### 用户登录流程
1. 用户点击Header中的"GitHub登录"按钮
2. 前端调用 `authAPI.loginWithGitHub()` 重定向到GitHub授权页面
3. 用户在GitHub上授权应用
4. GitHub重定向到后端回调地址 `/auth/github/callback`
5. 后端处理OAuth回调，获取用户信息，生成JWT token
6. 后端重定向到前端回调页面 `/auth/callback` 并携带token信息
7. 前端回调页面解析token，存储到localStorage
8. 更新用户状态，跳转到首页

### 用户状态管理
- 登录状态存储在Zustand store中
- Token存储在localStorage中
- 支持自动token刷新
- 页面刷新后自动恢复登录状态

## API接口

### 认证相关接口
- `GET /auth/github` - GitHub登录入口
- `GET /auth/github/callback` - GitHub OAuth回调
- `GET /auth/me` - 获取当前用户信息
- `POST /auth/refresh` - 刷新访问令牌
- `POST /auth/logout` - 用户登出

## 文件结构

### 前端新增文件
```
src/
├── api/
│   └── auth.ts                 # 认证API接口
├── store/
│   └── authStore.ts            # 用户状态管理
├── pages/
│   └── OAuthCallback.tsx       # OAuth回调处理页面
└── components/header/
    └── ActionButtons.tsx       # 更新的Header按钮组件
```

### 后端相关文件
```
src/main/kotlin/com/cleveronion/
├── controller/
│   └── AuthController.kt       # 认证控制器
├── service/
│   ├── AuthService.kt          # 认证服务
│   ├── GitHubOAuthService.kt   # GitHub OAuth服务
│   └── UserService.kt          # 用户服务
├── domain/entity/
│   └── User.kt                 # 用户实体
└── Authentication.kt           # 认证配置
```

## 安全特性

1. **JWT Token**: 使用JWT进行用户认证
2. **Token刷新**: 支持访问令牌自动刷新
3. **HTTPS支持**: 生产环境建议使用HTTPS
4. **状态验证**: OAuth2 state参数防止CSRF攻击
5. **Token过期处理**: 自动处理token过期情况

## 开发和测试

### 启动项目
1. 启动后端服务：`./gradlew run` (端口8080)
2. 启动前端服务：`npm run dev` (端口3000)

### 测试登录功能
1. 访问 `http://localhost:3000`
2. 点击Header中的"GitHub登录"按钮
3. 完成GitHub授权
4. 验证登录状态和用户信息显示

## 注意事项

1. **环境变量**: 生产环境中应使用环境变量存储敏感信息
2. **CORS配置**: 确保后端正确配置CORS
3. **URL配置**: 生产环境需要更新回调URL
4. **错误处理**: 已实现基本错误处理，可根据需要扩展

## 后续优化建议

1. 添加更多OAuth提供商（Google、微信等）
2. 实现记住登录状态功能
3. 添加用户权限管理
4. 优化错误提示和用户体验
5. 添加登录日志记录