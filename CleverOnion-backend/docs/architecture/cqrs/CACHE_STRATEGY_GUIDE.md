# CQRS 缓存策略指南

## 📅 创建日期：2025-10-01

---

## 🎯 缓存原则

### ✅ 正确做法：在 Controller 层缓存 Response 对象

```java
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @GetMapping("/{id}")
    @Cacheable(value = "category-responses", key = "#id")
    public Result<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryId categoryId = CategoryId.of(id);
        Optional<CategoryAggregate> categoryOpt = categoryQueryService.findById(categoryId);

        if (categoryOpt.isEmpty()) {
            throw new ResourceNotFoundException("分类不存在");
        }

        CategoryResponse response = new CategoryResponse(categoryOpt.get());
        return Result.success(response);
    }
}
```

**优点**：

- ✅ 领域层保持纯粹（无技术依赖）
- ✅ 缓存 Response 对象（DTO），序列化无问题
- ✅ 符合 DDD 分层架构原则

### ❌ 错误做法：在 QueryService 层缓存聚合根

```java
// ❌ 不要这样做
@Service
public class CategoryQueryService {

    @Cacheable(value = "categories", key = "#categoryId.value")  // ❌ 错误
    public Optional<CategoryAggregate> findById(CategoryId categoryId) {
        // ...
    }
}
```

**问题**：

- ❌ 领域层包含技术依赖（@Cacheable 注解）
- ❌ 序列化领域对象可能有问题（值对象反序列化）
- ❌ 违反 DDD 分层架构原则

---

## 📋 推荐缓存端点

### Category 模块

```java
// 1. 查询分类详情
@GetMapping("/{id}")
@Cacheable(value = "category-responses", key = "#id")
public Result<CategoryResponse> getCategoryById(@PathVariable Long id)

// 2. 查询所有分类
@GetMapping
@Cacheable(value = "category-list-responses", key = "#sortBy + ':' + #order")
public Result<CategoryListResponse> getAllCategories(
    @RequestParam(defaultValue = "name") String sortBy,
    @RequestParam(defaultValue = "asc") String order)

// 3. 分页查询分类
@GetMapping("/page")
@Cacheable(value = "category-page-responses", key = "'page:' + #page + ':' + #size")
public Result<CategoryListResponse> getCategoriesWithPagination(
    @RequestParam(defaultValue = "0") Integer page,
    @RequestParam(defaultValue = "10") Integer size)
```

### Tag 模块

```java
// 1. 查询标签详情
@GetMapping("/{id}")
@Cacheable(value = "tag-responses", key = "#id")
public Result<TagResponse> getTagById(@PathVariable Long id)

// 2. 查询所有标签
@GetMapping
@Cacheable(value = "tag-list-responses", key = "'all'")
public Result<TagListResponse> getAllTags()

// 3. 查询热门标签
@GetMapping("/page-with-count")
@Cacheable(value = "tag-popular-responses", key = "'page:' + #page + ':' + #size")
public Result<TagWithCountListResponse> getTagsWithArticleCount(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size)
```

### Comment 模块

```java
// 1. 查询文章评论列表
@GetMapping
@Cacheable(value = "comment-list-responses", key = "#articleId + ':' + #page + ':' + #size")
public Result<CommentListResponse> getComments(
    @RequestParam Long articleId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size)

// 2. 查询顶级评论
@GetMapping("/top-level")
@Cacheable(value = "comment-top-responses", key = "#articleId + ':' + #page + ':' + #size")
public Result<CommentListResponse> getTopLevelComments(
    @RequestParam Long articleId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size)
```

### User 模块

```java
// 1. 查询用户详情
@GetMapping("/{id}")
@Cacheable(value = "user-responses", key = "#id")
public Result<UserResponse> getUserById(@PathVariable Long id)

// 2. 分页查询用户
@GetMapping
@Cacheable(value = "user-list-responses", key = "'page:' + #page + ':' + #size")
public Result<UserListResponse> getUsers(
    @RequestParam(defaultValue = "0") Integer page,
    @RequestParam(defaultValue = "10") Integer size)
```

---

## 🔧 CacheConfig 配置

已配置的缓存区域保持不变，但用途调整为缓存 Response 对象：

```java
// Category缓存配置（缓存Response对象）
"category-responses", defaultConfig.entryTtl(Duration.ofMinutes(15)),
"category-list-responses", defaultConfig.entryTtl(Duration.ofMinutes(5)),
"category-page-responses", defaultConfig.entryTtl(Duration.ofMinutes(5)),

// Tag缓存配置
"tag-responses", defaultConfig.entryTtl(Duration.ofMinutes(15)),
"tag-list-responses", defaultConfig.entryTtl(Duration.ofMinutes(5)),
"tag-popular-responses", defaultConfig.entryTtl(Duration.ofMinutes(10)),

// Comment缓存配置
"comment-list-responses", defaultConfig.entryTtl(Duration.ofMinutes(5)),
"comment-top-responses", defaultConfig.entryTtl(Duration.ofMinutes(5)),

// User缓存配置
"user-responses", defaultConfig.entryTtl(Duration.ofMinutes(30)),
"user-list-responses", defaultConfig.entryTtl(Duration.ofMinutes(10)),
```

---

## ⚡ 缓存失效策略

命令操作时使用`@CacheEvict`清除相关缓存：

```java
@PostMapping
@CacheEvict(value = {"category-responses", "category-list-responses", "category-page-responses"}, allEntries = true)
public Result<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
    // ...
}
```

---

## 📝 实施建议

### 阶段 1：核心端点（立即实施）

优先为以下高频访问端点添加缓存：

1. ✅ 查询详情端点（GET /{id}）
2. ✅ 列表查询端点（GET /）
3. ✅ 分页查询端点（GET /page）

### 阶段 2：次要端点（按需实施）

根据实际访问量决定是否添加缓存：

- 搜索端点
- 统计端点
- 热门/最近端点

---

## 🎯 架构优势

### 符合 DDD 原则

**分层清晰**：

- **领域层**：纯粹的业务逻辑，无技术依赖
- **应用层**：业务流程编排，无技术依赖
- **表现层**：技术实现（缓存），依赖领域层

### 灵活性

- ✅ 可以针对不同端点使用不同缓存策略
- ✅ 可以缓存不同格式的 Response
- ✅ 便于 A/B 测试和性能调优

---

## 📊 预期性能

| 操作     | 无缓存 | Controller 层缓存 | 提升     |
| -------- | ------ | ----------------- | -------- |
| 详情查询 | 20ms   | 1-2ms             | 10-20 倍 |
| 列表查询 | 100ms  | 5-10ms            | 10-20 倍 |
| 分页查询 | 80ms   | 3-5ms             | 15-25 倍 |

---

**文档版本**: v1.0  
**创建日期**: 2025-10-01  
**状态**: 待实施

**相关文档**:

- [CacheConfig.java](../../src/main/java/com/cleveronion/blog/infrastructure/common/config/CacheConfig.java)
- [CQRS 实施指南](./design/cqrs-implementation-guide.md)
