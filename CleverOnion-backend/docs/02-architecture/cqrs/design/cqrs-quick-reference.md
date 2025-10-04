# CQRS 快速参考手册

## 📌 核心概念

### CQRS 是什么？

**命令查询职责分离（CQRS）** = 将系统操作分为：

- **命令（Command）**: 改变状态（写操作）
- **查询（Query）**: 读取状态（读操作）

### 为什么使用 CQRS？

✅ 职责清晰  
✅ 独立优化  
✅ 易于扩展  
✅ 性能提升

---

## 🎯 快速识别

### 如何判断是命令还是查询？

| 类型     | 特征            | 示例                                                    |
| -------- | --------------- | ------------------------------------------------------- |
| **命令** | 改变系统状态    | `createArticle()`, `updateContent()`, `deleteArticle()` |
| **命令** | 使用写事务      | `@Transactional`                                        |
| **命令** | 返回聚合或 void | `ArticleAggregate` or `void`                            |
| **查询** | 不改变状态      | `findById()`, `searchByTitle()`, `countArticles()`      |
| **查询** | 使用只读事务    | `@Transactional(readOnly = true)`                       |
| **查询** | 可以缓存        | `@Cacheable`                                            |

---

## 📁 标准目录结构

```
application/article/
├── command/
│   ├── CreateArticleDraftCommand.java
│   ├── UpdateArticleCommand.java
│   ├── PublishArticleCommand.java
│   └── DeleteArticleCommand.java
│
└── service/
    ├── ArticleCommandService.java  ← 命令服务
    └── ArticleQueryService.java    ← 查询服务
```

---

## 💻 代码模板

### 命令服务模板

```java
@Service
@Transactional  // 写事务
public class ArticleCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleCommandService.class);
    private final ArticleRepository repository;
    private final DomainEventPublisher eventPublisher;

    @CacheEvict(value = "articles", allEntries = true)
    public ArticleAggregate executeCommand(Command cmd) {
        logger.debug("执行命令: {}", cmd);

        // 1. 验证
        // 2. 执行业务逻辑
        // 3. 保存
        // 4. 发布事件
        // 5. 记录日志

        return result;
    }
}
```

### 查询服务模板

```java
@Service
@Transactional(readOnly = true)  // 只读事务
public class ArticleQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleQueryService.class);
    private final ArticleRepository repository;

    @Cacheable(value = "articles", key = "#id")
    public Optional<Article> findById(ArticleId id) {
        logger.debug("查询: id={}", id);
        return repository.findById(id);
    }
}
```

### 命令对象模板

```java
public class SomeCommand {
    private final Type param1;
    private final Type param2;

    public SomeCommand(Type param1, Type param2) {
        this.param1 = requireNonNull(param1, "param1不能为空");
        this.param2 = requireNonNull(param2, "param2不能为空");
    }

    // Only getters, no setters
    public Type getParam1() { return param1; }
    public Type getParam2() { return param2; }
}
```

---

## 🔧 控制器使用

### Before (传统方式)

```java
@RestController
public class ArticleController {

    private final ArticleApplicationService service;

    @PostMapping("/articles")
    public Result<ArticleResponse> create(@RequestBody CreateRequest req) {
        ArticleAggregate article = service.createDraft(...);
        return Result.success(article);
    }

    @GetMapping("/articles/{id}")
    public Result<ArticleResponse> get(@PathVariable Long id) {
        Optional<ArticleAggregate> article = service.findById(...);
        return Result.success(article);
    }
}
```

### After (CQRS 方式)

```java
@RestController
public class ArticleController {

    private final ArticleCommandService commandService;  // 命令
    private final ArticleQueryService queryService;      // 查询

    @PostMapping("/articles")
    public Result<ArticleResponse> create(@RequestBody CreateRequest req) {
        // 构建命令 → 执行命令
        CreateArticleDraftCommand cmd = buildCommand(req);
        ArticleAggregate article = commandService.createDraft(cmd);
        return Result.success(article);
    }

    @GetMapping("/articles/{id}")
    public Result<ArticleResponse> get(@PathVariable Long id) {
        // 直接查询（带缓存）
        Optional<ArticleAggregate> article = queryService.findById(...);
        return Result.success(article);
    }
}
```

---

## 🗺️ 方法迁移映射表

### ArticleApplicationService → CQRS Services

| 原方法                    | 迁移到           | 类型 | 备注           |
| ------------------------- | ---------------- | ---- | -------------- |
| `createDraft()`           | `CommandService` | 命令 | 添加缓存失效   |
| `updateContent()`         | `CommandService` | 命令 | 添加缓存失效   |
| `publishArticle()`        | `CommandService` | 命令 | 清除所有缓存   |
| `deleteArticle()`         | `CommandService` | 命令 | 清除所有缓存   |
| `findById()`              | `QueryService`   | 查询 | 添加缓存       |
| `findPublishedArticles()` | `QueryService`   | 查询 | 添加缓存       |
| `searchByTitle()`         | `QueryService`   | 查询 | 不缓存（动态） |
| `countByAuthorId()`       | `QueryService`   | 查询 | 添加缓存       |

---

## 🎨 缓存策略速查

### 缓存配置

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10));
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

