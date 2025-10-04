# CQRS 引入实施任务清单

## 文档说明

本文档提供了引入 CQRS 架构的详细任务清单，每个任务都可以独立完成和验证。按照此清单执行，可以安全地将现有代码重构为 CQRS 架构，且不影响业务功能。

**执行原则**：渐进式、可回滚、持续验证

---

## 📊 任务概览

| 阶段     | 任务数    | 预计时间    | 风险等级 |
| -------- | --------- | ----------- | -------- |
| 准备阶段 | 8 个      | 1-2 天      | 🟢 低    |
| 命令实施 | 15 个     | 2-3 天      | 🟡 中    |
| 查询实施 | 12 个     | 2-3 天      | 🟢 低    |
| 缓存集成 | 6 个      | 1-2 天      | 🟡 中    |
| 清理优化 | 5 个      | 1 天        | 🟢 低    |
| **总计** | **46 个** | **7-11 天** | -        |

---

## 阶段 1：准备工作（1-2 天）

### 1.1 创建基础结构

#### 任务 1.1.1：创建命令对象目录

- **描述**：创建 `command/` 目录结构
- **操作**：
  ```bash
  mkdir -p src/main/java/com/cleveronion/blog/application/article/command
  ```
- **验证**：目录已创建
- **时间**：5 分钟
- **依赖**：无

#### 任务 1.1.2：创建命令服务文件

- **描述**：创建空的 `ArticleCommandService.java`
- **操作**：创建文件，添加基本类结构

  ```java
  @Service
  @Transactional
  public class ArticleCommandService {
      private static final Logger logger = LoggerFactory.getLogger(ArticleCommandService.class);
      private final ArticleRepository articleRepository;

      public ArticleCommandService(ArticleRepository articleRepository) {
          this.articleRepository = articleRepository;
      }
  }
  ```

- **验证**：文件编译通过，Spring 可以正常启动
- **时间**：10 分钟
- **依赖**：1.1.1

#### 任务 1.1.3：创建查询服务文件

- **描述**：创建空的 `ArticleQueryService.java`
- **操作**：创建文件，添加基本类结构

  ```java
  @Service
  @Transactional(readOnly = true)
  public class ArticleQueryService {
      private static final Logger logger = LoggerFactory.getLogger(ArticleQueryService.class);
      private final ArticleRepository articleRepository;

      public ArticleQueryService(ArticleRepository articleRepository) {
          this.articleRepository = articleRepository;
      }
  }
  ```

- **验证**：文件编译通过，Spring 可以正常启动
- **时间**：10 分钟
- **依赖**：1.1.1

### 1.2 创建领域事件基础设施

#### 任务 1.2.1：实现 DomainEventPublisher

- **描述**：实现领域事件发布器
- **文件**：`domain/common/event/DomainEventPublisher.java`
- **操作**：实现事件发布逻辑
- **验证**：单元测试通过
- **时间**：30 分钟
- **依赖**：无

#### 任务 1.2.2：完善 AggregateRoot 基类

- **描述**：添加事件管理方法
- **文件**：`domain/common/aggregate/AggregateRoot.java`
- **操作**：添加 `addDomainEvent()`, `getDomainEvents()`, `clearDomainEvents()`
- **验证**：编译通过
- **时间**：20 分钟
- **依赖**：1.2.1

### 1.3 创建命令对象

#### 任务 1.3.1：创建 CreateArticleDraftCommand

- **描述**：创建文章草稿命令对象
- **文件**：`application/article/command/CreateArticleDraftCommand.java`
- **验证**：编译通过，构造函数验证生效
- **时间**：15 分钟
- **依赖**：1.1.1

#### 任务 1.3.2：创建 UpdateArticleCommand

- **描述**：更新文章命令对象
- **文件**：`application/article/command/UpdateArticleCommand.java`
- **验证**：编译通过
- **时间**：15 分钟
- **依赖**：1.1.1

#### 任务 1.3.3：创建 PublishArticleCommand

- **描述**：发布文章命令对象
- **文件**：`application/article/command/PublishArticleCommand.java`
- **验证**：编译通过
- **时间**：10 分钟
- **依赖**：1.1.1

#### 任务 1.3.4：编写命令对象单元测试

- **描述**：为所有命令对象编写测试
- **验证**：测试覆盖率 > 90%
- **时间**：30 分钟
- **依赖**：1.3.1-1.3.3

---

## 阶段 2：命令方法实施（2-3 天）

### 2.1 实现创建类命令

