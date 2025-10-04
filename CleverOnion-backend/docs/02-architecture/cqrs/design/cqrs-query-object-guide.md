# CQRS 查询对象使用指南

## 问题：查询需要单独的对象吗？

### 简短回答

**大多数情况下不需要**。与命令（Command）不同，查询操作通常直接传递参数即可。

### 详细说明

```
命令侧 (必须使用Command对象):
────────────────────────────────
CreateArticleDraftCommand ✅ 必须
UpdateArticleCommand      ✅ 必须
PublishArticleCommand     ✅ 必须

查询侧 (视情况而定):
────────────────────────────────
findById(ArticleId id)    ✅ 直接传参（推荐）
search(ArticleSearchQuery) ⚠️  复杂查询可选
```

---

## 何时使用 Query 对象

### ✅ 适合使用 Query 对象的场景

#### 1. 查询参数很多（>3 个）

```java
// ❌ 不好：参数太多，难以维护
public List<ArticleAggregate> searchArticles(
    String keyword,
    CategoryId categoryId,
    TagId tagId,
    ArticleStatus status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String authorName,
    int page,
    int size,
    String sortBy,
    String sortDirection) {
    // ...
}

// ✅ 好：使用Query对象封装
public List<ArticleAggregate> search(ArticleSearchQuery query) {
    // ...
}
```

**Query 对象**：

```java
public class ArticleSearchQuery {
    private final String keyword;
    private final CategoryId categoryId;
    private final TagId tagId;
    private final ArticleStatus status;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String authorName;
    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    // 构造函数、getters

    public boolean hasKeyword() {
        return keyword != null && !keyword.isEmpty();
    }

    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }
}
```

#### 2. 查询条件可选且复杂

```java
public class ArticleFilterQuery {
    private final CategoryId categoryId;        // 可选
    private final TagId tagId;                  // 可选
    private final ArticleStatus status;         // 可选
    private final AuthorId authorId;            // 可选
    private final Pagination pagination;        // 必须

    // 提供便捷的判断方法
    public boolean hasFilters() {
        return categoryId != null || tagId != null ||
               status != null || authorId != null;
    }
}
```

#### 3. 需要复用查询逻辑

```java
// 同一个Query对象可以在多处使用
ArticleSearchQuery query = new ArticleSearchQuery(...);

// 在服务层使用
List<Article> articles = queryService.search(query);

// 在审计日志中记录
auditLog.logSearch(query);

// 在缓存键中使用
String cacheKey = query.toCacheKey();
```

### ❌ 不需要 Query 对象的场景

#### 1. 简单查询（1-2 个参数）

```java
// ✅ 推荐：直接传参
public Optional<ArticleAggregate> findById(ArticleId id) {
    return repository.findById(id);
}

// ✅ 推荐：简单分页查询
public List<ArticleAggregate> findPublishedArticles(int page, int size) {
    return repository.findPublishedArticles(page, size);
}

// ❌ 过度设计：没有必要
public Optional<ArticleAggregate> findById(ArticleByIdQuery query) {
    return repository.findById(query.getId());
}
```

#### 2. 固定的查询条件

```java
// ✅ 推荐：方法名已经表达了查询意图
public List<ArticleAggregate> findPublishedArticles(int page, int size) { }

public List<ArticleAggregate> findByAuthor(AuthorId authorId, int page, int size) { }

public long countPublishedArticles() { }
```

#### 3. RESTful 风格的查询

```java
// ✅ 推荐：直接从请求参数映射
@GetMapping("/articles")
public Result<ArticleListResponse> getArticles(
    @RequestParam(required = false) Long categoryId,
    @RequestParam(required = false) Long tagId,
    @RequestParam(required = false) String status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {

    // 可以直接传递参数给查询服务
    return queryService.findArticles(categoryId, tagId, status, page, size);
}
```

---

## 决策树