### 缓存注解

| 注解          | 用途     | 位置     |
| ------------- | -------- | -------- |
| `@Cacheable`  | 缓存结果 | 查询方法 |
| `@CacheEvict` | 清除缓存 | 命令方法 |
| `@CachePut`   | 更新缓存 | 极少使用 |
| `@Caching`    | 组合操作 | 复杂场景 |

### 缓存键设计

```java
// 单个实体
@Cacheable(value = "articles", key = "#id")

// 列表查询
@Cacheable(value = "article-lists", key = "'published:' + #page + ':' + #size")

// 统计查询
@Cacheable(value = "stats", key = "'author-count:' + #authorId")

// 组合条件
@Cacheable(value = "articles", key = "'cat:' + #catId + ':tag:' + #tagId")
```

---

## ⚡ 常用命令

### 创建新命令

```bash
# 1. 创建命令对象
touch application/article/command/YourCommand.java

# 2. 在 CommandService 中添加方法
vim application/article/service/ArticleCommandService.java

# 3. 更新控制器
vim presentation/api/controller/ArticleController.java
```

### 创建新查询

```bash
# 1. 在 QueryService 中添加方法
vim application/article/service/ArticleQueryService.java

# 2. 添加缓存注解（如需要）
# 3. 更新控制器
```

---

## ✅ 实施检查清单

### Phase 1: 准备

- [ ] 创建 `command/` 目录
- [ ] 创建 `ArticleCommandService.java`
- [ ] 创建 `ArticleQueryService.java`
- [ ] 配置依赖注入

### Phase 2: 迁移命令

- [ ] 识别所有命令方法
- [ ] 创建命令对象
- [ ] 迁移到 CommandService
- [ ] 添加 `@CacheEvict` 注解
- [ ] 更新控制器引用

### Phase 3: 迁移查询

- [ ] 识别所有查询方法
- [ ] 迁移到 QueryService
- [ ] 添加 `@Cacheable` 注解
- [ ] 更新控制器引用

### Phase 4: 缓存配置

- [ ] 启用 Spring Cache
- [ ] 配置 Redis CacheManager
- [ ] 设计缓存键策略
- [ ] 配置过期时间

### Phase 5: 测试

- [ ] 编写命令服务测试
- [ ] 编写查询服务测试
- [ ] 集成测试
- [ ] 性能测试

### Phase 6: 清理

- [ ] 标记旧服务 @Deprecated
- [ ] 确认无引用
- [ ] 删除旧服务
- [ ] 更新文档

---

## ⚠️ 常见陷阱

| 陷阱               | 后果                 | 解决方案                               |
| ------------------ | -------------------- | -------------------------------------- |
| 查询方法使用写事务 | 性能下降，锁竞争     | 使用 `@Transactional(readOnly = true)` |
| 忘记清除缓存       | 数据不一致           | 命令方法添加 `@CacheEvict`             |
| 过度缓存           | 内存压力，一致性问题 | 只缓存高频查询                         |
| 缓存键冲突         | 数据混乱             | 使用清晰的命名约定                     |
| 命令对象可变       | 并发问题             | 使用 final 字段                        |
| 缺少事件发布       | 业务逻辑不完整       | 在命令方法中发布事件                   |

---

## 📊 性能对比

### 预期性能提升

| 场景         | 改进前 | 改进后 | 提升         |
| ------------ | ------ | ------ | ------------ |
| 单篇文章查询 | 20ms   | 1-2ms  | **10-20 倍** |
| 文章列表查询 | 100ms  | 5-10ms | **10-20 倍** |
| 统计查询     | 50ms   | 1ms    | **50 倍**    |
| 创建文章     | 30ms   | 30ms   | 无变化       |
| 更新文章     | 25ms   | 25ms   | 无变化       |

_注：具体数值取决于缓存命中率和数据量_

---

## 🔗 快速链接

- [完整实施指南](./cqrs-implementation-guide.md) - 详细的方案和步骤
- [代码示例](#6-代码示例) - 完整的代码示例
- [迁移策略](#7-迁移策略) - 如何平滑迁移
- [最佳实践](#10-最佳实践) - 设计原则和注意事项

---

## 💡 一句话总结

> **将改变状态的操作（命令）和读取状态的操作（查询）分离到不同的服务中，各自优化，互不影响。**

---

## ❓ 常见疑问

### Q: 查询需要像命令那样使用专门的 Query 对象吗？

**A**: **大多数情况不需要**。

- ✅ **命令** - 总是使用 Command 对象
- ⚠️ **查询** - 简单查询直接传参，复杂查询（参数>5 个）可选使用 Query 对象

详见：[CQRS 查询对象使用指南](./cqrs-query-object-guide.md)

```java
// ✅ 命令 - 必须使用对象
commandService.createDraft(CreateArticleDraftCommand command);

// ✅ 简单查询 - 直接传参（推荐）
queryService.findById(ArticleId id);
queryService.findPublishedArticles(int page, int size);

// ⚠️ 复杂查询 - 可选使用Query对象
queryService.search(ArticleSearchQuery query);  // 参数很多时
```

---

**需要帮助？** 查看 [完整实施指南](./cqrs-implementation-guide.md) 或联系架构组。