#### 任务 2.1.1：实现 createDraft 命令方法

- **描述**：在 `ArticleCommandService` 中实现创建草稿方法
- **操作**：
  1. 从 `ArticleApplicationService` 复制方法
  2. 修改参数为 `CreateArticleDraftCommand`
  3. 添加 `@CacheEvict` 注解
  4. 添加事件发布逻辑
- **验证**：
  - 方法编译通过
  - 单元测试通过
  - 手动测试创建文章功能正常
- **时间**：1 小时
- **依赖**：1.3.1, 1.2.1

#### 任务 2.1.2：实现 createAndPublish 命令方法

- **描述**：实现创建并发布方法
- **验证**：单元测试通过，功能正常
- **时间**：45 分钟
- **依赖**：2.1.1

### 2.2 实现更新类命令

#### 任务 2.2.1：实现 updateContent 命令方法

- **描述**：实现更新内容方法
- **操作**：
  1. 使用 `UpdateArticleCommand`
  2. 包含权限验证
  3. 添加缓存失效
- **验证**：单元测试通过，更新功能正常
- **时间**：1 小时
- **依赖**：1.3.2

#### 任务 2.2.2：实现 addTags 命令方法

- **描述**：实现添加标签方法
- **验证**：单元测试通过
- **时间**：30 分钟
- **依赖**：2.2.1

#### 任务 2.2.3：实现 removeTags 命令方法

- **描述**：实现移除标签方法
- **验证**：单元测试通过
- **时间**：30 分钟
- **依赖**：2.2.1

### 2.3 实现状态变更命令

#### 任务 2.3.1：实现 publish 命令方法

- **描述**：实现发布文章方法
- **操作**：
  1. 验证作者权限
  2. 调用聚合根的 `publish()`
  3. 发布领域事件
  4. 清除所有缓存
- **验证**：单元测试通过，发布功能正常
- **时间**：45 分钟
- **依赖**：1.3.3, 1.2.1

#### 任务 2.3.2：实现 archive 命令方法

- **描述**：实现归档文章方法
- **验证**：单元测试通过
- **时间**：30 分钟
- **依赖**：2.3.1

#### 任务 2.3.3：实现 revertToDraft 命令方法

- **描述**：实现撤回到草稿方法
- **验证**：单元测试通过
- **时间**：30 分钟
- **依赖**：2.3.1

### 2.4 实现删除命令

#### 任务 2.4.1：实现 delete 命令方法

- **描述**：实现删除文章方法
- **操作**：
  1. 验证作者权限
  2. 删除文章
  3. 清除缓存
- **验证**：单元测试通过，删除功能正常
- **时间**：30 分钟
- **依赖**：1.1.2

### 2.5 更新控制器（命令部分）

#### 任务 2.5.1：在控制器中注入 CommandService

- **描述**：在 `ArticleController` 中添加 `ArticleCommandService` 依赖
- **操作**：
  ```java
  private final ArticleCommandService commandService;  // 新增
  private final ArticleApplicationService applicationService;  // 保留
  ```
- **验证**：编译通过，应用启动正常
- **时间**：10 分钟
- **依赖**：2.1.1

#### 任务 2.5.2：更新 createArticle 控制器方法

- **描述**：修改控制器使用新的 CommandService
- **操作**：
  1. 构建 `CreateArticleDraftCommand`
  2. 调用 `commandService.createDraft()`
  3. 保留旧代码注释备份
- **验证**：
  - 编译通过
  - Postman 测试创建文章 API 正常
  - 数据库数据正确
- **时间**：20 分钟
- **依赖**：2.5.1, 2.1.1

#### 任务 2.5.3：更新 publishArticle 控制器方法

- **描述**：修改发布文章接口
- **验证**：API 测试通过
- **时间**：15 分钟
- **依赖**：2.5.1, 2.3.1

#### 任务 2.5.4：更新 updateArticle 控制器方法

- **描述**：修改更新文章接口
- **验证**：API 测试通过
- **时间**：20 分钟
- **依赖**：2.5.1, 2.2.1

#### 任务 2.5.5：更新 deleteArticle 控制器方法

- **描述**：修改删除文章接口
- **验证**：API 测试通过
- **时间**：15 分钟
- **依赖**：2.5.1, 2.4.1

#### 任务 2.5.6：更新 archiveArticle 控制器方法

- **描述**：修改归档文章接口
- **验证**：API 测试通过
- **时间**：15 分钟
- **依赖**：2.5.1, 2.3.2