```
查询操作需要Query对象吗？
    │
    ├─ 查询参数 ≤ 3个？
    │   └─ 是 → ❌ 不需要，直接传参
    │
    ├─ 查询条件复杂且可选？
    │   └─ 是 → ✅ 考虑使用Query对象
    │
    ├─ 需要复用查询逻辑？
    │   └─ 是 → ✅ 使用Query对象
    │
    └─ 需要记录/审计查询？
        └─ 是 → ✅ 使用Query对象
```

---

## 对比：Command vs Query 对象

### Command 对象（必须）

| 特性         | 说明         | 原因             |
| ------------ | ------------ | ---------------- |
| **使用率**   | 100%         | 所有命令都应封装 |
| **不可变性** | 强制         | 防止并发问题     |
| **验证**     | 构造时验证   | 确保命令有效     |
| **用途**     | 封装操作意图 | 明确业务意图     |
| **审计**     | 可记录和重放 | 支持事件溯源     |

```java
// ✅ 命令对象 - 总是使用
public class CreateArticleDraftCommand {
    private final ArticleContent content;
    private final CategoryId categoryId;
    private final AuthorId authorId;

    public CreateArticleDraftCommand(...) {
        this.content = requireNonNull(content);
        this.categoryId = requireNonNull(categoryId);
        this.authorId = requireNonNull(authorId);
    }
}
```

### Query 对象（可选）

| 特性         | 说明         | 原因               |
| ------------ | ------------ | ------------------ |
| **使用率**   | 10-30%       | 仅复杂查询使用     |
| **不可变性** | 推荐         | 保持一致性         |
| **验证**     | 可选         | 查询失败不改变状态 |
| **用途**     | 封装查询条件 | 简化方法签名       |
| **审计**     | 可选         | 记录搜索行为       |

```java
// ✅ 简单查询 - 直接传参
public Optional<ArticleAggregate> findById(ArticleId id) { }

// ✅ 复杂查询 - 使用Query对象
public List<ArticleAggregate> search(ArticleSearchQuery query) { }
```

---

## 实际示例

### 示例 1：简单查询（不使用 Query 对象）

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    // ✅ 单参数查询 - 直接传参
    @Cacheable(value = "articles", key = "#id.value")
    public Optional<ArticleAggregate> findById(ArticleId id) {
        return repository.findById(id);
    }

    // ✅ 双参数查询 - 直接传参
    @Cacheable(value = "article-lists", key = "'published:' + #page + ':' + #size")
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        return repository.findPublishedArticles(page, size);
    }

    // ✅ 三参数查询 - 还算简单，直接传参
    public List<ArticleAggregate> findByAuthorAndStatus(
        AuthorId authorId,
        ArticleStatus status,
        Pagination pagination) {
        return repository.findByAuthorAndStatus(authorId, status, pagination);
    }
}
```

### 示例 2：复杂查询（使用 Query 对象）

```java
// 定义Query对象
public class ArticleSearchQuery {
    private final String keyword;          // 可选
    private final CategoryId categoryId;   // 可选
    private final Set<TagId> tagIds;       // 可选
    private final ArticleStatus status;    // 可选
    private final LocalDateTime startDate; // 可选
    private final LocalDateTime endDate;   // 可选
    private final Pagination pagination;   // 必须
    private final SortCriteria sort;       // 可选

    public ArticleSearchQuery(Builder builder) {
        this.keyword = builder.keyword;
        this.categoryId = builder.categoryId;
        this.tagIds = builder.tagIds;
        this.status = builder.status;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.pagination = requireNonNull(builder.pagination);
        this.sort = builder.sort;
    }

