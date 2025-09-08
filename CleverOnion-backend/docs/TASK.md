# 项目开发任务清单

本文档用于跟踪个人博客系统的开发进度。任务按阶段划分，每个阶段都是一个宏观的功能模块集合。

---

### 阶段一：项目初始化与基础架构搭建

**目标**: 搭建一个稳固的项目骨架，为后续功能开发奠定基础。

- **T1.1**: 初始化 Spring Boot 项目，并集成所需依赖（Spring Web, Spring Data JPA, PostgreSQL, Redis, Sa-Token）。
- **T1.2**: 根据《开发规范.md》创建 DDD 分层项目结构（presentation, application, domain, infrastructure）。
- **T1.3**: 配置 application.yml，完成数据库（PostgreSQL）和 Redis 的连接设置。
- **T1.4**: 配置 JPA 和 Sa-Token 的基础参数。

---

### 阶段二：用户与鉴权模块开发

**目标**: 实现完整的用户认证流程，确保系统安全性。

- **T2.1**: 实现 `User` 领域实体和仓储接口（Repository）。
- **T2.2**: 在基础设施层实现 `UserRepository`.
- **T2.3**: 开发 GitHub OAuth2 登录流程，实现从 code 置换 token 到获取用户信息的完整逻辑。
- **T2.4**: 集成 Sa-Token，在用户登录成功后生成双 Token（Access & Refresh Token）。
- **T2.5**: 创建受保护的 API 测试端点，验证 Sa-Token 拦截与鉴权有效性。

---

### 阶段三：核心文章模块开发

**目标**: 完成文章、分类、标签的完整 CRUD 功能。

- **T3.1**: 创建 `Article`, `Category`, `Tag` 的相关聚合、领域实体、值对象及它们之间的关系。
- **T3.2**: 定义 `ArticleRepository`, `CategoryRepository`, `TagRepository` 仓储接口。
- **T3.3**: 在基础设施层实现仓储。（`articles`, `categories`, `tags`, `article_tags`）。
- **T3.4**: 在应用层创建 `ArticleApplicationService`，负责文章的创建、更新、删除、查询等业务流程。
- **T3.5**: 在应用层创建 `CategoryApplicationService`，负责分类的创建、更新、删除、查询等业务流程。
- **T3.6**: 在应用层创建 `TagApplicationService`，负责标签的创建、更新、删除、查询等业务流程。
- **T3.7**: 在表现层创建 `ArticleController` 和相关 DTO，对外暴露文章管理的 API 接口。
- **T3.8**: 在表现层创建 `CategoryController` 和相关 DTO，对外暴露分类管理的 API 接口。
- **T3.9**: 在表现层创建 `TagController` 和相关 DTO，对外暴露标签管理的 API 接口。

---

### 阶段四：评论模块开发

**目标**: 为文章增加互动功能。

- **T4.1**: 创建 `Comment` 领域实体，包含层级关系（parent_id）。
- **T4.2**: 定义并实现 `CommentRepository`。
- **T4.3**: 在应用层创建 `CommentApplicationService`，负责发表和删除评论的逻辑。
- **T4.4**: 在表现层创建 `CommentController`，对外暴露评论操作的 API。

---

### 阶段五：缓存集成与优化

**目标**: 提升系统性能和响应速度。

- **T5.1**: 按照《技术设计文档》中的策略，为文章、分类、标签等热点数据增加 Redis 缓存。
- **T5.2**: 实现缓存更新与淘汰机制，确保数据一致性。

---

### 阶段六：收尾与测试

**目标**: 确保系统稳定、可靠。

- **T6.1**: 配置全局异常处理器，统一处理业务异常和系统异常。
- **T6.2**: 编写核心业务流程的集成测试。
- **T6.3**: 使用 OpenAPI (Swagger) 生成并完善 API 文档。
- **T6.4**: 进行最终的代码审查和重构。