### 2.6 命令测试

#### 任务 2.6.1：编写 CommandService 单元测试

- **描述**：为所有命令方法编写单元测试
- **覆盖**：
  - 正常流程测试
  - 异常情况测试
  - 权限验证测试
- **验证**：测试覆盖率 > 85%
- **时间**：2 小时
- **依赖**：2.1-2.4 所有任务

#### 任务 2.6.2：集成测试验证命令功能

- **描述**：端到端测试所有命令接口
- **验证**：所有命令 API 功能正常
- **时间**：1 小时
- **依赖**：2.5 所有任务

---

## 阶段 3：查询方法实施（2-3 天）

### 3.1 迁移基础查询方法

#### 任务 3.1.1：实现 findById 查询方法

- **描述**：在 `ArticleQueryService` 中实现根据 ID 查询
- **操作**：
  1. 从 `ArticleApplicationService` 复制方法
  2. 确认使用 `@Transactional(readOnly = true)`
  3. 添加参数验证
- **验证**：
  - 编译通过
  - 单元测试通过
- **时间**：15 分钟
- **依赖**：1.1.3

#### 任务 3.1.2：实现 findByAuthorId 查询方法

- **描述**：实现按作者查询（带分页）
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.1.1

#### 任务 3.1.3：实现 findByCategoryId 查询方法

- **描述**：实现按分类查询
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.1.1

#### 任务 3.1.4：实现 findByTagId 查询方法

- **描述**：实现按标签查询
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.1.1

### 3.2 迁移分页查询方法

#### 任务 3.2.1：实现 findPublishedArticles 查询方法

- **描述**：实现已发布文章列表查询
- **验证**：单元测试通过
- **时间**：20 分钟
- **依赖**：3.1.1

#### 任务 3.2.2：实现 findAllArticles 查询方法

- **描述**：实现所有文章列表查询
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.2.1

#### 任务 3.2.3：迁移其他分页查询方法（批量）

- **描述**：迁移剩余的分页查询方法（约 15 个）
- **方法列表**：
  - `findPublishedByCategoryId()`
  - `findPublishedByTagId()`
  - `findByCategoryAndTag()`
  - 等
- **验证**：所有方法编译通过
- **时间**：1.5 小时
- **依赖**：3.2.1

### 3.3 迁移搜索和统计方法

#### 任务 3.3.1：实现 searchByTitle 查询方法

- **描述**：实现标题搜索
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.1.1

#### 任务 3.3.2：实现 searchByContent 查询方法

- **描述**：实现内容搜索
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.3.1

#### 任务 3.3.3：迁移所有统计方法（批量）

- **描述**：迁移所有 `countByXxx()` 方法（约 10 个）
- **验证**：所有方法编译通过
- **时间**：1 小时
- **依赖**：3.1.1

### 3.4 迁移特殊查询方法

#### 任务 3.4.1：实现 findRecentlyPublished 方法

- **描述**：实现最近发布文章查询
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.1.1

#### 任务 3.4.2：实现 findPopularArticles 方法

- **描述**：实现热门文章查询
- **验证**：单元测试通过
- **时间**：15 分钟
- **依赖**：3.4.1

### 3.5 更新控制器（查询部分）

#### 任务 3.5.1：在控制器中注入 QueryService

- **描述**：在 `ArticleController` 中添加 `ArticleQueryService` 依赖
- **验证**：编译通过，应用启动正常
- **时间**：10 分钟
- **依赖**：3.1.1

#### 任务 3.5.2：更新 getArticleById 控制器方法

- **描述**：修改控制器使用 `QueryService.findById()`
- **验证**：API 测试通过
- **时间**：15 分钟
- **依赖**：3.5.1, 3.1.1

#### 任务 3.5.3：更新 getArticles 控制器方法（列表查询）

- **描述**：修改文章列表查询接口
- **验证**：API 测试通过，分页正确
- **时间**：20 分钟
- **依赖**：3.5.1, 3.2.1

#### 任务 3.5.4：更新其他查询控制器方法（批量）

- **描述**：更新所有剩余的查询接口
- **方法**：`getArticlesByAuthor`, `searchArticles`, `getRecentArticles` 等
- **验证**：所有 API 测试通过
- **时间**：1 小时
- **依赖**：3.5.1, 3.1-3.4 所有任务

### 3.6 查询测试

#### 任务 3.6.1：编写 QueryService 单元测试