    // 便捷方法
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    public boolean hasTagFilter() {
        return tagIds != null && !tagIds.isEmpty();
    }

    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }

    // Builder 模式
    public static class Builder {
        private String keyword;
        private CategoryId categoryId;
        private Set<TagId> tagIds;
        private ArticleStatus status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Pagination pagination;
        private SortCriteria sort;

        public Builder pagination(Pagination pagination) {
            this.pagination = pagination;
            return this;
        }

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder category(CategoryId categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder tags(Set<TagId> tagIds) {
            this.tagIds = tagIds;
            return this;
        }

        public Builder status(ArticleStatus status) {
            this.status = status;
            return this;
        }

        public Builder dateRange(LocalDateTime start, LocalDateTime end) {
            this.startDate = start;
            this.endDate = end;
            return this;
        }

        public Builder sort(SortCriteria sort) {
            this.sort = sort;
            return this;
        }

        public ArticleSearchQuery build() {
            return new ArticleSearchQuery(this);
        }
    }
}
```

**使用 Query 对象：**

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    /**
     * 高级搜索 - 使用Query对象
     */
    public List<ArticleAggregate> search(ArticleSearchQuery query) {
        logger.debug("执行文章搜索: keyword={}, hasFilters={}",
            query.hasKeyword(), query.hasFilters());

        // 构建动态查询
        Specification<ArticlePO> spec = buildSpecification(query);

        return repository.findAll(spec, query.getPagination());
    }

    private Specification<ArticlePO> buildSpecification(ArticleSearchQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.hasKeyword()) {
                predicates.add(cb.like(root.get("title"), "%" + query.getKeyword() + "%"));
            }

            if (query.hasCategoryFilter()) {
                predicates.add(cb.equal(root.get("categoryId"), query.getCategoryId().getValue()));
            }

            if (query.hasDateRange()) {
                predicates.add(cb.between(root.get("createdAt"),
                    query.getStartDate(), query.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

**控制器中使用：**

```java
@RestController
public class ArticleController {

    @GetMapping("/articles/search")
    public Result<ArticleListResponse> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Set<Long> tagIds,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDirection) {

        // 构建Query对象
        ArticleSearchQuery query = new ArticleSearchQuery.Builder()
            .keyword(keyword)
            .category(categoryId != null ? new CategoryId(categoryId) : null)
            .tags(convertToTagIds(tagIds))
            .status(status != null ? ArticleStatus.valueOf(status) : null)
            .dateRange(startDate, endDate)
            .pagination(new Pagination(page, size))
            .sort(new SortCriteria(sortBy, sortDirection))
            .build();

        // 执行查询
        List<ArticleAggregate> articles = queryService.search(query);

        return Result.success(new ArticleListResponse(articles));
    }
}
```

---

## 最佳实践指南

### 规则 1: 简单查询直接传参

```java
// ✅ 推荐
public Optional<ArticleAggregate> findById(ArticleId id);
public List<ArticleAggregate> findByAuthor(AuthorId authorId);
public List<ArticleAggregate> findPublishedArticles(int page, int size);

// ❌ 过度设计
public Optional<ArticleAggregate> findById(FindArticleByIdQuery query);
```

### 规则 2: 复杂查询使用 Query 对象

```java
// ✅ 参数>3个，使用Query对象
public List<ArticleAggregate> search(ArticleSearchQuery query);

// ✅ 动态条件，使用Query对象
public List<ArticleAggregate> findByFilters(ArticleFilterQuery query);
```

### 规则 3: 分页和排序可以使用值对象

```java
// 创建通用的分页值对象
public class Pagination {
    private final int page;
    private final int size;

    public Pagination(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("页码不能小于0");
        if (size <= 0 || size > 100) throw new IllegalArgumentException("每页大小必须在1-100之间");
        this.page = page;
        this.size = size;
    }

    // getters
}

// 使用
public List<ArticleAggregate> findPublishedArticles(Pagination pagination) {
    return repository.findPublishedArticles(
        pagination.getPage(),
        pagination.getSize()
    );
}
```

### 规则 4: Query 对象应该不可变

```java
public class ArticleSearchQuery {
    private final String keyword;
    private final CategoryId categoryId;
    // ... 其他字段都是 final

    // ✅ 只提供 getters
    public String getKeyword() { return keyword; }

    // ❌ 不要提供 setters
    // public void setKeyword(String keyword) { ... }
}
```

---

## 推荐的项目结构

### 方案 A：不使用 Query 对象（推荐用于当前项目）

```
application/article/
├── command/                          # 命令对象（必须）
│   ├── CreateArticleDraftCommand.java
│   ├── UpdateArticleCommand.java
│   └── PublishArticleCommand.java
│
└── service/
    ├── ArticleCommandService.java    # 命令服务
    └── ArticleQueryService.java      # 查询服务（直接传参）
