# 其他模块 CQRS 架构实施指南

## 📅 创建日期：2025-10-01

---

## 📋 目录

- [1. 概述](#1-概述)
- [2. 模块分析](#2-模块分析)
- [3. Category 模块 CQRS 设计](#3-category-模块-cqrs-设计)
- [4. Tag 模块 CQRS 设计](#4-tag-模块-cqrs-设计)
- [5. User 模块 CQRS 设计](#5-user-模块-cqrs-设计)
- [6. Comment 模块 CQRS 设计](#6-comment-模块-cqrs-设计)
- [7. 实施优先级](#7-实施优先级)
- [8. 缓存策略](#8-缓存策略)
- [9. 事件设计](#9-事件设计)
- [10. 代码统计](#10-代码统计)
- [11. 实施步骤](#11-实施步骤)

---

## 1. 概述

### 1.1 目标

基于 Article 模块 CQRS 架构改造的成功经验，将 CQRS 模式应用到其他四个核心模块：

- **Category**（分类）- 博客分类管理
- **Tag**（标签）- 文章标签系统
- **User**（用户）- 用户信息管理
- **Comment**（评论）- 评论系统（已部分使用命令对象）

### 1.2 Article 模块经验总结

**成功要素**：

- ✅ 清晰的职责分离（命令 vs 查询）
- ✅ 不可变命令对象设计
- ✅ 领域事件机制
- ✅ 完善的缓存策略
- ✅ 90%+ 测试覆盖率
- ✅ 渐进式平滑迁移

**性能提升**：

| 操作类型 | 改进前 | 改进后 | 提升倍数 |
| -------- | ------ | ------ | -------- |
| 详情查询 | 20ms   | 1-2ms  | 10-20x   |
| 列表查询 | 100ms  | 5-10ms | 10-20x   |
| 统计查询 | 50ms   | 1ms    | 50x      |

### 1.3 设计原则

1. **参考 Article 模式** - 保持架构一致性
2. **职责分离** - 命令（写）与查询（读）完全隔离
3. **性能优先** - 查询方法配置缓存
4. **事件驱动** - 命令发布领域事件
5. **渐进迁移** - 不影响现有功能
6. **测试覆盖** - 每个服务 90%+ 覆盖率

---

## 2. 模块分析

### 2.1 模块对比表

| 模块         | 当前代码行数 | 命令方法  | 查询方法   | 读写比例 | CQRS 适用性 | 优先级 |
| ------------ | ------------ | --------- | ---------- | -------- | ----------- | ------ |
| **Category** | 475 行       | 5 个(25%) | 15 个(75%) | 1:3      | ✅ 高       | P1     |
| **Tag**      | 617 行       | 8 个(28%) | 21 个(72%) | 1:2.6    | ✅ 高       | P1     |
| **Comment**  | 348 行       | 2 个(18%) | 9 个(82%)  | 1:4.5    | ✅ 高       | P2     |
| **User**     | 255 行       | 1 个(11%) | 8 个(89%)  | 1:8      | ✅ 中       | P3     |

**总计**：1695 行代码，16 个命令方法，53 个查询方法

### 2.2 CQRS 适用性评估

#### ✅ 高度适合（Category, Tag, Comment）

**理由**：

- 读操作占比 > 70%
- 查询方法数量多（9-21 个）
- 有明确的命令操作
- 需要缓存优化
- 有事件发布需求

#### ⚠️ 适度适合（User）

**理由**：

- 读操作占比最高（89%）
- 但命令操作简单（只有 1 个）
- 查询方法较少（8 个）
- **建议**：简化版 CQRS，重点优化查询缓存

---

## 3. Category 模块 CQRS 设计

### 3.1 模块概况

```
当前: CategoryApplicationService (475行)
├── 命令方法 5个 (25%)
└── 查询方法 15个 (75%)

问题:
❌ 职责混合
❌ 无法独立优化
❌ 缓存策略缺失
```

### 3.2 CQRS 架构设计

```
After CQRS:

CategoryCommandService (~180行)       CategoryQueryService (~240行)
├── 命令方法 5个                      ├── 查询方法 15个
├── @Transactional(写事务)            ├── @Transactional(readOnly=true)
├── @CacheEvict(缓存失效)             ├── @Cacheable(查询缓存)
└── 发布领域事件                      └── 性能优化

优势:
✅ 职责清晰
✅ 独立优化
✅ 缓存友好
✅ 易于扩展
```

### 3.3 命令服务设计

#### 3.3.1 服务定义

```java
/**
 * 分类命令服务
 * 负责处理所有修改分类状态的操作
 */
@Service
@Transactional
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final DomainEventPublisher eventPublisher;

    // 5个命令方法
}
```

#### 3.3.2 命令方法清单

| 方法名             | 说明     | 命令对象                | 领域事件                 | 缓存失效                   |
| ------------------ | -------- | ----------------------- | ------------------------ | -------------------------- |
| `createCategory`   | 创建分类 | CreateCategoryCommand   | CategoryCreatedEvent     | categories, category-lists |
| `updateCategory`   | 更新分类 | UpdateCategoryCommand   | CategoryUpdatedEvent     | categories, category-lists |
| `deleteCategory`   | 删除分类 | DeleteCategoryCommand   | CategoryDeletedEvent     | categories, category-lists |
| `createCategories` | 批量创建 | CreateCategoriesCommand | CategoryCreatedEvent × N | categories, category-lists |

#### 3.3.3 命令对象设计

**CreateCategoryCommand**

```java
/**
 * 创建分类命令
 * 不可变对象，封装创建分类所需的所有参数
 */
public final class CreateCategoryCommand {
    private final String name;
    private final String icon;

    private CreateCategoryCommand(String name, String icon) {
        this.name = validateName(name);
        this.icon = icon;
    }

    public static CreateCategoryCommand of(String name, String icon) {
        return new CreateCategoryCommand(name, icon);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        return name.trim();
    }

    // Getters only (no setters - immutable)
    public String getName() { return name; }
    public String getIcon() { return icon; }
}
```

**UpdateCategoryCommand**

```java
/**
 * 更新分类命令
 */
public final class UpdateCategoryCommand {
    private final CategoryId categoryId;
    private final String name;
    private final String icon;

    private UpdateCategoryCommand(CategoryId categoryId, String name, String icon) {
        this.categoryId = validateCategoryId(categoryId);
        this.name = validateName(name);
        this.icon = icon;
    }

    public static UpdateCategoryCommand of(CategoryId categoryId, String name, String icon) {
        return new UpdateCategoryCommand(categoryId, name, icon);
    }

    // Validation and getters...
}
```

**DeleteCategoryCommand**

```java
/**
 * 删除分类命令
 */
public final class DeleteCategoryCommand {
    private final CategoryId categoryId;

    private DeleteCategoryCommand(CategoryId categoryId) {
        this.categoryId = validateCategoryId(categoryId);
    }

    public static DeleteCategoryCommand of(CategoryId categoryId) {
        return new DeleteCategoryCommand(categoryId);
    }

    // Validation and getters...
}
```

#### 3.3.4 命令服务实现示例

```java
@Service
@Transactional
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final DomainEventPublisher eventPublisher;

    public CategoryCommandService(
        CategoryRepository categoryRepository,
        ArticleRepository articleRepository,
        DomainEventPublisher eventPublisher
    ) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建分类
     *
     * @param command 创建命令
     * @return 创建的分类聚合
     */
    @CacheEvict(value = {"categories", "category-lists"}, allEntries = true)
    public CategoryAggregate createCategory(CreateCategoryCommand command) {
        logger.debug("执行创建分类命令: {}", command);

        // 检查名称是否已存在
        if (categoryRepository.existsByName(command.getName())) {
            throw new IllegalArgumentException("分类名称已存在: " + command.getName());
        }

        // 创建分类聚合
        CategoryAggregate category = CategoryAggregate.create(
            command.getName(),
            command.getIcon()
        );

        // 保存
        CategoryAggregate saved = categoryRepository.save(category);

        // 发布事件
        eventPublisher.publish(new CategoryCreatedEvent(
            saved.getId(),
            saved.getName()
        ));

        logger.info("成功创建分类: {}", saved.getId().getValue());
        return saved;
    }

    /**
     * 更新分类
     */
    @CacheEvict(value = {"categories", "category-lists"}, allEntries = true)
    public CategoryAggregate updateCategory(UpdateCategoryCommand command) {
        // 实现逻辑...
    }

    /**
     * 删除分类
     */
    @CacheEvict(value = {"categories", "category-lists"}, allEntries = true)
    public void deleteCategory(DeleteCategoryCommand command) {
        logger.debug("执行删除分类命令: {}", command);

        CategoryId categoryId = command.getCategoryId();

        // 检查分类是否存在
        CategoryAggregate category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        // 检查是否有文章使用该分类
        long articleCount = articleRepository.countByCategoryId(categoryId);
        if (articleCount > 0) {
            throw new IllegalStateException(
                "无法删除分类，该分类下还有 " + articleCount + " 篇文章"
            );
        }

        // 删除分类
        categoryRepository.deleteById(categoryId);

        // 发布事件
        eventPublisher.publish(new CategoryDeletedEvent(
            categoryId,
            category.getName()
        ));

        logger.info("成功删除分类: {}", categoryId.getValue());
    }
}
```

### 3.4 查询服务设计

#### 3.4.1 服务定义

```java
/**
 * 分类查询服务
 * 负责处理所有分类查询操作，配置缓存优化性能
 */
@Service
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    // 15个查询方法，全部配置缓存
}
```

#### 3.4.2 查询方法清单

| 方法名                  | 说明         | 缓存配置                     | 缓存 Key                | TTL     |
| ----------------------- | ------------ | ---------------------------- | ----------------------- | ------- |
| `findById`              | 根据 ID 查询 | @Cacheable("categories")     | #id                     | 15 分钟 |
| `findByIds`             | 批量查询     | @Cacheable("categories")     | #ids                    | 15 分钟 |
| `findByName`            | 根据名称查询 | @Cacheable("categories")     | #name                   | 15 分钟 |
| `findAll`               | 查询所有分类 | @Cacheable("category-lists") | 'all'                   | 5 分钟  |
| `findAllOrderByName`    | 排序查询     | @Cacheable("category-lists") | 'sorted:'+#asc          | 5 分钟  |
| `findWithPagination`    | 分页查询     | @Cacheable("category-lists") | 'page:'+#page+':'+#size | 5 分钟  |
| `searchByName`          | 名称搜索     | @Cacheable("category-lists") | 'search:'+#keyword      | 5 分钟  |
| `countAll`              | 统计总数     | @Cacheable("category-stats") | 'count'                 | 30 分钟 |
| `findRecentlyCreated`   | 最近创建     | @Cacheable("category-lists") | 'recent:'+#limit        | 5 分钟  |
| `existsById`            | 检查存在     | @Cacheable("category-stats") | 'exists:'+#id           | 30 分钟 |
| `existsByName`          | 名称检查     | @Cacheable("category-stats") | 'exists:'+#name         | 30 分钟 |
| `getCategoryUsageCount` | 使用统计     | @Cacheable("category-stats") | 'usage:'+#id            | 10 分钟 |
| `findWithArticleCount`  | 带文章数     | @Cacheable("category-lists") | 'with-count:'+#page     | 5 分钟  |

#### 3.4.3 查询服务实现示例

```java
@Service
@Transactional(readOnly = true)
public class CategoryQueryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryQueryService.class);

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    /**
     * 根据ID查找分类
     */
    @Cacheable(value = "categories", key = "#categoryId.value")
    public Optional<CategoryAggregate> findById(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        return categoryRepository.findById(categoryId);
    }

    /**
     * 查找所有分类
     */
    @Cacheable(value = "category-lists", key = "'all'")
    public List<CategoryAggregate> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * 分页查询分类
     */
    @Cacheable(
        value = "category-lists",
        key = "'page:' + #page + ':' + #size"
    )
    public List<CategoryAggregate> findWithPagination(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("无效的分页参数");
        }
        return categoryRepository.findAll(page, size);
    }

    /**
     * 统计分类总数
     */
    @Cacheable(value = "category-stats", key = "'count'")
    public long countAll() {
        return categoryRepository.count();
    }

    /**
     * 查询分类及其文章数量
     */
    @Cacheable(
        value = "category-lists",
        key = "'with-count:' + #page + ':' + #size"
    )
    public List<CategoryWithArticleCount> findWithArticleCount(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("无效的分页参数");
        }
        return categoryRepository.findCategoriesWithArticleCount(page, size);
    }
}
```

### 3.5 领域事件设计

```java
/**
 * 分类创建事件
 */
public class CategoryCreatedEvent {
    private final CategoryId categoryId;
    private final String categoryName;
    private final Instant occurredAt;

    public CategoryCreatedEvent(CategoryId categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.occurredAt = Instant.now();
    }

    // Getters...
}

/**
 * 分类更新事件
 */
public class CategoryUpdatedEvent {
    private final CategoryId categoryId;
    private final String oldName;
    private final String newName;
    private final Instant occurredAt;

    // Constructor and getters...
}

/**
 * 分类删除事件
 */
public class CategoryDeletedEvent {
    private final CategoryId categoryId;
    private final String categoryName;
    private final Instant occurredAt;

    // Constructor and getters...
}
```

### 3.6 预期收益

| 指标         | 改进前 | 改进后     | 提升     |
| ------------ | ------ | ---------- | -------- |
| 服务代码行数 | 475 行 | 180+240 行 | 职责分离 |
| 方法数       | 20 个  | 5+15 个    | 清晰划分 |
| 分类详情查询 | 15ms   | 1-2ms      | 10-15 倍 |
| 分类列表查询 | 50ms   | 3-5ms      | 10-15 倍 |
| 统计查询     | 30ms   | 1ms        | 30 倍    |
| 代码可维护性 | 中     | 高         | 显著提升 |

---

## 4. Tag 模块 CQRS 设计

### 4.1 模块概况

```
当前: TagApplicationService (617行)
├── 命令方法 8个 (28%)
└── 查询方法 21个 (72%)

特点:
- 最复杂的模块（617行）
- 已有事件发布机制
- 方法数量最多
```

### 4.2 CQRS 架构设计

```
After CQRS:

TagCommandService (~260行)            TagQueryService (~300行)
├── 命令方法 8个                      ├── 查询方法 21个
├── @Transactional(写事务)            ├── @Transactional(readOnly=true)
├── @CacheEvict(缓存失效)             ├── @Cacheable(查询缓存)
└── 发布领域事件(已有)                └── 性能优化

优势:
✅ 简化最复杂的服务
✅ 查询性能大幅提升
✅ 易于维护和扩展
```

### 4.3 命令服务设计

#### 4.3.1 命令方法清单

| 方法名                | 说明       | 命令对象                 | 领域事件              | 缓存失效        |
| --------------------- | ---------- | ------------------------ | --------------------- | --------------- |
| `createTag`           | 创建标签   | CreateTagCommand         | TagCreatedEvent       | tags, tag-lists |
| `updateTagName`       | 更新标签   | UpdateTagCommand         | TagUpdatedEvent       | tags, tag-lists |
| `deleteTag`           | 删除标签   | DeleteTagCommand         | TagDeletedEvent(已有) | tags, tag-lists |
| `createTags`          | 批量创建   | CreateTagsCommand        | TagCreatedEvent × N   | tags, tag-lists |
| `saveTags`            | 批量保存   | SaveTagsCommand          | -                     | tags, tag-lists |
| `deleteTags`          | 批量删除   | DeleteTagsCommand        | TagDeletedEvent × N   | tags, tag-lists |
| `cleanupUnusedTags`   | 清理未使用 | CleanupUnusedTagsCommand | TagDeletedEvent × N   | tags, tag-lists |
| `findOrCreateByNames` | 查找或创建 | FindOrCreateTagsCommand  | TagCreatedEvent × N   | tags, tag-lists |

#### 4.3.2 命令对象示例

```java
/**
 * 创建标签命令
 */
public final class CreateTagCommand {
    private final String name;

    private CreateTagCommand(String name) {
        this.name = validateName(name);
    }

    public static CreateTagCommand of(String name) {
        return new CreateTagCommand(name);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        return name.trim();
    }

    public String getName() { return name; }
}

/**
 * 批量创建标签命令
 */
public final class CreateTagsCommand {
    private final List<String> names;

    private CreateTagsCommand(List<String> names) {
        this.names = validateNames(names);
    }

    public static CreateTagsCommand of(List<String> names) {
        return new CreateTagsCommand(names);
    }

    private List<String> validateNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("标签名称列表不能为空");
        }
        return names.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .distinct()
            .toList();
    }

    public List<String> getNames() { return names; }
}

/**
 * 清理未使用标签命令
 */
public final class CleanupUnusedTagsCommand {
    private CleanupUnusedTagsCommand() {}

    public static CleanupUnusedTagsCommand create() {
        return new CleanupUnusedTagsCommand();
    }
}
```

### 4.4 查询服务设计

#### 4.4.1 查询方法清单（21 个）

**基础查询（6 个）**

| 方法名               | 缓存 Key                | TTL     |
| -------------------- | ----------------------- | ------- |
| `findById`           | #id                     | 15 分钟 |
| `findByIds`          | #ids                    | 15 分钟 |
| `findByName`         | #name                   | 15 分钟 |
| `findByNames`        | #names                  | 15 分钟 |
| `findAll`            | 'all'                   | 5 分钟  |
| `findWithPagination` | 'page:'+#page+':'+#size | 5 分钟  |

**排序查询（2 个）**

| 方法名                    | 缓存 Key            | TTL    |
| ------------------------- | ------------------- | ------ |
| `findAllOrderByName`      | 'sorted-name:'+#asc | 5 分钟 |
| `findAllOrderByCreatedAt` | 'sorted-time:'+#asc | 5 分钟 |

**搜索查询（2 个）**

| 方法名               | 缓存 Key           | TTL    |
| -------------------- | ------------------ | ------ |
| `searchByName`       | 'search:'+#keyword | 5 分钟 |
| `searchByNamePrefix` | 'prefix:'+#prefix  | 5 分钟 |

**统计查询（3 个）**

| 方法名         | 缓存 Key        | TTL     |
| -------------- | --------------- | ------- |
| `countAll`     | 'count'         | 30 分钟 |
| `existsById`   | 'exists:'+#id   | 30 分钟 |
| `existsByName` | 'exists:'+#name | 30 分钟 |

**特殊查询（4 个）**

| 方法名                 | 缓存 Key            | TTL     |
| ---------------------- | ------------------- | ------- |
| `findRecentlyCreated`  | 'recent:'+#limit    | 5 分钟  |
| `findPopularTags`      | 'popular:'+#limit   | 10 分钟 |
| `findUnusedTags`       | 'unused'            | 5 分钟  |
| `findWithArticleCount` | 'with-count:'+#page | 5 分钟  |

**业务查询（4 个）**

| 方法名             | 缓存 Key              | TTL     |
| ------------------ | --------------------- | ------- |
| `getTagUsageCount` | 'usage:'+#id          | 10 分钟 |
| `findShortTags`    | 'short:'+#maxLen      | 10 分钟 |
| `filterByKeyword`  | 'filter-kw:'+#keyword | 5 分钟  |
| `filterByPrefix`   | 'filter-pfx:'+#prefix | 5 分钟  |

### 4.5 预期收益

| 指标         | 改进前 | 改进后     | 提升     |
| ------------ | ------ | ---------- | -------- |
| 服务代码行数 | 617 行 | 260+300 行 | 职责分离 |
| 方法数       | 29 个  | 8+21 个    | 清晰划分 |
| 标签详情查询 | 10ms   | 1ms        | 10 倍    |
| 热门标签查询 | 80ms   | 5ms        | 16 倍    |
| 统计查询     | 25ms   | 1ms        | 25 倍    |

---

## 5. User 模块 CQRS 设计

### 5.1 模块概况

```
当前: UserApplicationService (255行)
├── 命令方法 1个 (11%)
└── 查询方法 8个 (89%)

特点:
- 最简单的模块
- 读操作占比最高(89%)
- 命令操作简单
```

### 5.2 简化版 CQRS 设计

由于 User 模块命令操作很少（只有 1 个），建议采用**简化版 CQRS**：

```
After CQRS (简化版):

UserCommandService (~100行)           UserQueryService (~150行)
├── 命令方法 1个                      ├── 查询方法 8个
├── @Transactional(写事务)            ├── @Transactional(readOnly=true)
├── @CacheEvict(缓存失效)             ├── @Cacheable(查询缓存)
└── 可选事件发布                      └── 重点优化

策略:
✅ 重点优化查询性能(缓存)
⚠️ 命令简化处理
⚠️ 事件发布可选
```

### 5.3 命令服务设计

#### 5.3.1 命令方法

| 方法名                         | 说明            | 命令对象              | 领域事件（可选）                   | 缓存失效          |
| ------------------------------ | --------------- | --------------------- | ---------------------------------- | ----------------- |
| `createOrUpdateUserFromGitHub` | GitHub 用户同步 | SyncGitHubUserCommand | UserCreatedEvent, UserUpdatedEvent | users, user-lists |

#### 5.3.2 命令对象

```java
/**
 * 同步GitHub用户命令
 */
public final class SyncGitHubUserCommand {
    private final GitHubId gitHubId;
    private final String username;
    private final String avatarUrl;

    private SyncGitHubUserCommand(
        GitHubId gitHubId,
        String username,
        String avatarUrl
    ) {
        this.gitHubId = validateGitHubId(gitHubId);
        this.username = validateUsername(username);
        this.avatarUrl = avatarUrl;
    }

    public static SyncGitHubUserCommand fromGitHubInfo(GitHubUserInfo info) {
        return new SyncGitHubUserCommand(
            GitHubId.of(info.getId()),
            info.getDisplayName(),
            info.getAvatarUrl()
        );
    }

    // Validation and getters...
}
```

#### 5.3.3 命令服务实现

```java
@Service
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    // 事件发布可选
    // private final DomainEventPublisher eventPublisher;

    @CacheEvict(value = {"users", "user-lists"}, allEntries = true)
    public UserAggregate syncUserFromGitHub(SyncGitHubUserCommand command) {
        logger.debug("执行GitHub用户同步命令: {}", command);

        Optional<UserAggregate> existing = userRepository
            .findByGitHubId(command.getGitHubId());

        UserAggregate user;
        if (existing.isPresent()) {
            // 更新用户
            user = existing.get();
            user.updateProfile(command.getUsername(), command.getAvatarUrl());
        } else {
            // 创建用户
            user = UserAggregate.createFromGitHub(
                command.getGitHubId(),
                command.getUsername(),
                command.getAvatarUrl()
            );
        }

        return userRepository.save(user);
    }
}
```

### 5.4 查询服务设计

#### 5.4.1 查询方法清单（8 个）

| 方法名               | 缓存配置                 | 缓存 Key                | TTL     |
| -------------------- | ------------------------ | ----------------------- | ------- |
| `findById`           | @Cacheable("users")      | #id                     | 30 分钟 |
| `findByIds`          | @Cacheable("users")      | #ids                    | 30 分钟 |
| `findByGitHubId`     | @Cacheable("users")      | 'gh:'+#ghId             | 30 分钟 |
| `findByUsername`     | @Cacheable("users")      | 'un:'+#username         | 30 分钟 |
| `existsByGitHubId`   | @Cacheable("user-stats") | 'exists-gh:'+#ghId      | 60 分钟 |
| `existsByUsername`   | @Cacheable("user-stats") | 'exists-un:'+#username  | 60 分钟 |
| `findWithPagination` | @Cacheable("user-lists") | 'page:'+#page+':'+#size | 10 分钟 |
| `countAll`           | @Cacheable("user-stats") | 'count'                 | 60 分钟 |

#### 5.4.2 查询服务实现

```java
@Service
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#userId.value")
    public Optional<UserAggregate> findById(UserId userId) {
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    @Cacheable(value = "users", key = "'gh:' + #gitHubId.value")
    public Optional<UserAggregate> findByGitHubId(GitHubId gitHubId) {
        if (gitHubId == null) return Optional.empty();
        return userRepository.findByGitHubId(gitHubId);
    }

    @Cacheable(value = "user-lists", key = "'page:' + #page + ':' + #size")
    public UserListResponse findWithPagination(int page, int size) {
        // 实现逻辑...
    }

    @Cacheable(value = "user-stats", key = "'count'")
    public long countAll() {
        return userRepository.count();
    }
}
```

### 5.5 预期收益

| 指标         | 改进前 | 改进后     | 提升     |
| ------------ | ------ | ---------- | -------- |
| 服务代码行数 | 255 行 | 100+150 行 | 职责分离 |
| 用户详情查询 | 10ms   | 1ms        | 10 倍    |
| 用户列表查询 | 50ms   | 3-5ms      | 10-15 倍 |
| exists 检查  | 5ms    | <1ms       | 5-10 倍  |

---

## 6. Comment 模块 CQRS 设计

### 6.1 模块概况

```
当前: CommentApplicationService (348行)
├── 命令方法 2个 (18%) - 已使用Command对象 ✅
└── 查询方法 9个 (82%)

特点:
- 已部分实现CQRS(命令对象)
- 需要完善查询优化
```

### 6.2 CQRS 架构设计

```
After CQRS:

CommentCommandService (~150行)        CommentQueryService (~180行)
├── 命令方法 2个(已有Command)          ├── 查询方法 9个
├── @Transactional(写事务)            ├── @Transactional(readOnly=true)
├── @CacheEvict(缓存失效)             ├── @Cacheable(查询缓存)
└── 发布领域事件                      └── 性能优化

优势:
✅ 基于现有Command对象
✅ 补充查询优化
✅ 评论缓存策略
```

### 6.3 命令服务设计

#### 6.3.1 现有命令对象

已经存在的命令对象（需要继续使用）：

- ✅ `CreateCommentCommand` - 创建评论命令
- ✅ `DeleteCommentCommand` - 删除评论命令
- ✅ `GetCommentsQuery` - 查询评论（建议迁移到 QueryService）

#### 6.3.2 命令方法清单

| 方法名          | 命令对象                   | 领域事件            | 缓存失效                |
| --------------- | -------------------------- | ------------------- | ----------------------- |
| `createComment` | CreateCommentCommand(已有) | CommentCreatedEvent | comments, comment-lists |
| `deleteComment` | DeleteCommentCommand(已有) | CommentDeletedEvent | comments, comment-lists |

#### 6.3.3 命令服务实现

```java
@Service
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * 创建评论
     */
    @CacheEvict(value = {"comments", "comment-lists"}, allEntries = true)
    public CommentAggregate createComment(CreateCommentCommand command) {
        logger.debug("执行创建评论命令: {}", command);

        // 验证文章ID
        ArticleId articleId = new ArticleId(command.getArticleId().toString());
        UserId userId = UserId.of(command.getUserId());

        CommentAggregate comment;
        if (command.isReply()) {
            CommentId parentId = new CommentId(command.getParentId());

            // 验证父评论
            CommentAggregate parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("父评论不存在"));

            if (!parent.getArticleId().equals(articleId)) {
                throw new IllegalArgumentException("父评论不属于指定文章");
            }

            comment = CommentAggregate.createReply(
                command.getContent(), articleId, userId, parentId
            );
        } else {
            comment = CommentAggregate.createComment(
                command.getContent(), articleId, userId
            );
        }

        CommentAggregate saved = commentRepository.save(comment);

        // 发布事件
        eventPublisher.publish(new CommentCreatedEvent(
            saved.getId(),
            saved.getArticleId(),
            saved.getUserId()
        ));

        logger.info("成功创建评论: {}", saved.getId().getValue());
        return saved;
    }

    /**
     * 删除评论（递归删除子评论）
     */
    @CacheEvict(value = {"comments", "comment-lists"}, allEntries = true)
    public void deleteComment(DeleteCommentCommand command) {
        logger.debug("执行删除评论命令: {}", command);

        CommentId commentId = new CommentId(command.getCommentId());
        UserId userId = UserId.of(command.getUserId());

        // 验证评论存在和权限
        CommentAggregate comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除该评论");
        }

        // 递归删除子评论
        deleteCommentRecursively(commentId);

        // 发布事件
        eventPublisher.publish(new CommentDeletedEvent(
            commentId,
            comment.getArticleId()
        ));

        logger.info("成功删除评论: {}", commentId.getValue());
    }

    private void deleteCommentRecursively(CommentId commentId) {
        List<CommentAggregate> replies = commentRepository.findByParentId(commentId);
        for (CommentAggregate reply : replies) {
            deleteCommentRecursively(reply.getId());
        }
        commentRepository.deleteById(commentId);
    }
}
```

### 6.4 查询服务设计

#### 6.4.1 查询方法清单（9 个）

| 方法名                            | 缓存配置                    | 缓存 Key                       | TTL     |
| --------------------------------- | --------------------------- | ------------------------------ | ------- |
| `findById`                        | @Cacheable("comments")      | #id                            | 10 分钟 |
| `findByArticleId`                 | @Cacheable("comment-lists") | 'article:'+#articleId          | 5 分钟  |
| `findTopLevelByArticleId`         | @Cacheable("comment-lists") | 'top:'+#articleId              | 5 分钟  |
| `findTopLevelByArticleIdWithPage` | @Cacheable("comment-lists") | 'top:'+#articleId+':'+#page    | 5 分钟  |
| `findRepliesByParentId`           | @Cacheable("comment-lists") | 'replies:'+#parentId           | 5 分钟  |
| `findRepliesByParentIdWithPage`   | @Cacheable("comment-lists") | 'replies:'+#parentId+':'+#page | 5 分钟  |
| `countByArticleId`                | @Cacheable("comment-stats") | 'count:'+#articleId            | 15 分钟 |
| `countTopLevelByArticleId`        | @Cacheable("comment-stats") | 'count-top:'+#articleId        | 15 分钟 |
| `countRepliesByParentId`          | @Cacheable("comment-stats") | 'count-replies:'+#parentId     | 15 分钟 |

#### 6.4.2 查询服务实现

```java
@Service
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;

    @Cacheable(value = "comments", key = "#commentId.value")
    public Optional<CommentAggregate> findById(CommentId commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        return commentRepository.findById(commentId);
    }

    @Cacheable(
        value = "comment-lists",
        key = "'article:' + #articleId.value"
    )
    public List<CommentAggregate> findByArticleId(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        return commentRepository.findByArticleId(articleId);
    }

    @Cacheable(
        value = "comment-lists",
        key = "'top:' + #articleId.value + ':' + #page + ':' + #size"
    )
    public List<CommentAggregate> findTopLevelByArticleId(
        ArticleId articleId, int page, int size
    ) {
        validatePagination(page, size);
        return commentRepository.findTopLevelCommentsByArticleId(
            articleId, page, size
        );
    }

    @Cacheable(
        value = "comment-stats",
        key = "'count:' + #articleId.value"
    )
    public long countByArticleId(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        return commentRepository.countByArticleId(articleId);
    }
}
```

### 6.5 预期收益

| 指标         | 改进前 | 改进后     | 提升     |
| ------------ | ------ | ---------- | -------- |
| 服务代码行数 | 348 行 | 150+180 行 | 职责分离 |
| 评论列表查询 | 60ms   | 3-5ms      | 12-20 倍 |
| 评论数统计   | 40ms   | 1-2ms      | 20-40 倍 |
| 回复查询     | 50ms   | 3ms        | 15 倍    |

---

## 7. 实施优先级

### 7.1 优先级排序

| 优先级    | 模块     | 复杂度 | 预期用时 | 理由                             |
| --------- | -------- | ------ | -------- | -------------------------------- |
| **P1-高** | Category | 中     | 1-1.5 天 | 使用频繁，方法较多，性能提升明显 |
| **P1-高** | Tag      | 高     | 1.5-2 天 | 最复杂，性能瓶颈，热门查询多     |
| **P2-中** | Comment  | 中     | 1 天     | 已有 Command 基础，补充查询优化  |
| **P3-低** | User     | 低     | 0.5-1 天 | 简单模块，简化版 CQRS            |

### 7.2 推荐实施顺序

```
阶段1: Category (1-1.5天) ✅
  ↓
阶段2: Tag (1.5-2天) ✅
  ↓
阶段3: Comment (1天) ✅
  ↓
阶段4: User (0.5-1天) ✅
────────────────────────
总计: 4-5.5天
```

### 7.3 并行实施建议

如果有多人协作，可以并行：

```
开发者A: Category + Comment (2-2.5天)
开发者B: Tag + User (2-3天)
────────────────────────────────
总计(并行): 2-3天
```

---

## 8. 缓存策略

### 8.1 缓存区域设计

| 缓存区域         | 用途     | TTL     | 失效策略                   |
| ---------------- | -------- | ------- | -------------------------- |
| `categories`     | 分类详情 | 15 分钟 | 命令操作时 allEntries=true |
| `category-lists` | 分类列表 | 5 分钟  | 命令操作时 allEntries=true |
| `category-stats` | 分类统计 | 30 分钟 | 命令操作时 allEntries=true |
| `tags`           | 标签详情 | 15 分钟 | 命令操作时 allEntries=true |
| `tag-lists`      | 标签列表 | 5 分钟  | 命令操作时 allEntries=true |
| `tag-stats`      | 标签统计 | 30 分钟 | 命令操作时 allEntries=true |
| `users`          | 用户详情 | 30 分钟 | 命令操作时 allEntries=true |
| `user-lists`     | 用户列表 | 10 分钟 | 命令操作时 allEntries=true |
| `user-stats`     | 用户统计 | 60 分钟 | 命令操作时 allEntries=true |
| `comments`       | 评论详情 | 10 分钟 | 命令操作时 allEntries=true |
| `comment-lists`  | 评论列表 | 5 分钟  | 命令操作时 allEntries=true |
| `comment-stats`  | 评论统计 | 15 分钟 | 命令操作时 allEntries=true |

### 8.2 缓存配置类

扩展现有的 `CacheConfig.java`:

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeValuesWith(/* JSON序列化 */);

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
            // Article (已有)
            "articles", defaultConfig.entryTtl(Duration.ofMinutes(15)),
            "article-lists", defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "article-stats", defaultConfig.entryTtl(Duration.ofMinutes(30)),

            // Category
            "categories", defaultConfig.entryTtl(Duration.ofMinutes(15)),
            "category-lists", defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "category-stats", defaultConfig.entryTtl(Duration.ofMinutes(30)),

            // Tag
            "tags", defaultConfig.entryTtl(Duration.ofMinutes(15)),
            "tag-lists", defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "tag-stats", defaultConfig.entryTtl(Duration.ofMinutes(30)),

            // User
            "users", defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "user-lists", defaultConfig.entryTtl(Duration.ofMinutes(10)),
            "user-stats", defaultConfig.entryTtl(Duration.ofMinutes(60)),

            // Comment
            "comments", defaultConfig.entryTtl(Duration.ofMinutes(10)),
            "comment-lists", defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "comment-stats", defaultConfig.entryTtl(Duration.ofMinutes(15))
        );

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
```

### 8.3 缓存失效规则

所有命令方法统一使用：

```java
@CacheEvict(
    value = {"xxx", "xxx-lists", "xxx-stats"},
    allEntries = true
)
```

**优点**：

- ✅ 简单可靠，不会遗漏
- ✅ 避免缓存不一致
- ✅ 性能损失可接受（命令操作少）

**缺点**：

- ⚠️ 清除全部缓存，略显粗暴

**未来优化**：

```java
// 精细化缓存失效（可选）
@CacheEvict(value = "categories", key = "#categoryId.value")
```

---

## 9. 事件设计

### 9.1 Category 事件

```java
// CategoryCreatedEvent
public class CategoryCreatedEvent {
    private final CategoryId categoryId;
    private final String categoryName;
    private final Instant occurredAt;
}

// CategoryUpdatedEvent
public class CategoryUpdatedEvent {
    private final CategoryId categoryId;
    private final String oldName;
    private final String newName;
    private final Instant occurredAt;
}

// CategoryDeletedEvent
public class CategoryDeletedEvent {
    private final CategoryId categoryId;
    private final String categoryName;
    private final Instant occurredAt;
}
```

### 9.2 Tag 事件

```java
// TagCreatedEvent
public class TagCreatedEvent {
    private final TagId tagId;
    private final String tagName;
    private final Instant occurredAt;
}

// TagUpdatedEvent
public class TagUpdatedEvent {
    private final TagId tagId;
    private final String oldName;
    private final String newName;
    private final Instant occurredAt;
}

// TagDeletedEvent (已存在)
public class TagDeletedEvent {
    private final TagId tagId;
    private final String tagName;
    private final Instant occurredAt;
}
```

### 9.3 User 事件（可选）

```java
// UserCreatedEvent (可选)
public class UserCreatedEvent {
    private final UserId userId;
    private final GitHubId gitHubId;
    private final String username;
    private final Instant occurredAt;
}

// UserUpdatedEvent (可选)
public class UserUpdatedEvent {
    private final UserId userId;
    private final String oldUsername;
    private final String newUsername;
    private final Instant occurredAt;
}
```

### 9.4 Comment 事件

```java
// CommentCreatedEvent
public class CommentCreatedEvent {
    private final CommentId commentId;
    private final ArticleId articleId;
    private final UserId userId;
    private final boolean isReply;
    private final Instant occurredAt;
}

// CommentDeletedEvent
public class CommentDeletedEvent {
    private final CommentId commentId;
    private final ArticleId articleId;
    private final Instant occurredAt;
}
```

### 9.5 事件处理建议

```java
// 示例：评论创建后更新文章评论数
@Component
public class CommentEventHandler {

    @EventListener
    @Async
    public void handleCommentCreated(CommentCreatedEvent event) {
        // 更新文章评论数缓存
        // 发送通知等
    }

    @EventListener
    @Async
    public void handleCommentDeleted(CommentDeletedEvent event) {
        // 更新文章评论数缓存
    }
}
```

---

## 10. 代码统计

### 10.1 改造前后对比

| 模块     | 改造前      | CommandService | QueryService | 命令对象   | 事件类     | 总代码      | 净变化            |
| -------- | ----------- | -------------- | ------------ | ---------- | ---------- | ----------- | ----------------- |
| Category | 475 行      | 180 行         | 240 行       | 180 行     | 90 行      | 690 行      | +215 行(+45%)     |
| Tag      | 617 行      | 260 行         | 300 行       | 240 行     | 90 行      | 890 行      | +273 行(+44%)     |
| User     | 255 行      | 100 行         | 150 行       | 80 行      | 60 行      | 390 行      | +135 行(+53%)     |
| Comment  | 348 行      | 150 行         | 180 行       | 0 行(已有) | 60 行      | 390 行      | +42 行(+12%)      |
| **合计** | **1695 行** | **690 行**     | **870 行**   | **500 行** | **300 行** | **2360 行** | **+665 行(+39%)** |

### 10.2 文件结构变化

```
application/
├── article/          (已完成CQRS)
│   ├── command/
│   │   ├── CreateArticleDraftCommand.java
│   │   ├── UpdateArticleCommand.java
│   │   └── PublishArticleCommand.java
│   └── service/
│       ├── ArticleCommandService.java
│       ├── ArticleQueryService.java
│       └── CategoryApplicationService.java (待拆分)
│       └── TagApplicationService.java (待拆分)
│
├── category/         (新增)
│   ├── command/
│   │   ├── CreateCategoryCommand.java
│   │   ├── UpdateCategoryCommand.java
│   │   └── DeleteCategoryCommand.java
│   └── service/
│       ├── CategoryCommandService.java
│       └── CategoryQueryService.java
│
├── tag/              (新增)
│   ├── command/
│   │   ├── CreateTagCommand.java
│   │   ├── UpdateTagCommand.java
│   │   ├── DeleteTagCommand.java
│   │   └── CreateTagsCommand.java
│   └── service/
│       ├── TagCommandService.java
│       └── TagQueryService.java
│
├── user/
│   ├── command/      (新增)
│   │   └── SyncGitHubUserCommand.java
│   └── service/
│       ├── UserCommandService.java (新增)
│       ├── UserQueryService.java (新增)
│       └── UserApplicationService.java (待拆分)
│
└── comment/
    ├── command/      (已存在，继续使用)
    │   ├── CreateCommentCommand.java ✅
    │   ├── DeleteCommentCommand.java ✅
    │   └── GetCommentsQuery.java (迁移到QueryService)
    └── service/
        ├── CommentCommandService.java (新增)
        ├── CommentQueryService.java (新增)
        └── CommentApplicationService.java (待拆分)
```

### 10.3 测试文件统计

每个模块需要创建测试文件：

| 模块     | 命令服务测试 | 查询服务测试 | 命令对象测试 | 总测试代码   |
| -------- | ------------ | ------------ | ------------ | ------------ |
| Category | ~200 行      | ~150 行      | ~100 行      | ~450 行      |
| Tag      | ~250 行      | ~180 行      | ~120 行      | ~550 行      |
| User     | ~100 行      | ~120 行      | ~50 行       | ~270 行      |
| Comment  | ~150 行      | ~130 行      | 0 行(已有)   | ~280 行      |
| **合计** | **~700 行**  | **~580 行**  | **~270 行**  | **~1550 行** |

---

## 11. 实施步骤

### 11.1 Category 模块实施计划

#### 阶段 1: 准备工作（2-3 小时）

**任务**：

1. ✅ 创建包结构 `application/category/`
2. ✅ 创建命令对象（3 个）
   - CreateCategoryCommand
   - UpdateCategoryCommand
   - DeleteCategoryCommand
3. ✅ 创建领域事件（3 个）
   - CategoryCreatedEvent
   - CategoryUpdatedEvent
   - CategoryDeletedEvent
4. ✅ 创建命令对象单元测试

**验证标准**：

- [ ] 所有命令对象编译通过
- [ ] 命令对象测试覆盖率 > 90%
- [ ] 事件类定义完整

#### 阶段 2: 命令服务实施（3-4 小时）

**任务**：

1. ✅ 创建 `CategoryCommandService.java`
2. ✅ 实现 5 个命令方法
   - createCategory
   - updateCategory
   - deleteCategory
   - createCategories
3. ✅ 配置缓存失效注解
4. ✅ 集成事件发布
5. ✅ 编写命令服务单元测试

**验证标准**：

- [ ] 所有命令方法实现完成
- [ ] 事件正确发布
- [ ] 缓存正确失效
- [ ] 测试覆盖率 > 85%

#### 阶段 3: 查询服务实施（3-4 小时）

**任务**：

1. ✅ 创建 `CategoryQueryService.java`
2. ✅ 实现 15 个查询方法
3. ✅ 配置@Cacheable 注解（13 个方法）
4. ✅ 配置缓存 key 和 TTL
5. ✅ 编写查询服务单元测试

**验证标准**：

- [ ] 所有查询方法实现完成
- [ ] 缓存配置正确
- [ ] 测试覆盖率 > 85%

#### 阶段 4: 控制器集成（1-2 小时）

**任务**：

1. ✅ 更新 CategoryController
2. ✅ 注入 CommandService 和 QueryService
3. ✅ 更新 API 方法调用
4. ✅ 编写集成测试

**验证标准**：

- [ ] 所有 API 功能正常
- [ ] 集成测试通过

#### 阶段 5: 清理和文档（1 小时）

**任务**：

1. ✅ 标记旧服务 @Deprecated
2. ✅ 删除旧服务代码
3. ✅ 更新相关文档
4. ✅ 代码 review

**验证标准**：

- [ ] 无编译错误
- [ ] 所有测试通过
- [ ] 文档更新完成

**Category 模块总计：1-1.5 天**

---

### 11.2 Tag 模块实施计划

#### 阶段 1: 准备工作（3-4 小时）

**任务**：

1. ✅ 创建包结构 `application/tag/`
2. ✅ 创建命令对象（7 个）
   - CreateTagCommand
   - UpdateTagCommand
   - DeleteTagCommand
   - CreateTagsCommand
   - SaveTagsCommand
   - DeleteTagsCommand
   - CleanupUnusedTagsCommand
3. ✅ 创建领域事件（2 个，TagDeletedEvent 已存在）
   - TagCreatedEvent
   - TagUpdatedEvent
4. ✅ 创建命令对象单元测试

**验证标准**：

- [ ] 所有命令对象编译通过
- [ ] 命令对象测试覆盖率 > 90%

#### 阶段 2: 命令服务实施（4-5 小时）

**任务**：

1. ✅ 创建 `TagCommandService.java`
2. ✅ 实现 8 个命令方法
3. ✅ 复用现有 TagDeletedEvent
4. ✅ 配置缓存失效注解
5. ✅ 编写命令服务单元测试

**验证标准**：

- [ ] 所有命令方法实现完成
- [ ] 事件正确发布
- [ ] 测试覆盖率 > 85%

#### 阶段 3: 查询服务实施（4-5 小时）

**任务**：

1. ✅ 创建 `TagQueryService.java`
2. ✅ 实现 21 个查询方法
3. ✅ 配置@Cacheable 注解（19 个方法）
4. ✅ 编写查询服务单元测试

**验证标准**：

- [ ] 所有查询方法实现完成
- [ ] 缓存配置正确
- [ ] 测试覆盖率 > 85%

#### 阶段 4: 控制器集成（1-2 小时）

**任务**：

1. ✅ 更新 TagController
2. ✅ 更新 API 方法调用
3. ✅ 编写集成测试

**验证标准**：

- [ ] 所有 API 功能正常
- [ ] 集成测试通过

#### 阶段 5: 清理和文档（1 小时）

**任务**：

1. ✅ 清理旧代码
2. ✅ 更新文档

**Tag 模块总计：1.5-2 天**

---

### 11.3 Comment 模块实施计划

#### 阶段 1: 准备工作（1-2 小时）

**任务**：

1. ✅ 创建领域事件（2 个）
   - CommentCreatedEvent
   - CommentDeletedEvent
2. ✅ 创建事件单元测试

**注意**：命令对象已存在，无需创建

**验证标准**：

- [ ] 事件类编译通过
- [ ] 事件测试通过

#### 阶段 2: 命令服务实施（2-3 小时）

**任务**：

1. ✅ 创建 `CommentCommandService.java`
2. ✅ 迁移 2 个命令方法（复用现有 Command）
3. ✅ 配置缓存失效
4. ✅ 集成事件发布
5. ✅ 编写命令服务单元测试

**验证标准**：

- [ ] 命令方法迁移完成
- [ ] 事件正确发布
- [ ] 测试覆盖率 > 85%

#### 阶段 3: 查询服务实施（3-4 小时）

**任务**：

1. ✅ 创建 `CommentQueryService.java`
2. ✅ 实现 9 个查询方法
3. ✅ 配置@Cacheable 注解（9 个方法）
4. ✅ 编写查询服务单元测试

**验证标准**：

- [ ] 所有查询方法实现完成
- [ ] 缓存配置正确
- [ ] 测试覆盖率 > 85%

#### 阶段 4: 控制器集成（1 小时）

**任务**：

1. ✅ 更新 CommentController
2. ✅ 更新 API 方法调用
3. ✅ 编写集成测试

**验证标准**：

- [ ] 所有 API 功能正常
- [ ] 集成测试通过

#### 阶段 5: 清理和文档（0.5 小时）

**任务**：

1. ✅ 清理旧代码
2. ✅ 更新文档

**Comment 模块总计：1 天**

---

### 11.4 User 模块实施计划（简化版）

#### 阶段 1: 准备工作（1-1.5 小时）

**任务**：

1. ✅ 创建包结构 `application/user/command/`
2. ✅ 创建命令对象（1 个）
   - SyncGitHubUserCommand
3. ✅ 创建领域事件（2 个，可选）
   - UserCreatedEvent (可选)
   - UserUpdatedEvent (可选)
4. ✅ 创建命令对象单元测试

**验证标准**：

- [ ] 命令对象编译通过
- [ ] 命令对象测试通过

#### 阶段 2: 命令服务实施（1.5-2 小时）

**任务**：

1. ✅ 创建 `UserCommandService.java`
2. ✅ 实现 1 个命令方法
3. ✅ 配置缓存失效
4. ✅ 编写命令服务单元测试

**验证标准**：

- [ ] 命令方法实现完成
- [ ] 测试覆盖率 > 85%

#### 阶段 3: 查询服务实施（2-3 小时）

**任务**：

1. ✅ 创建 `UserQueryService.java`
2. ✅ 实现 8 个查询方法
3. ✅ 配置@Cacheable 注解（8 个方法）
4. ✅ 编写查询服务单元测试

**验证标准**：

- [ ] 所有查询方法实现完成
- [ ] 缓存配置正确
- [ ] 测试覆盖率 > 85%

#### 阶段 4: 控制器集成（0.5-1 小时）

**任务**：

1. ✅ 更新 UserController (如果有)
2. ✅ 更新 API 方法调用
3. ✅ 编写集成测试

**验证标准**：

- [ ] 所有 API 功能正常
- [ ] 集成测试通过

#### 阶段 5: 清理和文档（0.5 小时）

**任务**：

1. ✅ 清理旧代码
2. ✅ 更新文档

**User 模块总计：0.5-1 天**

---

### 11.5 总体实施时间线

#### 单人实施（串行）

```
Day 1: Category 模块
  ├── 上午: 准备 + 命令服务
  └── 下午: 查询服务 + 控制器 + 清理

Day 2: Tag 模块
  ├── 上午: 准备 + 命令服务
  └── 下午: 查询服务

Day 3: Tag 模块完成 + Comment 模块
  ├── 上午: Tag 控制器 + 清理
  └── 下午: Comment 完整实施

Day 4: User 模块 + 总体测试
  ├── 上午: User 完整实施
  └── 下午: 集成测试 + 文档

总计: 4-5天
```

#### 双人并行实施

```
开发者A:
Day 1: Category 模块
Day 2: Comment 模块
Day 3: 集成测试

开发者B:
Day 1-2: Tag 模块
Day 3: User 模块 + 集成测试

总计: 3天
```

---

## 12. 总结

### 12.1 核心要点

1. **参考 Article 成功经验**

   - 命令与查询分离
   - 不可变命令对象
   - 缓存优化策略
   - 事件驱动设计

2. **差异化策略**

   - Category/Tag: 标准 CQRS
   - Comment: 基于现有 Command
   - User: 简化版 CQRS

3. **预期收益**
   - 查询性能提升 10-50 倍
   - 代码职责更清晰
   - 易于维护和扩展

### 12.2 风险控制

1. **渐进式迁移**

   - 新旧服务并存
   - 逐步切换
   - 可随时回滚

2. **充分测试**

   - 单元测试覆盖率 > 85%
   - 集成测试覆盖所有 API
   - 性能测试验证缓存效果

3. **文档完整**
   - 详细的设计文档
   - 实施任务清单
   - 进度跟踪表

### 12.3 下一步行动

1. ✅ 审阅本设计文档
2. ⬜ 创建详细的任务清单
3. ⬜ 创建进度跟踪表
4. ⬜ 开始实施（建议从 Category 开始）

---

**文档版本**：v1.0  
**创建日期**：2025-10-01  
**最后更新**：2025-10-01  
**作者**：CleverOnion Team

---

**相关文档**：

- [Article 模块 CQRS 实施指南](./cqrs-implementation-guide.md)
- [CQRS 快速参考](./cqrs-quick-reference.md)
- [CQRS 查询对象指南](./cqrs-query-object-guide.md)
- [Article 模块完成报告](../COMPLETION_REPORT.md)