- **描述**：为所有查询方法编写单元测试
- **验证**：测试覆盖率 > 85%
- **时间**：1.5 小时
- **依赖**：3.1-3.4 所有任务

#### 任务 3.6.2：集成测试验证查询功能

- **描述**：端到端测试所有查询接口
- **验证**：所有查询 API 功能正常
- **时间**：45 分钟
- **依赖**：3.5 所有任务

---

## 阶段 4：缓存集成（1-2 天，可选）

### 4.1 缓存配置

#### 任务 4.1.1：创建 CacheConfig 配置类

- **描述**：配置 Redis CacheManager
- **文件**：`infrastructure/common/config/CacheConfig.java`
- **操作**：
  1. 添加 `@EnableCaching` 注解
  2. 配置 Redis 序列化
  3. 设置缓存过期时间
- **验证**：应用启动正常，Redis 连接成功
- **时间**：30 分钟
- **依赖**：无

#### 任务 4.1.2：配置缓存键命名策略

- **描述**：定义缓存键的命名规范
- **操作**：创建 `CacheKeyGenerator` 工具类
- **验证**：缓存键格式正确
- **时间**：20 分钟
- **依赖**：4.1.1

### 4.2 为查询方法添加缓存

#### 任务 4.2.1：为 findById 添加缓存

- **描述**：添加 `@Cacheable` 注解
- **操作**：
  ```java
  @Cacheable(value = "articles", key = "#id.value")
  public Optional<ArticleAggregate> findById(ArticleId id) { }
  ```
- **验证**：
  - 第一次查询访问数据库
  - 第二次查询从缓存读取（查看日志）
- **时间**：15 分钟
- **依赖**：4.1.1, 3.1.1

#### 任务 4.2.2：为列表查询添加缓存

- **描述**：为 `findPublishedArticles` 等方法添加缓存
- **验证**：缓存生效，查询性能提升
- **时间**：30 分钟
- **依赖**：4.2.1

#### 任务 4.2.3：为统计方法添加缓存

- **描述**：为所有 `countByXxx()` 方法添加缓存
- **验证**：缓存生效
- **时间**：30 分钟
- **依赖**：4.2.1

### 4.3 为命令方法添加缓存失效

#### 任务 4.3.1：为创建命令添加缓存失效

- **描述**：`createDraft()`, `createAndPublish()` 添加 `@CacheEvict`
- **操作**：
  ```java
  @CacheEvict(value = "article-lists", allEntries = true)
  public ArticleAggregate createDraft(...) { }
  ```
- **验证**：创建文章后，列表缓存被清除
- **时间**：20 分钟
- **依赖**：4.2.2

#### 任务 4.3.2：为更新命令添加缓存失效

- **描述**：更新方法添加缓存失效逻辑
- **操作**：
  ```java
  @CacheEvict(value = {"articles", "article-lists"}, key = "#articleId.value", allEntries = true)
  ```
- **验证**：更新后相关缓存被清除
- **时间**：20 分钟
- **依赖**：4.3.1

#### 任务 4.3.3：为删除命令添加缓存失效

- **描述**：删除方法添加缓存失效
- **验证**：删除后所有相关缓存被清除
- **时间**：15 分钟
- **依赖**：4.3.2

### 4.4 缓存测试

#### 任务 4.4.1：编写缓存集成测试

- **描述**：测试缓存的读取和失效
- **测试场景**：
  1. 首次查询，缓存未命中
  2. 二次查询，缓存命中
  3. 更新后，缓存失效
  4. 再次查询，重新缓存
- **验证**：所有场景测试通过
- **时间**：1 小时
- **依赖**：4.2, 4.3 所有任务

---

## 阶段 5：清理和优化（1 天）

### 5.1 标记旧服务为废弃

#### 任务 5.1.1：为 ArticleApplicationService 添加 @Deprecated

- **描述**：标记旧服务类和方法为废弃
- **操作**：

  ```java
  @Service
  @Deprecated  // 添加注解
  @Transactional
  public class ArticleApplicationService {

      @Deprecated  // 为每个方法添加
      public ArticleAggregate createDraft(...) { }
  }
  ```

- **验证**：编译时显示废弃警告
- **时间**：20 分钟
- **依赖**：2.5, 3.5 所有任务完成

#### 任务 5.1.2：在旧服务中添加迁移提示注释

- **描述**：添加 JavaDoc 提示使用新服务
- **操作**：
  ```java
  /**
   * @deprecated 请使用 {@link ArticleCommandService#createDraft(CreateArticleDraftCommand)}
   */
  @Deprecated
  public ArticleAggregate createDraft(...) { }
  ```
