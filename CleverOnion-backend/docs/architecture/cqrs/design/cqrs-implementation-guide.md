# CQRS 架构引入方案

## 文档说明

本文档详细阐述了在 CleverOnion 后端项目中引入 CQRS（Command Query Responsibility Segregation，命令查询职责分离）架构模式的完整方案，包括设计思路、实施步骤、代码示例和最佳实践。

**文档版本**: 1.0  
**创建日期**: 2025-10-01  
**适用版本**: 0.0.1-SNAPSHOT 及以上

---

## 📋 目录

1. [CQRS 概述](#1-cqrs-概述)
2. [为什么引入 CQRS](#2-为什么引入-cqrs)
3. [当前问题分析](#3-当前问题分析)
4. [CQRS 架构设计](#4-cqrs-架构设计)
5. [详细实施步骤](#5-详细实施步骤)
6. [代码示例](#6-代码示例)
7. [迁移策略](#7-迁移策略)
8. [测试策略](#8-测试策略)
9. [性能优化](#9-性能优化)
10. [最佳实践](#10-最佳实践)
11. [常见问题](#11-常见问题)

---

## 1. CQRS 概述

### 1.1 什么是 CQRS

CQRS（Command Query Responsibility Segregation）是一种架构模式，它将系统的操作分为两类：

- **命令（Command）**: 改变系统状态的操作（创建、更新、删除）
- **查询（Query）**: 读取系统状态的操作（查询、搜索、统计）

核心思想：**将读操作和写操作分离到不同的服务中**。

### 1.2 CQRS 的核心原则

```
传统架构:
┌─────────────────────────────────┐
│   ApplicationService            │
│  ├── create()                   │
│  ├── update()                   │
│  ├── delete()                   │
│  ├── findById()                 │
│  ├── findByXxx()                │
│  └── countByXxx()               │
└─────────────────────────────────┘

CQRS 架构:
┌──────────────────┐    ┌──────────────────┐
│ CommandService   │    │  QueryService    │
│  ├── create()    │    │  ├── findById()  │
│  ├── update()    │    │  ├── findByXxx() │
│  └── delete()    │    │  └── countByXxx()│
└──────────────────┘    └──────────────────┘
        ↓                        ↓
    写模型                    读模型
```

### 1.3 CQRS 的优势

| 优势         | 说明                                         |
| ------------ | -------------------------------------------- |
| **职责清晰** | 命令和查询的职责明确分离，代码更易理解和维护 |
| **独立优化** | 读写操作可以独立优化，互不影响               |
| **独立扩展** | 读写服务可以独立扩展，提高系统可扩展性       |
| **性能提升** | 查询服务可以使用缓存、读副本等优化手段       |
| **事务简化** | 命令服务专注于业务逻辑和数据一致性           |
| **符合 DDD** | 更好地体现领域驱动设计的思想                 |

---

## 2. 为什么引入 CQRS

### 2.1 业务需求

CleverOnion 博客系统具有以下特征：

1. **读多写少**: 文章浏览量远大于创建/编辑量
2. **复杂查询**: 多维度的文章查询（按分类、标签、作者、状态等）
3. **性能要求**: 列表页面需要快速响应
4. **独立演进**: 读写逻辑可能有不同的发展方向

### 2.2 技术收益

| 收益             | 具体表现                              |
| ---------------- | ------------------------------------- |
| **代码质量提升** | 单个服务类从 1028 行缩减到 300-400 行 |
| **可维护性提升** | 命令和查询逻辑分离，修改影响范围更小  |
| **可测试性提升** | 测试用例更加专注和简洁                |
| **性能提升**     | 查询服务可以引入缓存、索引优化等      |
| **扩展性提升**   | 读写服务可以独立部署和扩展            |

### 2.3 架构演进

CQRS 是向更高级架构模式演进的基础：

```
当前阶段      CQRS 阶段          事件溯源阶段        微服务阶段
───────────   ───────────────   ─────────────────   ──────────────
单一服务      读写分离           事件驱动             服务拆分
  │              │                 │                    │
  │              │                 │                    │
  ▼              ▼                 ▼                    ▼
复杂度低      复杂度中           复杂度高             复杂度很高
灵活性低      灵活性中           灵活性高             灵活性很高
```

---

## 3. 当前问题分析

### 3.1 代码统计

**ArticleApplicationService** 当前状态：

| 指标         | 数值         |
| ------------ | ------------ |
| 总行数       | 1028 行      |
| 总方法数     | 52 个        |
| 命令方法     | 12 个（23%） |
| 查询方法     | 40 个（77%） |
| 平均方法长度 | 约 20 行     |

### 3.2 方法分类

#### 命令方法（写操作）

```java
// 创建相关
- createDraft()
- createAndPublishArticle()

// 更新相关
- updateContent()
- updateCategory()
- addTags()
- removeTags()

// 状态变更
- publishArticle()
- archiveArticle()
- revertToDraft()

// 删除相关
- deleteArticle()
```

#### 查询方法（读操作）

```java
// 基础查询
- findById()
- findByAuthorId()
- findByCategoryId()
- findByTagId()
- findByStatus()

// 分页查询（约20个方法）
- findPublishedArticles()
- findByAuthorId(page, size)
- findByCategoryId(page, size)
- findPublishedByCategoryId()
- findPublishedByTagId()
- findByCategoryAndTag()
- findAllArticles()
- ...

// 搜索
- searchByTitle()
- searchByContent()

// 统计（约10个方法）
- countByAuthorId()
- countByCategoryId()
- countByTagId()
- countByStatus()
- countPublishedArticles()
- ...

// 特殊查询
- findRecentlyPublished()
- findPopularArticles()
```

### 3.3 存在的问题

#### 问题 1: 职责过重 🔴

```java
@Service
@Transactional
public class ArticleApplicationService {
    // 52个方法混在一起，职责不清晰
    // 违反单一职责原则（SRP）
}
```

**影响**:

- 代码理解困难
- 修改风险大
- 测试复杂

#### 问题 2: 事务管理混乱 🟡

```java
@Transactional  // 类级别默认写事务
public class ArticleApplicationService {

    @Transactional(readOnly = true)  // 方法级别改为只读事务
    public List<ArticleAggregate> findById() { }
}
```

**影响**:

- 部分查询方法使用了写事务
- 影响性能和数据库连接池

#### 问题 3: 优化空间受限 🟡

```java
// 查询方法无法独立优化
public List<ArticleAggregate> findPublishedArticles() {
    // 无法方便地引入缓存
    // 无法使用读副本
    // 无法独立调优
}
```

#### 问题 4: 难以扩展 🟢

- 读写操作无法独立扩展
- 无法针对读多写少的场景优化
- 难以引入缓存策略

---

## 4. CQRS 架构设计

### 4.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│                    (ArticleController)                       │
└────────────────────────┬───────────────────┬────────────────┘
                         │                   │
            ┌────────────▼──────────┐   ┌───▼──────────────┐
            │  Command Service      │   │  Query Service   │
            │  (写操作)             │   │  (读操作)        │
            ├───────────────────────┤   ├──────────────────┤
            │ • createDraft()       │   │ • findById()     │
            │ • updateContent()     │   │ • findByXxx()    │
            │ • publishArticle()    │   │ • searchByXxx()  │
            │ • deleteArticle()     │   │ • countByXxx()   │
            └───────────┬───────────┘   └────────┬─────────┘
                        │                        │
            ┌───────────▼───────────┐   ┌────────▼─────────┐
            │   Write Model         │   │   Read Model     │
            │   (完整聚合)          │   │   (可优化)       │
            └───────────┬───────────┘   └────────┬─────────┘
                        │                        │
            ┌───────────▼────────────────────────▼─────────┐
            │              Repository Layer                 │
            │              (Infrastructure)                 │
            └───────────────────────────────────────────────┘
                                 │
            ┌────────────────────▼─────────────────────────┐
            │              Database (PostgreSQL)            │
            └───────────────────────────────────────────────┘
```

### 4.2 服务划分

#### 4.2.1 命令服务 (ArticleCommandService)

**职责**: 处理所有会改变系统状态的操作

```java
@Service
@Transactional  // 默认写事务
public class ArticleCommandService {

    // 创建操作
    ArticleAggregate createDraft(...);
    ArticleAggregate createAndPublishArticle(...);

    // 更新操作
    ArticleAggregate updateContent(...);
    ArticleAggregate updateCategory(...);
    ArticleAggregate addTags(...);
    ArticleAggregate removeTags(...);

    // 状态变更
    ArticleAggregate publishArticle(...);
    ArticleAggregate archiveArticle(...);
    ArticleAggregate revertToDraft(...);

    // 删除操作
    void deleteArticle(...);
}
```

**特点**:

- ✅ 使用写事务
- ✅ 包含业务规则验证
- ✅ 权限检查
- ✅ 发布领域事件
- ✅ 操作日志记录

#### 4.2.2 查询服务 (ArticleQueryService)

**职责**: 处理所有读取操作

```java
@Service
@Transactional(readOnly = true)  // 只读事务
public class ArticleQueryService {

    // 基础查询
    Optional<ArticleAggregate> findById(...);
    List<ArticleAggregate> findByAuthorId(...);
    List<ArticleAggregate> findByCategoryId(...);

    // 分页查询
    List<ArticleAggregate> findPublishedArticles(int page, int size);
    List<ArticleAggregate> findByAuthorId(AuthorId, int page, int size);
    // ... 其他分页查询

    // 搜索
    List<ArticleAggregate> searchByTitle(String keyword);
    List<ArticleAggregate> searchByContent(String keyword);

    // 统计
    long countByAuthorId(...);
    long countByCategoryId(...);
    long countPublishedArticles();
    // ... 其他统计方法

    // 特殊查询
    List<ArticleAggregate> findRecentlyPublished(int limit);
    List<ArticleAggregate> findPopularArticles(int limit);
}
```

**特点**:

- ✅ 使用只读事务
- ✅ 可以引入缓存
- ✅ 可以使用读副本
- ✅ 性能优化友好
- ✅ 方法命名更清晰

### 4.3 目录结构

```
application/article/
├── command/                      # 命令层
│   ├── CreateArticleDraftCommand.java
│   ├── PublishArticleCommand.java
│   ├── UpdateArticleCommand.java
│   └── DeleteArticleCommand.java
│
├── service/
│   ├── ArticleCommandService.java    # 命令服务
│   └── ArticleQueryService.java      # 查询服务
│
└── eventhandler/                 # 事件处理器
    └── ArticleEventHandler.java
```

### 4.4 数据流向

#### 写操作流向

```
Controller
    │
    ├─ 构建 Command 对象
    │
    ▼
CommandService
    │
    ├─ 验证权限
    ├─ 调用领域模型
    ├─ 保存数据
    ├─ 发布事件
    │
    ▼
Repository
    │
    ▼
Database
    │
    ▼
EventHandler (可选)
    │
    ├─ 更新缓存
    ├─ 发送通知
    └─ 更新搜索索引
```

#### 读操作流向

```
Controller
    │
    ▼
QueryService
    │
    ├─ 检查缓存 ───┐
    │              │ 缓存命中
    │              └──────────┐
    ├─ 缓存未命中  │          │
    │              │          │
    ▼              │          │
Repository         │          │
    │              │          │
    ▼              │          │
Database           │          │
    │              │          │
    ├─ 更新缓存────┘          │
    │                         │
    ▼                         │
返回结果 ◄───────────────────┘
```

---

## 5. 详细实施步骤

### 5.1 第一阶段：准备工作（1-2 天）

#### 步骤 1: 创建命令对象

在 `application/article/command/` 目录下创建命令对象：

```java
// CreateArticleDraftCommand.java
public class CreateArticleDraftCommand {
    private final ArticleContent content;
    private final CategoryId categoryId;
    private final AuthorId authorId;
    private final Set<TagId> tagIds;

    // 构造函数、getters
}
```

> **💡 注意**：查询操作通常**不需要**专门的 Query 对象，直接传递参数即可。
> 只有参数很多（>5 个）或查询逻辑复杂时才考虑使用 Query 对象。
> 详见：[CQRS 查询对象使用指南](./cqrs-query-object-guide.md)

#### 步骤 2: 创建服务类

创建两个新的服务类：

- `ArticleCommandService.java` - 使用 Command 对象
- `ArticleQueryService.java` - 查询方法直接传参（推荐）

#### 步骤 3: 配置依赖注入

确保 Spring 可以正确注入新的服务：

```java
@Service
@Transactional
public class ArticleCommandService {
    private final ArticleRepository articleRepository;
    private final DomainEventPublisher eventPublisher;

    public ArticleCommandService(
        ArticleRepository articleRepository,
        DomainEventPublisher eventPublisher) {
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }
}
```

### 5.2 第二阶段：迁移命令方法（2-3 天）

#### 步骤 1: 识别命令方法

从 `ArticleApplicationService` 中识别所有命令方法：

```java
// 命令方法特征：
// 1. 改变系统状态
// 2. 返回聚合或 void
// 3. 使用写事务
```

#### 步骤 2: 逐个迁移

将命令方法迁移到 `ArticleCommandService`：

```java
// 从 ArticleApplicationService 移动到 ArticleCommandService
public ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
    logger.debug("开始创建文章草稿: authorId={}", authorId.getValue());

    // 创建聚合
    ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);

    // 保存
    ArticleAggregate savedArticle = articleRepository.save(article);

    // 发布事件
    eventPublisher.publish(article.getDomainEvents());
    article.clearDomainEvents();

    logger.info("文章草稿创建成功: articleId={}", savedArticle.getId().getValue());
    return savedArticle;
}
```

#### 步骤 3: 更新控制器引用

```java
@RestController
public class ArticleController {

    private final ArticleCommandService commandService;  // 新增
    private final ArticleQueryService queryService;       // 新增
    // private final ArticleApplicationService service;   // 待删除

    @PostMapping
    public Result<ArticleResponse> createArticle(@RequestBody CreateArticleRequest request) {
        // 调用命令服务
        ArticleAggregate article = commandService.createDraft(...);
        return Result.success(buildResponse(article));
    }
}
```

### 5.3 第三阶段：迁移查询方法（2-3 天）

#### 步骤 1: 迁移查询方法

将所有查询方法迁移到 `ArticleQueryService`：

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    private final ArticleRepository articleRepository;

    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        return articleRepository.findById(articleId);
    }

    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("分页参数无效");
        }
        return articleRepository.findPublishedArticles(page, size);
    }

    // ... 其他查询方法
}
```

#### 步骤 2: 更新控制器引用

```java
@RestController
public class ArticleController {

    @GetMapping("/{id}")
    public Result<ArticleResponse> getArticleById(@PathVariable Long id) {
        // 调用查询服务
        Optional<ArticleAggregate> article = queryService.findById(new ArticleId(id));
        return article.map(a -> Result.success(buildResponse(a)))
                     .orElse(Result.notFound("文章不存在"));
    }

    @GetMapping
    public Result<ArticleListResponse> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 调用查询服务
        List<ArticleAggregate> articles = queryService.findPublishedArticles(page, size);
        long total = queryService.countPublishedArticles();
        return Result.success(new ArticleListResponse(articles, total, page, size));
    }
}
```

### 5.4 第四阶段：引入缓存（可选，1-2 天）

#### 步骤 1: 配置缓存

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

#### 步骤 2: 添加缓存注解

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    @Cacheable(value = "articles", key = "#articleId.value")
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        return articleRepository.findById(articleId);
    }

    @Cacheable(value = "article-lists",
               key = "'published:' + #page + ':' + #size")
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        return articleRepository.findPublishedArticles(page, size);
    }
}
```

#### 步骤 3: 缓存失效

在命令服务中清除相关缓存：

```java
@Service
@Transactional
public class ArticleCommandService {

    @CacheEvict(value = "articles", key = "#articleId.value")
    public ArticleAggregate updateContent(ArticleId articleId, ArticleContent newContent, AuthorId authorId) {
        // 更新逻辑
    }

    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate publishArticle(ArticleId articleId, AuthorId authorId) {
        // 发布逻辑
    }
}
```

### 5.5 第五阶段：清理和优化（1 天）

#### 步骤 1: 删除旧服务

确认所有引用已更新后，删除 `ArticleApplicationService`。

#### 步骤 2: 更新测试

重写测试用例，分别测试命令服务和查询服务。

#### 步骤 3: 更新文档

更新架构文档和 API 文档。

---

## 6. 代码示例

### 6.1 命令服务完整示例

```java
package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.application.article.command.*;
import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.valueobject.*;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * 文章命令服务
 * 负责处理所有改变文章状态的操作
 *
 * @author CleverOnion
 * @since 2.0.0
 */
@Service
@Transactional
public class ArticleCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleCommandService.class);

    private final ArticleRepository articleRepository;
    private final DomainEventPublisher eventPublisher;

    public ArticleCommandService(
            ArticleRepository articleRepository,
            DomainEventPublisher eventPublisher) {
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建文章草稿
     *
     * @param command 创建文章草稿命令
     * @return 创建的文章聚合
     */
    @CacheEvict(value = "article-lists", allEntries = true)
    public ArticleAggregate createDraft(CreateArticleDraftCommand command) {
        logger.debug("执行创建文章草稿命令: authorId={}, categoryId={}",
            command.getAuthorId().getValue(), command.getCategoryId().getValue());

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
        publishDomainEvents(article);

        logger.info("文章草稿创建成功: articleId={}, title={}",
            savedArticle.getId().getValue(),
            savedArticle.getContent().getTitle());

        return savedArticle;
    }

    /**
     * 创建并发布文章
     *
     * @param command 发布文章命令
     * @return 发布的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate createAndPublish(PublishArticleCommand command) {
        logger.debug("执行创建并发布文章命令: authorId={}", command.getAuthorId().getValue());

        // 创建草稿
        ArticleAggregate article = ArticleAggregate.createDraft(
            command.getContent(),
            command.getCategoryId(),
            command.getAuthorId()
        );

        // 添加标签
        if (command.getTagIds() != null) {
            command.getTagIds().forEach(article::addTag);
        }

        // 立即发布
        article.publish();

        // 保存
        ArticleAggregate savedArticle = articleRepository.save(article);

        // 发布领域事件
        publishDomainEvents(article);

        logger.info("文章创建并发布成功: articleId={}", savedArticle.getId().getValue());

        return savedArticle;
    }

    /**
     * 更新文章内容
     *
     * @param command 更新文章命令
     * @return 更新后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"},
                key = "#command.articleId.value",
                allEntries = true)
    public ArticleAggregate updateContent(UpdateArticleCommand command) {
        logger.debug("执行更新文章命令: articleId={}", command.getArticleId().getValue());

        // 查找文章
        ArticleAggregate article = findArticleOrThrow(command.getArticleId());

        // 验证权限
        checkAuthorPermission(article, command.getAuthorId());

        // 更新内容
        article.updateContent(command.getNewContent());

        // 更新分类（如果提供）
        if (command.getNewCategoryId() != null) {
            article.updateCategory(command.getNewCategoryId());
        }

        // 更新标签（如果提供）
        if (command.getNewTagIds() != null) {
            article.clearTags();
            command.getNewTagIds().forEach(article::addTag);
        }

        // 保存
        ArticleAggregate savedArticle = articleRepository.save(article);

        // 发布领域事件
        publishDomainEvents(article);

        logger.info("文章更新成功: articleId={}", savedArticle.getId().getValue());

        return savedArticle;
    }

    /**
     * 发布文章
     *
     * @param articleId 文章ID
     * @param authorId 作者ID
     * @return 发布后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate publish(ArticleId articleId, AuthorId authorId) {
        logger.debug("执行发布文章命令: articleId={}", articleId.getValue());

        // 查找文章
        ArticleAggregate article = findArticleOrThrow(articleId);

        // 验证权限
        checkAuthorPermission(article, authorId);

        // 发布
        article.publish();

        // 保存
        ArticleAggregate savedArticle = articleRepository.save(article);

        // 发布领域事件
        publishDomainEvents(article);

        logger.info("文章发布成功: articleId={}", savedArticle.getId().getValue());

        return savedArticle;
    }

    /**
     * 归档文章
     *
     * @param articleId 文章ID
     * @param authorId 作者ID
     * @return 归档后的文章聚合
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public ArticleAggregate archive(ArticleId articleId, AuthorId authorId) {
        logger.debug("执行归档文章命令: articleId={}", articleId.getValue());

        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);

        article.archive();

        ArticleAggregate savedArticle = articleRepository.save(article);
        publishDomainEvents(article);

        logger.info("文章归档成功: articleId={}", savedArticle.getId().getValue());

        return savedArticle;
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @param authorId 作者ID
     */
    @CacheEvict(value = {"articles", "article-lists"}, allEntries = true)
    public void delete(ArticleId articleId, AuthorId authorId) {
        logger.debug("执行删除文章命令: articleId={}", articleId.getValue());

        ArticleAggregate article = findArticleOrThrow(articleId);
        checkAuthorPermission(article, authorId);

        articleRepository.deleteById(articleId);

        logger.info("文章删除成功: articleId={}", articleId.getValue());
    }

    // ========== 私有辅助方法 ==========

    private ArticleAggregate findArticleOrThrow(ArticleId articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId.getValue()));
    }

    private void checkAuthorPermission(ArticleAggregate article, AuthorId authorId) {
        if (!article.belongsToAuthor(authorId)) {
            throw new IllegalStateException("只有文章作者才能执行此操作");
        }
    }

    private void publishDomainEvents(ArticleAggregate article) {
        if (article.getDomainEvents() != null && !article.getDomainEvents().isEmpty()) {
            eventPublisher.publish(article.getDomainEvents());
            article.clearDomainEvents();
        }
    }
}
```

### 6.2 查询服务完整示例

```java
package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.repository.ArticleRepository;
import com.cleveronion.blog.domain.article.valueobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 文章查询服务
 * 负责处理所有文章查询操作
 *
 * @author CleverOnion
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleQueryService.class);

    private final ArticleRepository articleRepository;

    public ArticleQueryService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /**
     * 根据ID查询文章
     *
     * @param articleId 文章ID
     * @return 文章聚合（如果存在）
     */
    @Cacheable(value = "articles", key = "#articleId.value")
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }

        logger.debug("查询文章: articleId={}", articleId.getValue());
        return articleRepository.findById(articleId);
    }

    /**
     * 查询已发布的文章列表（分页）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    @Cacheable(value = "article-lists",
               key = "'published:' + #page + ':' + #size")
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        validatePageParams(page, size);

        logger.debug("查询已发布文章列表: page={}, size={}", page, size);
        return articleRepository.findPublishedArticles(page, size);
    }

    /**
     * 根据作者ID查询文章（分页）
     *
     * @param authorId 作者ID
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    @Cacheable(value = "article-lists",
               key = "'author:' + #authorId.value + ':' + #page + ':' + #size")
    public List<ArticleAggregate> findByAuthor(AuthorId authorId, int page, int size) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        validatePageParams(page, size);

        logger.debug("查询作者文章: authorId={}, page={}, size={}",
            authorId.getValue(), page, size);
        return articleRepository.findByAuthorId(authorId, page, size);
    }

    /**
     * 根据分类ID查询已发布文章（分页）
     *
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    @Cacheable(value = "article-lists",
               key = "'category:' + #categoryId.value + ':' + #page + ':' + #size")
    public List<ArticleAggregate> findPublishedByCategory(
            CategoryId categoryId, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        validatePageParams(page, size);

        logger.debug("查询分类文章: categoryId={}, page={}, size={}",
            categoryId.getValue(), page, size);
        return articleRepository.findPublishedByCategoryId(categoryId, page, size);
    }

    /**
     * 根据标签ID查询已发布文章（分页）
     *
     * @param tagId 标签ID
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    @Cacheable(value = "article-lists",
               key = "'tag:' + #tagId.value + ':' + #page + ':' + #size")
    public List<ArticleAggregate> findPublishedByTag(TagId tagId, int page, int size) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        validatePageParams(page, size);

        logger.debug("查询标签文章: tagId={}, page={}, size={}",
            tagId.getValue(), page, size);
        return articleRepository.findPublishedByTagId(tagId, page, size);
    }

    /**
     * 搜索文章（按标题）
     *
     * @param keyword 搜索关键词
     * @return 文章列表
     */
    public List<ArticleAggregate> searchByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        logger.debug("搜索文章: keyword={}", keyword);
        return articleRepository.findByTitleContaining(keyword.trim());
    }

    /**
     * 统计已发布文章总数
     *
     * @return 文章数量
     */
    @Cacheable(value = "article-stats", key = "'published-count'")
    public long countPublishedArticles() {
        logger.debug("统计已发布文章数量");
        return articleRepository.countPublishedArticles();
    }

    /**
     * 统计作者的文章数量
     *
     * @param authorId 作者ID
     * @return 文章数量
     */
    @Cacheable(value = "article-stats", key = "'author-count:' + #authorId.value")
    public long countByAuthor(AuthorId authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }

        logger.debug("统计作者文章数量: authorId={}", authorId.getValue());
        return articleRepository.countByAuthorId(authorId);
    }

    /**
     * 统计分类的文章数量
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    @Cacheable(value = "article-stats", key = "'category-count:' + #categoryId.value")
    public long countByCategory(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        logger.debug("统计分类文章数量: categoryId={}", categoryId.getValue());
        return articleRepository.countByCategoryId(categoryId);
    }

    /**
     * 查询最近发布的文章
     *
     * @param limit 限制数量
     * @return 文章列表
     */
    @Cacheable(value = "article-lists", key = "'recent:' + #limit")
    public List<ArticleAggregate> findRecentlyPublished(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }

        logger.debug("查询最近发布文章: limit={}", limit);
        return articleRepository.findRecentlyPublished(limit);
    }

    /**
     * 查询热门文章
     *
     * @param limit 限制数量
     * @return 文章列表
     */
    @Cacheable(value = "article-lists", key = "'popular:' + #limit")
    public List<ArticleAggregate> findPopularArticles(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }

        logger.debug("查询热门文章: limit={}", limit);
        return articleRepository.findPopularArticles(limit);
    }

    // ========== 私有辅助方法 ==========

    private void validatePageParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
    }
}
```

### 6.3 命令对象示例

```java
package com.cleveronion.blog.application.article.command;