```

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    // 简单查询 - 直接传参
    public Optional<ArticleAggregate> findById(ArticleId id) { }
    public List<ArticleAggregate> findPublishedArticles(int page, int size) { }
    public List<ArticleAggregate> findByCategory(CategoryId categoryId, int page, int size) { }

    // 如果确实需要复杂查询，单独方法
    public List<ArticleAggregate> searchWithMultipleFilters(
        String keyword, CategoryId categoryId, TagId tagId,
        ArticleStatus status, int page, int size) {
        // 实现
    }
}
```

### 方案 B：混合使用（适合复杂业务）

```
application/article/
├── command/                          # 命令对象
│   ├── CreateArticleDraftCommand.java
│   └── ...
│
├── query/                            # 查询对象（仅复杂查询）
│   ├── ArticleSearchQuery.java
│   └── ArticleFilterQuery.java
│
└── service/
    ├── ArticleCommandService.java    # 命令服务
    └── ArticleQueryService.java      # 查询服务
```

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    // 简单查询 - 直接传参
    public Optional<ArticleAggregate> findById(ArticleId id) { }
    public List<ArticleAggregate> findPublishedArticles(int page, int size) { }

    // 复杂查询 - 使用Query对象
    public List<ArticleAggregate> search(ArticleSearchQuery query) { }
    public List<ArticleAggregate> findByFilters(ArticleFilterQuery query) { }
}
```

---

## CleverOnion 项目建议

### 当前情况分析

查看当前的查询方法：

```java
// 简单查询（约80%） - 不需要Query对象
findById(ArticleId)                                    // 1个参数 ✅
findByAuthorId(AuthorId, int page, int size)          // 3个参数 ✅
findPublishedArticles(int page, int size)             // 2个参数 ✅
countByAuthorId(AuthorId)                             // 1个参数 ✅

// 中等复杂（约15%） - 边界情况
findByCategoryAndTag(CategoryId, TagId, int page, int size)  // 4个参数 ⚠️

// 复杂查询（约5%） - 可考虑Query对象
findByCategoryAndTagAndStatus(CategoryId, TagId, ArticleStatus, int page, int size)  // 5个参数 ⚠️
```

### 推荐方案

**阶段 1（当前）：不引入 Query 对象**

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {

    // 所有查询都直接传参
    public Optional<ArticleAggregate> findById(ArticleId id) { }

    public List<ArticleAggregate> findPublishedArticles(int page, int size) { }

    public List<ArticleAggregate> findByCategory(
        CategoryId categoryId, int page, int size) { }

    // 即使是复杂查询，也可以先直接传参
    public List<ArticleAggregate> findByCategoryAndTagAndStatus(
        CategoryId categoryId,
        TagId tagId,
        ArticleStatus status,
        int page,
        int size) {
        return repository.findByCategoryAndTagAndStatus(
            categoryId, tagId, status, page, size);
    }
}
```

**优点**：

- ✅ 简单直接
- ✅ 减少类的数量
- ✅ 易于理解
- ✅ 符合当前项目规模

**阶段 2（未来，如需要）：引入 Query 对象**

当出现以下情况时再考虑：

- 查询参数 > 5 个
- 需要复杂的动态查询构建
- 需要记录查询审计日志
- 查询逻辑需要在多处复用

```java
// 仅为复杂搜索引入Query对象
public class ArticleAdvancedSearchQuery {
    private final String keyword;
    private final CategoryId categoryId;
    private final Set<TagId> tagIds;
    private final ArticleStatus status;
    private final AuthorId authorId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Pagination pagination;
    private final SortCriteria sort;

    // Builder模式构造
}

@Service
public class ArticleQueryService {

    // 简单查询仍然直接传参
    public Optional<ArticleAggregate> findById(ArticleId id) { }

    // 只有高级搜索使用Query对象
    public List<ArticleAggregate> advancedSearch(ArticleAdvancedSearchQuery query) {
        // 复杂的动态查询逻辑
    }
}
```