- **验证**：文档清晰
- **时间**：30 分钟
- **依赖**：5.1.1

### 5.2 验证和清理

#### 任务 5.2.1：检查旧服务引用

- **描述**：确认 `ArticleApplicationService` 无引用
- **操作**：使用 IDE 的 "Find Usages" 功能
- **验证**：只在测试代码中有引用（可接受）
- **时间**：20 分钟
- **依赖**：5.1.2

#### 任务 5.2.2：删除 ArticleApplicationService

- **描述**：删除旧服务类文件
- **前提**：确认无业务代码引用
- **操作**：删除文件或移动到 `deprecated/` 目录
- **验证**：编译通过，所有测试通过
- **时间**：10 分钟
- **依赖**：5.2.1

#### 任务 5.2.3：更新相关测试

- **描述**：更新或删除旧服务的测试
- **操作**：迁移到新服务的测试或删除
- **验证**：测试套件全部通过
- **时间**：30 分钟
- **依赖**：5.2.2

### 5.3 文档更新

#### 任务 5.3.1：更新 API 文档

- **描述**：更新 Swagger 注释，反映 CQRS 架构
- **验证**：Swagger UI 显示正确
- **时间**：20 分钟
- **依赖**：5.2.2

#### 任务 5.3.2：更新架构文档

- **描述**：更新 `architecture.md`，描述 CQRS 架构
- **验证**：文档清晰完整
- **时间**：30 分钟
- **依赖**：5.3.1

### 5.4 性能测试和优化

#### 任务 5.4.1：基准性能测试

- **描述**：对比 CQRS 前后的性能数据
- **测试**：
  - 查询接口响应时间
  - 缓存命中率
  - 数据库查询次数
- **验证**：性能有提升或持平
- **时间**：1 小时
- **依赖**：4.4.1

---

## 🔍 详细任务执行指南

### 任务 2.1.1 详细步骤：实现 createDraft 命令方法

**前置条件**：

- ✅ 已创建 `ArticleCommandService`
- ✅ 已创建 `CreateArticleDraftCommand`
- ✅ 已实现 `DomainEventPublisher`

**执行步骤**：

1. **打开文件**：`ArticleCommandService.java`

2. **添加方法**：

   ```java
   @CacheEvict(value = "article-lists", allEntries = true)
   public ArticleAggregate createDraft(CreateArticleDraftCommand command) {
       logger.debug("执行创建文章草稿命令: authorId={}, categoryId={}",
           command.getAuthorId().getValue(),
           command.getCategoryId().getValue());

       // 创建聚合
       ArticleAggregate article = ArticleAggregate.createDraft(
           command.getContent(),
           command.getCategoryId(),
           command.getAuthorId()
       );

       // 添加标签
       if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
           command.getTagIds().forEach(article::addTag);
       }

       // 保存
       ArticleAggregate savedArticle = articleRepository.save(article);

       // 发布领域事件
       if (article.getDomainEvents() != null && !article.getDomainEvents().isEmpty()) {
           eventPublisher.publish(article.getDomainEvents());
           article.clearDomainEvents();
       }

       logger.info("文章草稿创建成功: articleId={}, title={}",
           savedArticle.getId().getValue(),
           savedArticle.getContent().getTitle());

       return savedArticle;
   }
   ```

3. **编写单元测试**：

   ```java
   @Test
   void should_create_draft_successfully() {
       // Given
       CreateArticleDraftCommand command = new CreateArticleDraftCommand(
           new ArticleContent("标题", "内容", "摘要"),
           new CategoryId(1L),
           new AuthorId(1L),
           null
       );

       ArticleAggregate savedArticle = mock(ArticleAggregate.class);
       when(articleRepository.save(any())).thenReturn(savedArticle);

       // When
       ArticleAggregate result = commandService.createDraft(command);

       // Then
       assertThat(result).isEqualTo(savedArticle);
       verify(articleRepository).save(any());
   }
   ```

4. **验证**：

   - [ ] 代码编译通过
   - [ ] 单元测试通过
   - [ ] 日志输出正确

5. **提交代码**：
   ```bash
   git add .
   git commit -m "feat(article): 实现创建文章草稿命令方法"
   ```

**预计时间**：1 小时  
**完成标准**：测试通过，日志正确，可以创建文章

---

### 任务 2.5.2 详细步骤：更新 createArticle 控制器方法

**前置条件**：