import com.cleveronion.blog.domain.article.valueobject.*;

import java.util.Objects;
import java.util.Set;

/**
 * 创建文章草稿命令
 *
 * @author CleverOnion
 * @since 2.0.0
 */
public class CreateArticleDraftCommand {

    private final ArticleContent content;
    private final CategoryId categoryId;
    private final AuthorId authorId;
    private final Set<TagId> tagIds;

    public CreateArticleDraftCommand(
            ArticleContent content,
            CategoryId categoryId,
            AuthorId authorId,
            Set<TagId> tagIds) {
        this.content = Objects.requireNonNull(content, "文章内容不能为空");
        this.categoryId = Objects.requireNonNull(categoryId, "分类ID不能为空");
        this.authorId = Objects.requireNonNull(authorId, "作者ID不能为空");
        this.tagIds = tagIds;
    }

    public ArticleContent getContent() {
        return content;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }

    public AuthorId getAuthorId() {
        return authorId;
    }

    public Set<TagId> getTagIds() {
        return tagIds;
    }

    @Override
    public String toString() {
        return "CreateArticleDraftCommand{" +
                "title='" + content.getTitle() + '\'' +
                ", categoryId=" + categoryId.getValue() +
                ", authorId=" + authorId.getValue() +
                ", tagCount=" + (tagIds != null ? tagIds.size() : 0) +
                '}';
    }
}
```

### 6.4 控制器更新示例

```java
package com.cleveronion.blog.presentation.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cleveronion.blog.application.article.command.*;
import com.cleveronion.blog.application.article.service.ArticleCommandService;
import com.cleveronion.blog.application.article.service.ArticleQueryService;
import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.valueobject.*;
import com.cleveronion.blog.presentation.api.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 文章控制器（CQRS版本）
 *
 * @author CleverOnion
 * @since 2.0.0
 */