---

## 对比总结

### Command（命令）- 总是使用对象

```java
// ✅ 标准做法
commandService.createDraft(CreateArticleDraftCommand command);
commandService.publish(PublishArticleCommand command);
commandService.update(UpdateArticleCommand command);
```

**原因**：

1. 命令代表业务意图，需要明确封装
2. 命令可能需要审计、重放
3. 命令的不可变性很重要
4. 命令对象是 CQRS 的核心

### Query（查询）- 视情况而定

```java
// ✅ 简单查询 - 直接传参（80%的场景）
queryService.findById(ArticleId id);
queryService.findPublishedArticles(int page, int size);

// ✅ 复杂查询 - 使用Query对象（20%的场景）
queryService.search(ArticleSearchQuery query);
```

**原因**：

1. 查询不改变状态，失败成本低
2. 简单查询使用对象是过度设计
3. 只有复杂查询才需要封装
4. 保持代码简洁

---

## 快速决策表

| 场景       | 参数数量 | 推荐方案   | 示例                                     |
| ---------- | -------- | ---------- | ---------------------------------------- |
| 单 ID 查询 | 1 个     | 直接传参   | `findById(id)`                           |
| 简单列表   | 2-3 个   | 直接传参   | `findPublished(page, size)`              |
| 带过滤列表 | 3-4 个   | 直接传参   | `findByCategory(categoryId, page, size)` |
| 多条件查询 | 5+个     | Query 对象 | `search(ArticleSearchQuery)`             |
| 动态查询   | 不定     | Query 对象 | `findByFilters(ArticleFilterQuery)`      |
| 全文搜索   | 复杂     | Query 对象 | `fullTextSearch(SearchQuery)`            |

---

## 更新后的 CQRS 架构图

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│                   (ArticleController)                    │
└────────────────┬──────────────────┬─────────────────────┘
                 │                  │
    ┌────────────▼────────┐   ┌─────▼──────────────────┐
    │ Command Service     │   │   Query Service        │
    │                     │   │                        │
    │ 使用Command对象 ✅  │   │   直接传参 ✅          │
    │ - CreateDraftCmd    │   │   - findById(id)       │
    │ - UpdateCmd         │   │   - findBy(params)     │
    │ - PublishCmd        │   │                        │
    │                     │   │   复杂查询可选Query ⚠️  │
    │                     │   │   - search(query)      │
    └─────────────────────┘   └────────────────────────┘
```

---

## 实施建议

### 对于 CleverOnion 项目

**推荐做法**：

1. **命令侧**：全部使用 Command 对象 ✅

   ```java
   CreateArticleDraftCommand
   UpdateArticleCommand
   PublishArticleCommand
   DeleteArticleCommand
   ```

2. **查询侧**：暂不引入 Query 对象 ✅

   ```java
   // 保持简单，直接传参
   findById(ArticleId id)
   findPublishedArticles(int page, int size)
   findByCategory(CategoryId categoryId, int page, int size)
   ```

3. **未来扩展**：如需要高级搜索，再引入 ⚠️
   ```java
   // 仅在需要时引入
   search(ArticleAdvancedSearchQuery query)
   ```

---

## 修订说明

本文档补充了 [CQRS 实施指南](./cqrs-implementation-guide.md) 中关于查询对象的说明。

### 核心要点

1. **Command 对象是必须的** - 所有命令操作都应封装
2. **Query 对象是可选的** - 仅复杂查询需要
3. **保持简单原则** - 不要过度设计
4. **渐进式引入** - 先简单实现，按需优化

---

**相关文档**：

- [CQRS 架构引入方案](./cqrs-implementation-guide.md) - 完整实施指南
- [CQRS 快速参考手册](./cqrs-quick-reference.md) - 核心概念速查

**问题反馈**：如有疑问，欢迎提出 Issue 讨论。