- ✅ `ArticleCommandService.createDraft()` 已实现
- ✅ `CreateArticleDraftCommand` 已创建

**执行步骤**：

1. **打开文件**：`ArticleController.java`

2. **找到方法**：`createArticle(CreateArticleRequest request)`

3. **备份原代码**（注释）：

   ```java
   // 旧实现（备份）
   // ArticleAggregate article = applicationService.createDraft(content, categoryId, authorId);
   ```

4. **修改为新实现**：

   ```java
   @PostMapping
   public Result<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
       logger.info("接收到创建文章请求，标题: {}", request.getTitle());

       // 构建文章内容值对象
       ArticleContent content = new ArticleContent(
           request.getTitle(),
           request.getContent(),
           request.getSummary()
       );

       // 构建命令对象
       CreateArticleDraftCommand command = new CreateArticleDraftCommand(
           content,
           new CategoryId(request.getCategoryId()),
           new AuthorId(StpUtil.getLoginIdAsLong()),
           convertTagNames(request.getTagNames())  // 转换标签
       );

       // 执行命令
       ArticleAggregate article = commandService.createDraft(command);

       // 构建响应
       ArticleResponse response = buildArticleResponseWithEntities(article);

       logger.info("成功创建文章，ID: {}", article.getId().getValue());

       return Result.success(response);
   }
   ```

5. **测试验证**：

   ```bash
   # 使用 Postman 或 curl 测试
   curl -X POST http://localhost:8080/api/articles \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -d '{
       "title": "测试文章",
       "content": "测试内容",
       "summary": "测试摘要",
       "categoryId": 1,
       "tagNames": ["测试"]
     }'
   ```

6. **验证清单**：

   - [ ] API 返回成功
   - [ ] 数据库中创建了文章
   - [ ] 响应数据完整
   - [ ] 日志输出正确
   - [ ] 缓存被清除（如已配置）

7. **提交代码**：
   ```bash
   git add .
   git commit -m "refactor(article): 更新创建文章接口使用 CommandService"
   ```

**预计时间**：20 分钟  
**完成标准**：API 功能正常，测试通过

---

## 📋 完整任务检查清单

### 阶段 1：准备工作

- [ ] 1.1.1 创建命令对象目录
- [ ] 1.1.2 创建命令服务文件
- [ ] 1.1.3 创建查询服务文件
- [ ] 1.2.1 实现 DomainEventPublisher
- [ ] 1.2.2 完善 AggregateRoot 基类
- [ ] 1.3.1 创建 CreateArticleDraftCommand
- [ ] 1.3.2 创建 UpdateArticleCommand
- [ ] 1.3.3 创建 PublishArticleCommand
- [ ] 1.3.4 编写命令对象单元测试

### 阶段 2：命令实施

- [ ] 2.1.1 实现 createDraft 命令方法
- [ ] 2.1.2 实现 createAndPublish 命令方法
- [ ] 2.2.1 实现 updateContent 命令方法
- [ ] 2.2.2 实现 addTags 命令方法
- [ ] 2.2.3 实现 removeTags 命令方法
- [ ] 2.3.1 实现 publish 命令方法
- [ ] 2.3.2 实现 archive 命令方法
- [ ] 2.3.3 实现 revertToDraft 命令方法
- [ ] 2.4.1 实现 delete 命令方法
- [ ] 2.5.1 在控制器中注入 CommandService
- [ ] 2.5.2 更新 createArticle 控制器方法
- [ ] 2.5.3 更新 publishArticle 控制器方法
- [ ] 2.5.4 更新 updateArticle 控制器方法
- [ ] 2.5.5 更新 deleteArticle 控制器方法
- [ ] 2.5.6 更新 archiveArticle 控制器方法
- [ ] 2.6.1 编写 CommandService 单元测试
- [ ] 2.6.2 集成测试验证命令功能

### 阶段 3：查询实施

- [ ] 3.1.1 实现 findById 查询方法
- [ ] 3.1.2 实现 findByAuthorId 查询方法
- [ ] 3.1.3 实现 findByCategoryId 查询方法
- [ ] 3.1.4 实现 findByTagId 查询方法
- [ ] 3.2.1 实现 findPublishedArticles 查询方法
- [ ] 3.2.2 实现 findAllArticles 查询方法
- [ ] 3.2.3 迁移其他分页查询方法
- [ ] 3.3.1 实现 searchByTitle 查询方法
- [ ] 3.3.2 实现 searchByContent 查询方法
- [ ] 3.3.3 迁移所有统计方法
- [ ] 3.4.1 实现 findRecentlyPublished 方法
- [ ] 3.4.2 实现 findPopularArticles 方法
- [ ] 3.5.1 在控制器中注入 QueryService
- [ ] 3.5.2 更新 getArticleById 控制器方法
- [ ] 3.5.3 更新 getArticles 控制器方法
- [ ] 3.5.4 更新其他查询控制器方法
- [ ] 3.6.1 编写 QueryService 单元测试
- [ ] 3.6.2 集成测试验证查询功能