@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleCommandService commandService;
    private final ArticleQueryService queryService;

    public ArticleController(
            ArticleCommandService commandService,
            ArticleQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * 创建文章草稿
     */
    @PostMapping
    public Result<ArticleResponse> createArticle(@RequestBody CreateArticleRequest request) {
        // 构建命令对象
        CreateArticleDraftCommand command = new CreateArticleDraftCommand(
            new ArticleContent(request.getTitle(), request.getContent(), request.getSummary()),
            new CategoryId(request.getCategoryId()),
            new AuthorId(StpUtil.getLoginIdAsLong()),
            convertTagNames(request.getTagNames())
        );

        // 执行命令
        ArticleAggregate article = commandService.createDraft(command);

        // 返回响应
        return Result.success(buildResponse(article));
    }

    /**
     * 发布文章
     */
    @PostMapping("/{id}/publish")
    public Result<ArticleResponse> publishArticle(@PathVariable Long id) {
        ArticleId articleId = new ArticleId(id.toString());
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());

        // 执行命令
        ArticleAggregate article = commandService.publish(articleId, authorId);

        return Result.success(buildResponse(article));
    }

    /**
     * 查询文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleResponse> getArticleById(@PathVariable Long id) {
        ArticleId articleId = new ArticleId(id.toString());

        // 执行查询
        Optional<ArticleAggregate> article = queryService.findById(articleId);

        return article.map(a -> Result.success(buildResponse(a)))
                     .orElse(Result.notFound("文章不存在"));
    }

    /**
     * 查询文章列表
     */
    @GetMapping
    public Result<ArticleListResponse> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 执行查询
        List<ArticleAggregate> articles = queryService.findPublishedArticles(page, size);
        long total = queryService.countPublishedArticles();

        return Result.success(new ArticleListResponse(articles, total, page, size));
    }

    // ... 其他方法
}
```

---

## 7. 迁移策略

### 7.1 渐进式迁移

**原则**: 不影响现有功能，逐步迁移

```
阶段1: 并存期（1-2周）
────────────────────────────
ArticleApplicationService  (保留)
ArticleCommandService      (新增)
ArticleQueryService        (新增)

