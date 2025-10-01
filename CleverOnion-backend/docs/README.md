# CleverOnion 博客系统 Wiki

欢迎来到 CleverOnion 博客系统的技术文档中心！这里包含了项目的完整技术文档，帮助开发者快速了解和参与项目开发。

📋 **[文档目录结构说明](./STRUCTURE.md)** - 了解文档组织方式

## 📚 文档目录

### 🏗️ 架构设计

- **[系统架构文档](./architecture.md)** - 详细介绍系统的整体架构设计、技术栈选择、分层结构和核心设计理念

### 🚀 功能特性

- **[功能说明文档](./features.md)** - 全面介绍系统的功能模块、API 接口、数据模型和业务流程

### 📋 开发规范

- **[开发规范文档](./development-guidelines.md)** - 项目的编码规范、架构规范、API 设计规范和最佳实践
- **[开发规范与项目分析](./standards/)** - 完整的开发规范总结和深度问题分析（推荐）
  - [后端开发规范总结](./standards/backend-development-standards.md) - 基于实际代码的全面规范总结
  - [项目问题分析与改进建议](./standards/issues-and-improvements.md) - 深度问题分析和具体改进方案

### 🔍 问题分析

- **[问题分析和优化建议](./issues-and-optimization.md)** - 当前代码中存在的问题分析和具体的优化建议

### 🏗️ 架构设计

- **[架构设计文档](./architecture/)** - 架构改进和设计方案
  - **[CQRS 架构专题](./architecture/cqrs/)** - 命令查询职责分离完整方案
    - [设计文档](./architecture/cqrs/design/) - 理论、方案、最佳实践
    - [执行文档](./architecture/cqrs/execution/) - 任务清单、进度跟踪

## 🎯 项目概述

CleverOnion 是一个基于 Spring Boot 和领域驱动设计（DDD）的现代化博客系统，采用前后端分离架构，提供完整的博客管理功能。

### 核心特性

- 🏛️ **DDD 架构**: 采用领域驱动设计，清晰的分层架构
- 🔐 **OAuth2 认证**: 支持 GitHub OAuth2 登录
- 📝 **文章管理**: 完整的文章生命周期管理
- 🏷️ **分类标签**: 灵活的分类和标签系统
- 💬 **评论系统**: 支持文章评论功能
- 👥 **用户管理**: 用户权限和角色管理
- 🛡️ **安全防护**: 完善的安全机制和权限控制

### 技术栈

- **后端框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **认证授权**: Sa-Token
- **API 文档**: OpenAPI 3 (Swagger)
- **构建工具**: Maven
- **Java 版本**: JDK 17+

## 🚀 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本
- Node.js 16+ (前端开发)

### 本地开发环境搭建

1. **克隆项目**

   ```bash
   git clone <repository-url>
   cd CleverOnion-backend
   ```

2. **配置数据库**

   ```sql
   CREATE DATABASE cleveronion_blog_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **配置应用**

   ```bash
   # 复制配置文件
   cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

   # 修改数据库连接信息和 GitHub OAuth2 配置
   ```

4. **启动应用**

   ```bash
   mvn spring-boot:run
   ```

5. **访问应用**
   - API 文档: http://localhost:8080/swagger-ui.html
   - 健康检查: http://localhost:8080/actuator/health

## 📖 开发指南

### 新功能开发流程

1. **创建功能分支**

   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **遵循开发规范**

   - 参考 [开发规范文档](./development-guidelines.md)
   - 遵循 DDD 分层架构
   - 编写单元测试和集成测试

3. **提交代码**

   ```bash
   git add .
   git commit -m "feat(module): 添加新功能描述"
   ```

4. **创建 Pull Request**
   - 填写详细的 PR 描述
   - 确保所有测试通过
   - 等待代码审查

### 代码规范要点

- **分层架构**: 严格遵循 presentation → application → domain → infrastructure 的依赖关系
- **命名规范**: 使用有意义的英文命名，避免拼音
- **异常处理**: 使用统一的异常处理机制
- **日志记录**: 合理使用日志级别，记录关键业务流程
- **测试覆盖**: 核心业务逻辑必须有单元测试

## 🔧 常用命令

### Maven 命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包应用
mvn package

# 跳过测试打包
mvn package -DskipTests

# 清理并重新构建
mvn clean install
```

### 数据库迁移

```bash
# 生成数据库变更脚本
mvn flyway:migrate

# 查看迁移状态
mvn flyway:info

# 清理数据库（仅开发环境）
mvn flyway:clean
```

## 🐛 问题排查

### 常见问题

1. **数据库连接失败**

   - 检查数据库服务是否启动
   - 验证连接配置是否正确
   - 确认数据库用户权限

2. **OAuth2 登录失败**

   - 检查 GitHub OAuth2 应用配置
   - 验证 client-id 和 client-secret
   - 确认回调 URL 配置正确

3. **测试失败**
   - 检查测试数据库配置
   - 确认测试环境隔离
   - 查看具体的错误日志

### 日志查看

```bash
# 查看应用日志
tail -f logs/application.log

# 查看错误日志
tail -f logs/error.log

# 查看 SQL 日志（开发环境）
grep "Hibernate:" logs/application.log
```

## 📊 项目统计

### 代码结构

```
src/main/java/com/cleveronion/blog/
├── presentation/          # 表现层 (Controllers, DTOs)
├── application/           # 应用层 (Services, Commands)
├── domain/               # 领域层 (Aggregates, Entities, Value Objects)
├── infrastructure/       # 基础设施层 (Repositories, External Services)
└── common/              # 通用模块 (Exceptions, Utils)
```

### 核心模块

- **文章模块**: 文章的完整生命周期管理
- **用户模块**: 用户认证、授权和个人信息管理
- **分类模块**: 文章分类管理
- **标签模块**: 文章标签管理
- **评论模块**: 文章评论功能
- **认证模块**: OAuth2 认证和会话管理

## 🤝 贡献指南

### 如何贡献

1. **Fork 项目**到你的 GitHub 账户
2. **创建功能分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **创建 Pull Request**

### 代码审查标准

- [ ] 代码符合项目规范
- [ ] 业务逻辑正确且完整
- [ ] 异常处理完善
- [ ] 测试覆盖充分
- [ ] 性能影响可接受
- [ ] 安全性考虑周全
- [ ] 文档更新及时

## 📞 联系方式

如果你在使用过程中遇到问题或有改进建议，欢迎通过以下方式联系：

- **Issue**: 在 GitHub 上创建 Issue
- **Discussion**: 参与 GitHub Discussions
- **Email**: 发送邮件到项目维护者

## 📄 许可证

本项目采用 MIT 许可证，详情请查看 [LICENSE](../LICENSE) 文件。

---

**Happy Coding! 🎉**

> 这个 Wiki 会持续更新，请定期查看最新的文档内容。如果你发现文档中有错误或需要补充的内容，欢迎提交 PR 或创建 Issue。