### 阶段 4：缓存集成（可选）

- [ ] 4.1.1 创建 CacheConfig 配置类
- [ ] 4.1.2 配置缓存键命名策略
- [ ] 4.2.1 为 findById 添加缓存
- [ ] 4.2.2 为列表查询添加缓存
- [ ] 4.2.3 为统计方法添加缓存
- [ ] 4.3.1 为创建命令添加缓存失效
- [ ] 4.3.2 为更新命令添加缓存失效
- [ ] 4.3.3 为删除命令添加缓存失效
- [ ] 4.4.1 编写缓存集成测试

### 阶段 5：清理优化

- [ ] 5.1.1 为 ArticleApplicationService 添加 @Deprecated
- [ ] 5.1.2 添加迁移提示注释
- [ ] 5.2.1 检查旧服务引用
- [ ] 5.2.2 删除 ArticleApplicationService
- [ ] 5.2.3 更新相关测试
- [ ] 5.3.1 更新 API 文档
- [ ] 5.3.2 更新架构文档
- [ ] 5.4.1 基准性能测试

---

## 🚨 风险控制和回滚方案

### 每个阶段的验证点

#### 阶段 1 验证

- ✅ 应用可以正常启动
- ✅ 所有测试通过
- ✅ 无编译错误

#### 阶段 2 验证

- ✅ 所有命令 API 功能正常
- ✅ 数据正确写入数据库
- ✅ 原有功能不受影响

#### 阶段 3 验证

- ✅ 所有查询 API 功能正常
- ✅ 查询结果正确
- ✅ 分页功能正常

#### 阶段 4 验证

- ✅ 缓存读取正常
- ✅ 缓存失效正确
- ✅ 性能有提升

#### 阶段 5 验证

- ✅ 旧代码已清理
- ✅ 所有测试通过
- ✅ 文档已更新

### 回滚方案

每个阶段都使用 Git 分支，便于回滚：

```bash
# 阶段 1
git checkout -b cqrs/phase-1-preparation
# ... 完成任务 ...
git commit -m "完成 CQRS 准备阶段"

# 阶段 2
git checkout -b cqrs/phase-2-commands
# ... 完成任务 ...
git commit -m "完成 CQRS 命令实施"

# 如果有问题，可以立即回滚
git checkout main
git branch -D cqrs/phase-2-commands
```

---

## 📊 进度跟踪

### 每日站会检查点

**Day 1-2：准备阶段**

- 目标：完成阶段 1 的 8 个任务
- 检查：应用能否正常启动？

**Day 3-5：命令实施**

- 目标：完成阶段 2 的 15 个任务
- 检查：所有命令 API 是否正常？

**Day 6-8：查询实施**

- 目标：完成阶段 3 的 12 个任务
- 检查：所有查询 API 是否正常？

**Day 9-10：缓存集成**

- 目标：完成阶段 4 的 6 个任务
- 检查：缓存是否生效？

**Day 11：清理优化**

- 目标：完成阶段 5 的 5 个任务
- 检查：是否可以发布？

---

## 🎯 关键里程碑

### 里程碑 1：准备完成（Day 2）

- ✅ 基础结构创建完成
- ✅ 命令对象全部创建
- ✅ 事件发布机制就绪

### 里程碑 2：命令侧完成（Day 5）

- ✅ 所有命令方法迁移完成
- ✅ 控制器命令接口更新完成
- ✅ 命令功能测试通过

### 里程碑 3：查询侧完成（Day 8）

- ✅ 所有查询方法迁移完成
- ✅ 控制器查询接口更新完成
- ✅ 查询功能测试通过

### 里程碑 4：缓存完成（Day 10）

- ✅ 缓存配置完成
- ✅ 查询缓存生效
- ✅ 缓存失效正确

### 里程碑 5：CQRS 上线（Day 11）

- ✅ 旧代码清理完成
- ✅ 文档更新完成
- ✅ 性能测试通过
- ✅ 准备发布