阶段2: 过渡期（1周）
────────────────────────────
ArticleApplicationService  (标记@Deprecated)
ArticleCommandService      (使用中)
ArticleQueryService        (使用中)

阶段3: 完成期
────────────────────────────
ArticleApplicationService  (删除)
ArticleCommandService      (使用中)
ArticleQueryService        (使用中)
```

### 7.2 向后兼容

如果需要保持向后兼容，可以让旧服务委托给新服务：

```java
@Service
@Deprecated
@Transactional
public class ArticleApplicationService {

    private final ArticleCommandService commandService;
    private final ArticleQueryService queryService;

    /**
     * @deprecated 请使用 ArticleCommandService.createDraft()
     */
    @Deprecated
    public ArticleAggregate createDraft(...) {
        return commandService.createDraft(...);
    }

    /**
     * @deprecated 请使用 ArticleQueryService.findById()
     */
    @Deprecated
    public Optional<ArticleAggregate> findById(...) {
        return queryService.findById(...);
    }
}
```

### 7.3 迁移检查清单

- [ ] 创建命令对象
- [ ] 创建命令服务
- [ ] 创建查询服务
- [ ] 迁移命令方法
- [ ] 迁移查询方法
- [ ] 更新控制器引用
- [ ] 更新测试用例
- [ ] 配置缓存
- [ ] 性能测试
- [ ] 代码审查
- [ ] 文档更新
- [ ] 删除旧服务

---

## 8. 测试策略

### 8.1 命令服务测试

```java
@ExtendWith(MockitoExtension.class)
class ArticleCommandServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private ArticleCommandService commandService;

    @Test
    @DisplayName("应该成功创建文章草稿")
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
        verify(articleRepository).save(any(ArticleAggregate.class));
        verify(eventPublisher).publish(anyList());
    }

    @Test
    @DisplayName("更新文章时应该验证作者权限")
    void should_check_author_permission_when_update() {
        // Given
        ArticleId articleId = new ArticleId("1");
        AuthorId authorId = new AuthorId(1L);
        AuthorId anotherAuthorId = new AuthorId(2L);

        ArticleAggregate article = mock(ArticleAggregate.class);
        when(article.belongsToAuthor(authorId)).thenReturn(true);
        when(article.belongsToAuthor(anotherAuthorId)).thenReturn(false);
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        UpdateArticleCommand command = new UpdateArticleCommand(
            articleId,
            new ArticleContent("新标题", "新内容", "新摘要"),
            null,
            null,
            anotherAuthorId
        );

        // When & Then
        assertThatThrownBy(() -> commandService.updateContent(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("只有文章作者才能执行此操作");
    }
}
```

### 8.2 查询服务测试

```java
@ExtendWith(MockitoExtension.class)
class ArticleQueryServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleQueryService queryService;

    @Test
    @DisplayName("应该成功查询文章")
    void should_find_article_by_id() {
        // Given
        ArticleId articleId = new ArticleId("1");
        ArticleAggregate article = mock(ArticleAggregate.class);
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        // When
        Optional<ArticleAggregate> result = queryService.findById(articleId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(article);
        verify(articleRepository).findById(articleId);
    }

    @Test
    @DisplayName("分页参数无效时应该抛出异常")
    void should_throw_exception_when_page_params_invalid() {
        // When & Then
        assertThatThrownBy(() -> queryService.findPublishedArticles(-1, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("页码不能小于0");

        assertThatThrownBy(() -> queryService.findPublishedArticles(0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("每页大小必须在1-100之间");
    }

    @Test
    @DisplayName("应该缓存查询结果")
    void should_cache_query_results() {
        // 这需要集成测试环境，配置实际的缓存
        // 验证第二次查询不会调用 repository
    }
}
```

### 8.3 集成测试

```java
@SpringBootTest
@Transactional
class ArticleCQRSIntegrationTest {

    @Autowired
    private ArticleCommandService commandService;

    @Autowired
    private ArticleQueryService queryService;

    @Test
    @DisplayName("创建文章后应该能够查询到")
    void should_query_article_after_create() {
        // Given
        CreateArticleDraftCommand command = new CreateArticleDraftCommand(
            new ArticleContent("测试标题", "测试内容", "测试摘要"),
            new CategoryId(1L),
            new AuthorId(1L),
            null
        );

        // When - 创建文章
        ArticleAggregate createdArticle = commandService.createDraft(command);

        // Then - 应该能查询到
        Optional<ArticleAggregate> foundArticle = queryService.findById(createdArticle.getId());
        assertThat(foundArticle).isPresent();
        assertThat(foundArticle.get().getContent().getTitle()).isEqualTo("测试标题");
    }
}
```

---

## 9. 性能优化

### 9.1 查询优化

#### 9.1.1 引入缓存

```java
@Cacheable(value = "articles", key = "#articleId.value", unless = "#result == null")
public Optional<ArticleAggregate> findById(ArticleId articleId) {
    return articleRepository.findById(articleId);
}
```

#### 9.1.2 使用数据库读副本

```yaml
spring:
  datasource:
    write:
      url: jdbc:postgresql://write-db:5432/blog
    read:
      url: jdbc:postgresql://read-db:5432/blog
```

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    @Autowired
    @Qualifier("readDataSource")
    private DataSource readDataSource;

    // 查询使用读副本
}
```

#### 9.1.3 查询结果投影

```java
// 列表查询不需要完整内容
@Query("SELECT new ArticleListProjection(a.id, a.title, a.summary, a.createdAt) " +
       "FROM ArticlePO a WHERE a.status = :status")
List<ArticleListProjection> findPublishedArticleList(@Param("status") String status);
```

### 9.2 命令优化

#### 9.2.1 异步事件处理

```java
@Component
public class ArticleEventHandler {

    @EventListener
    @Async  // 异步处理
    public void handleArticlePublished(ArticlePublishedEvent event) {
        // 发送通知
        // 更新搜索索引
        // 更新缓存
    }
}
```

#### 9.2.2 批量操作优化

```java
@Service
public class ArticleCommandService {

    @Transactional
    public List<ArticleAggregate> batchPublish(List<ArticleId> articleIds, AuthorId authorId) {
        // 批量查询
        List<ArticleAggregate> articles = articleRepository.findByIds(articleIds);

        // 批量操作
        articles.forEach(article -> {
            checkAuthorPermission(article, authorId);
            article.publish();
        });

        // 批量保存
        return articleRepository.saveAll(articles);
    }
}
```

### 9.3 性能监控

```java
@Aspect
@Component
public class PerformanceMonitoringAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.cleveronion.blog.application.article.service.*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(Timer.builder("article.service.method")
                .tag("class", joinPoint.getTarget().getClass().getSimpleName())
                .tag("method", joinPoint.getSignature().getName())
                .register(meterRegistry));
        }
    }
}
```

---

## 10. 最佳实践

### 10.1 命令设计原则

✅ **DO**:

- 使用不可变的命令对象
- 命令对象包含所有必要参数
- 命令对象在构造时验证基本约束
- 使用领域语言命名命令

❌ **DON'T**:

- 命令对象包含业务逻辑
- 命令对象可变
- 使用 setter 方法
- 使用技术术语命名

### 10.2 查询设计原则

✅ **DO**:

- 查询方法使用只读事务
- 为高频查询添加缓存
- 使用投影减少数据传输
- 合理设置缓存过期时间

❌ **DON'T**:

- 在查询方法中修改状态
- 过度缓存
- 缓存时间过长
- 忽略缓存失效

### 10.3 事务管理原则

✅ **DO**:

- 命令服务使用写事务
- 查询服务使用只读事务
- 保持事务简短
- 在应用服务层管理事务

❌ **DON'T**:

- 在领域层开启事务
- 长事务
- 嵌套事务（除非必要）
- 在查询中使用写事务

### 10.4 缓存策略原则

✅ **DO**:

- 缓存读多写少的数据
- 设置合理的过期时间
- 在写操作后清除相关缓存
- 使用缓存键的命名约定

❌ **DON'T**:

- 缓存所有查询
- 永久缓存
- 忘记清除缓存
- 缓存键冲突

---

## 11. 常见问题

### Q1: CQRS 会增加代码量吗？

**A**: 是的，会有一定增加（约 20-30%），但带来的好处：

- 代码更清晰，职责更明确
- 更容易理解和维护
- 长期来看降低维护成本

### Q2: 需要使用独立的读写数据库吗？

**A**: 不是必须的。CQRS 的核心是职责分离，不是物理分离。可以：

- 阶段 1: 同一数据库，分离服务
- 阶段 2: 引入缓存
- 阶段 3: 考虑读写分离（如果需要）

### Q3: 如何保证读写一致性？

**A**:

- **强一致性**: 命令执行后立即清除缓存
- **最终一致性**: 使用事件驱动异步更新
- 根据业务需求选择合适的一致性策略

### Q4: 查询服务可以调用命令服务吗？

**A**: **不可以**。这违反了 CQRS 原则。如果需要：

- 在控制器层协调
- 使用领域事件
- 重新审视设计

### Q5: 如何处理复杂的统计查询？

**A**:

- 使用专门的统计服务
- 引入 OLAP 数据库
- 使用物化视图
- 异步更新统计数据

### Q6: 测试会变得更复杂吗？

**A**: 不会，反而更简单：

- 命令测试专注于状态变更
- 查询测试专注于数据读取
- 测试用例更清晰

### Q7: 如何逐步迁移现有代码？

**A**: 参考第 7 节的迁移策略：

1. 并存期：新旧服务同时存在
2. 过渡期：标记旧服务为 @Deprecated
3. 完成期：删除旧服务

### Q8: 性能提升有多大？

**A**: 取决于具体场景：

- 引入缓存后，查询性能可提升 10-100 倍
- 使用读副本后，可提升 2-5 倍
- 需要实际测试验证

### Q9: 查询操作需要像命令那样使用专门的 Query 对象吗？

**A**: **大多数情况不需要**。这是 CQRS 中一个常见的误解。

**核心区别**：

- ✅ **命令（Command）** - **必须**使用 Command 对象封装
  - 代表业务意图，需要明确
  - 可能需要审计、重放
  - 不可变性非常重要
- ⚠️ **查询（Query）** - **通常不需要** Query 对象
  - 简单查询（80%）：直接传参即可
  - 复杂查询（20%）：参数>5 个时可选使用

**推荐做法**：

```java
// ✅ 命令 - 总是使用对象
commandService.createDraft(CreateArticleDraftCommand);
commandService.update(UpdateArticleCommand);

// ✅ 简单查询 - 直接传参
queryService.findById(ArticleId id);
queryService.findPublishedArticles(int page, int size);

// ⚠️ 复杂查询 - 参数太多时才用对象
queryService.advancedSearch(ArticleSearchQuery query);
```

详细说明请参考：[CQRS 查询对象使用指南](./cqrs-query-object-guide.md)

---

## 12. 参考资源

### 12.1 延伸阅读

- [CQRS Pattern - Martin Fowler](https://martinfowler.com/bliki/CQRS.html)
- [Domain-Driven Design Reference](https://www.domainlanguage.com/ddd/reference/)
- [Implementing Domain-Driven Design - Vaughn Vernon](https://vaughnvernon.com/)

### 12.2 相关文档

- [后端开发规范总结](../standards/backend-development-standards.md)
- [项目问题分析与改进建议](../standards/issues-and-improvements.md)
- [系统架构文档](../architecture.md)

---

## 13. 变更历史

| 版本 | 日期       | 作者        | 变更说明                       |
| ---- | ---------- | ----------- | ------------------------------ |
| 1.0  | 2025-10-01 | CleverOnion | 初始版本，完整的 CQRS 引入方案 |

---

**文档维护**: 本文档应随着 CQRS 实施进度更新，记录实际遇到的问题和解决方案。

**反馈**: 如有问题或改进建议，请提交 Issue 或 PR。