---

## 💡 最佳实践建议

### 执行建议

1. **每完成一个任务立即提交** - 方便回滚
2. **先测试后继续** - 确保每步都正确
3. **保留旧代码注释** - 便于对比和回滚
4. **并存期测试充分** - 新旧代码都可用时多测试

### 团队协作

1. **任务分配**：

   - 高级开发：负责命令实施（阶段 2）
   - 中级开发：负责查询实施（阶段 3）
   - 架构师：负责准备和审查

2. **代码审查**：

   - 每个阶段完成后进行 Code Review
   - 重点检查：职责分离、事务管理、缓存策略

3. **测试策略**：
   - 每个方法迁移后立即测试
   - 每个阶段完成后进行集成测试
   - 最终进行端到端测试

---

## 📝 任务模板

### 新增任务模板

```markdown
#### 任务 X.X.X：任务标题

- **描述**：简要描述任务内容
- **文件**：涉及的文件路径
- **操作**：
  1. 具体步骤 1
  2. 具体步骤 2
  3. ...
- **验证**：
  - [ ] 验证点 1
  - [ ] 验证点 2
- **时间**：预计时间
- **依赖**：依赖的任务编号
```

### 问题记录模板

```markdown
#### 问题记录 [日期]

**任务**：X.X.X  
**问题**：描述遇到的问题  
**原因**：问题的根本原因  
**解决方案**：如何解决的  
**预防措施**：如何避免再次发生
```

---

## 🔗 相关资源

### 实施指南

- [CQRS 架构引入方案](./cqrs-implementation-guide.md) - 完整技术方案
- [CQRS 快速参考手册](./cqrs-quick-reference.md) - 代码模板和速查
- [CQRS 查询对象指南](./cqrs-query-object-guide.md) - 查询对象使用说明

### 参考代码

- [现有 ArticleApplicationService](../../src/main/java/com/cleveronion/blog/application/article/service/ArticleApplicationService.java)
- [ArticleController](../../src/main/java/com/cleveronion/blog/presentation/api/controller/ArticleController.java)

---

## 📅 建议执行时间表

### 第 1 周

| 日期  | 任务                              | 负责人 | 状态 |
| ----- | --------------------------------- | ------ | ---- |
| Day 1 | 阶段 1.1-1.2（准备基础结构）      | 架构师 | ⬜   |
| Day 2 | 阶段 1.3（创建命令对象）          | 开发 A | ⬜   |
| Day 3 | 阶段 2.1-2.2（实现创建/更新命令） | 开发 A | ⬜   |
| Day 4 | 阶段 2.3-2.4（实现状态/删除命令） | 开发 A | ⬜   |
| Day 5 | 阶段 2.5（更新控制器命令部分）    | 开发 A | ⬜   |

### 第 2 周

| 日期   | 任务                                | 负责人 | 状态 |
| ------ | ----------------------------------- | ------ | ---- |
| Day 6  | 阶段 2.6（命令测试） + 阶段 3.1-3.2 | 开发 B | ⬜   |
| Day 7  | 阶段 3.3-3.4（搜索/统计查询）       | 开发 B | ⬜   |
| Day 8  | 阶段 3.5（更新控制器查询部分）      | 开发 B | ⬜   |
| Day 9  | 阶段 3.6（查询测试） + 阶段 4.1-4.2 | 开发 C | ⬜   |
| Day 10 | 阶段 4.3-4.4（缓存失效和测试）      | 开发 C | ⬜   |
| Day 11 | 阶段 5（清理优化）                  | 架构师 | ⬜   |

---

## ✅ 完成标准

### 功能完整性

- [ ] 所有原有 API 接口功能正常
- [ ] 所有数据操作正确
- [ ] 权限验证正常
- [ ] 异常处理正确

### 代码质量

- [ ] 命令服务测试覆盖率 > 85%
- [ ] 查询服务测试覆盖率 > 85%
- [ ] 无编译警告
- [ ] 代码符合规范

### 性能指标

- [ ] 查询性能不下降（缓存后应提升）
- [ ] 命令性能持平
- [ ] 缓存命中率 > 70%（如启用缓存）

### 文档更新

- [ ] API 文档已更新
- [ ] 架构文档已更新
- [ ] 代码注释完整

---

**任务执行人**：请在完成每个任务后，在对应的复选框打勾，并记录实际用时和遇到的问题。

**问题反馈**：执行过程中遇到问题，请记录在文档末尾的"问题记录"部分。
