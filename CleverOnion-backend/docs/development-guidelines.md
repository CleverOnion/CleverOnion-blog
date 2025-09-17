# CleverOnion 博客系统开发规范

## 1. 项目架构与分包规范

### 1.1 DDD 分层架构

本项目采用 DDD（领域驱动设计）分层与限界上下文相结合的架构模式，确保高内聚、低耦合。

```
├── com.cleveronion.blog          // 根包
│   ├── presentation             // 表现层
│   │   ├── api                  // API 接口
│   │   │   ├── controller       // 控制器
│   │   │   └── dto              // 数据传输对象 (DTO)
│   │   └── view                 // 服务端渲染视图
│   │
│   ├── application              // 应用层
│   │   ├── <bounded_context_1>  // 限界上下文1，如：article
│   │   │   ├── command          // 命令对象
│   │   │   ├── eventhandler     // 事件处理器
│   │   │   └── service          // 应用服务
│   │   └── <bounded_context_2>  // 限界上下文2，如：user
│   │       └── ...
│   │
│   ├── domain                   // 领域层
│   │   ├── <bounded_context_1>
│   │   │   ├── aggregate        // 聚合根
│   │   │   ├── entity           // 实体
│   │   │   ├── valueobject      // 值对象
│   │   │   ├── event            // 领域事件
│   │   │   └── repository       // 仓储接口
│   │   └── <bounded_context_2>
│   │       └── ...
│   │
│   ├── infrastructure           // 基础设施层
│   │   ├── <bounded_context_1>
│   │   │   ├── persistence      // 数据持久化
│   │   │   │   ├── po           // 持久化对象 (PO)
│   │   │   │   └── repository   // 仓储实现
│   │   │   └── client           // 外部服务客户端
│   │   └── <bounded_context_2>
│   │       └── ...
│   │
│   └── common                   // 通用/共享模块
│       ├── exception            // 统一异常
│       ├── security             // 安全配置
│       └── util                 // 通用工具类
```

### 1.2 分层依赖原则

**核心原则**: 代码只能依赖下方层级，领域层必须保持纯净，不依赖任何外部框架或技术。

**依赖关系**:
- `presentation` 层：只依赖 `application` 层
- `application` 层：依赖 `domain` 和 `infrastructure` 层
- `domain` 层：不依赖任何外部层（保持纯净）
- `infrastructure` 层：依赖 `domain` 层

## 2. 代码风格与命名规范

### 2.1 命名规范

#### 2.1.1 基本命名规则

- **类名、接口名**: 使用大驼峰命名法（PascalCase）
  ```java
  public class ArticleAggregate { }
  public interface ArticleRepository { }
  ```

- **方法名、变量名**: 使用小驼峰命名法（camelCase）
  ```java
  public ArticleAggregate createDraft(ArticleContent content) { }
  private Long currentUserId;
  ```

- **常量**: 使用全大写加下划线
  ```java
  public static final String DEFAULT_CATEGORY = "DEFAULT";
  private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
  ```

- **包名**: 使用全小写，单词间用点分隔
  ```java
  package com.cleveronion.blog.domain.article.aggregate;
  ```

#### 2.1.2 业务命名规范

- **聚合根**: 以 `Aggregate` 结尾
  ```java
  public class ArticleAggregate extends AggregateRoot<ArticleId> { }
  ```

- **值对象**: 以具体业务概念命名
  ```java
  public class ArticleContent { }
  public class ArticleId { }
  ```

- **应用服务**: 以 `ApplicationService` 结尾
  ```java
  public class ArticleApplicationService { }
  ```

- **仓储接口**: 以 `Repository` 结尾
  ```java
  public interface ArticleRepository { }
  ```

- **仓储实现**: 以 `RepositoryImpl` 结尾
  ```java
  public class ArticleRepositoryImpl implements ArticleRepository { }
  ```

- **控制器**: 以 `Controller` 结尾
  ```java
  public class ArticleController { }
  ```

- **DTO**: 根据用途命名
  ```java
  public class CreateArticleRequest { }
  public class ArticleResponse { }
  public class ArticleListResponse { }
  ```

### 2.2 代码格式规范

#### 2.2.1 缩进和空格

- 使用 4 个空格进行缩进，不使用 Tab
- 操作符前后加空格
- 逗号后加空格
- 方法参数列表中的逗号后加空格

```java
// 正确示例
public ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
    if (content == null || categoryId == null || authorId == null) {
        throw new IllegalArgumentException("参数不能为空");
    }
    return new ArticleAggregate(content, categoryId, authorId);
}
```

#### 2.2.2 大括号规范

- 左大括号不换行
- 右大括号单独一行
- if、for、while 等语句必须使用大括号

```java
// 正确示例
if (article.getStatus() == ArticleStatus.DRAFT) {
    article.publish();
} else {
    throw new IllegalStateException("只有草稿状态的文章才能发布");
}
```

### 2.3 注释规范

#### 2.3.1 类和接口注释

所有类、接口必须有 Javadoc 注释，说明其功能、作者、版本等信息。

```java
/**
 * 文章聚合根
 * 负责文章的生命周期管理，包括创建、发布、归档等操作
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class ArticleAggregate extends AggregateRoot<ArticleId> {
    // 实现代码
}
```

#### 2.3.2 方法注释

复杂方法必须有 Javadoc 注释，说明功能、参数、返回值和异常。

```java
/**
 * 创建文章草稿
 * 
 * @param content 文章内容，不能为空
 * @param categoryId 分类ID，不能为空
 * @param authorId 作者ID，不能为空
 * @return 创建的文章聚合根
 * @throws IllegalArgumentException 当参数为空时抛出
 */
public static ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
    // 实现代码
}
```

#### 2.3.3 行内注释

- 使用 `//` 进行行内注释
- 注释应该解释"为什么"而不是"是什么"
- 复杂业务逻辑必须添加注释

```java
// 检查文章状态，只有草稿状态的文章才能发布
if (this.status != ArticleStatus.DRAFT) {
    throw new IllegalStateException("只有草稿状态的文章才能发布");
}
```

## 3. DDD 核心规范

### 3.1 领域模型纯粹性

#### 3.1.1 领域层隔离

领域层的代码中，严禁出现 Spring、JPA、HTTP 等技术框架的引用。

```java
// ❌ 错误示例 - 领域层不应该依赖技术框架
@Entity
@Table(name = "articles")
public class ArticleAggregate {
    @Id
    @GeneratedValue
    private Long id;
}

// ✅ 正确示例 - 纯粹的领域模型
public class ArticleAggregate extends AggregateRoot<ArticleId> {
    private ArticleId id;
    private ArticleContent content;
    private ArticleStatus status;
}
```

#### 3.1.2 业务逻辑封装

所有业务逻辑都应该封装在领域模型中，而不是在应用服务或控制器中。

```java
// ✅ 正确示例 - 业务逻辑在聚合根中
public class ArticleAggregate extends AggregateRoot<ArticleId> {
    
    public void publish() {
        if (this.status != ArticleStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的文章才能发布");
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        
        // 发布领域事件
        this.addDomainEvent(new ArticlePublishedEvent(this.id, this.authorId));
    }
}
```

### 3.2 聚合设计规范

#### 3.2.1 聚合根作为唯一入口

每个聚合都有一个聚合根作为唯一入口，所有对聚合内部实体和值对象的操作都必须通过聚合根。

```java
// ✅ 正确示例 - 通过聚合根操作
public class ArticleAggregate extends AggregateRoot<ArticleId> {
    
    public void addTag(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        if (this.tagIds.contains(tagId)) {
            throw new IllegalArgumentException("标签已存在");
        }
        this.tagIds.add(tagId);
    }
}
```

#### 3.2.2 聚合边界控制

聚合应该保持较小的边界，避免过大的聚合影响性能和一致性。

```java
// ✅ 正确示例 - 合理的聚合边界
public class ArticleAggregate extends AggregateRoot<ArticleId> {
    private ArticleId id;
    private ArticleContent content;
    private AuthorId authorId;
    private CategoryId categoryId;
    private Set<TagId> tagIds;  // 只保存ID引用，不包含完整的Tag对象
    private ArticleStatus status;
}
```

### 3.3 值对象规范

#### 3.3.1 不可变性

值对象必须是不可变的，所有属性都应该是 final 的。

```java
// ✅ 正确示例 - 不可变值对象
public class ArticleContent {
    private final String title;
    private final String content;
    private final String summary;
    
    public ArticleContent(String title, String content, String summary) {
        this.title = validateTitle(title);
        this.content = validateContent(content);
        this.summary = summary;
    }
    
    // 只提供 getter 方法，不提供 setter 方法
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSummary() { return summary; }
}
```

#### 3.3.2 相等性判断

值对象的相等性判断基于所有属性值。

```java
public class ArticleId {
    private final Long value;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArticleId articleId = (ArticleId) obj;
        return Objects.equals(value, articleId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

### 3.4 仓储模式规范

#### 3.4.1 仓储接口设计

仓储接口应该使用领域语言，方法名应该体现业务意图。

```java
// ✅ 正确示例 - 使用领域语言的仓储接口
public interface ArticleRepository {
    
    Optional<ArticleAggregate> findById(ArticleId articleId);
    
    List<ArticleAggregate> findPublishedArticles();
    
    List<ArticleAggregate> findByAuthor(AuthorId authorId);
    
    List<ArticleAggregate> findByCategory(CategoryId categoryId);
    
    void save(ArticleAggregate article);
    
    void remove(ArticleId articleId);
}
```

#### 3.4.2 仓储实现

仓储实现负责领域模型和持久化对象之间的转换。

```java
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    
    private final ArticleJpaRepository jpaRepository;
    
    @Override
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        return jpaRepository.findById(articleId.getValue())
            .map(this::toDomainModel);
    }
    
    @Override
    public void save(ArticleAggregate article) {
        ArticlePo po = toPersistenceObject(article);
        jpaRepository.save(po);
    }
    
    private ArticleAggregate toDomainModel(ArticlePo po) {
        // PO 转换为领域模型
    }
    
    private ArticlePo toPersistenceObject(ArticleAggregate aggregate) {
        // 领域模型转换为 PO
    }
}
```

## 4. 应用层规范

### 4.1 应用服务职责

#### 4.1.1 业务流程编排

应用服务负责编排业务流程，但不包含核心业务逻辑。

```java
@Service
@Transactional
public class ArticleApplicationService {
    
    public ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
        // 1. 参数验证
        validateCreateDraftParameters(content, categoryId, authorId);
        
        // 2. 业务规则检查
        validateCategoryExists(categoryId);
        
        // 3. 创建聚合根（业务逻辑在聚合根中）
        ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);
        
        // 4. 持久化
        ArticleAggregate savedArticle = articleRepository.save(article);
        
        // 5. 发布事件
        domainEventPublisher.publish(article.getDomainEvents());
        
        return savedArticle;
    }
}
```

#### 4.1.2 事务管理

事务应该在应用服务层进行管理，使用 `@Transactional` 注解。

```java
@Service
@Transactional  // 类级别的事务注解
public class ArticleApplicationService {
    
    @Transactional(readOnly = true)  // 只读事务
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        return articleRepository.findById(articleId);
    }
    
    @Transactional  // 写事务
    public ArticleAggregate publishArticle(ArticleId articleId, AuthorId authorId) {
        // 事务逻辑
    }
}
```

### 4.2 命令查询分离

#### 4.2.1 命令对象

使用命令对象封装操作参数，保证不可变性。

```java
public class CreateArticleCommand {
    private final String title;
    private final String content;
    private final String summary;
    private final Long categoryId;
    private final Long authorId;
    
    public CreateArticleCommand(String title, String content, String summary, Long categoryId, Long authorId) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.categoryId = categoryId;
        this.authorId = authorId;
    }
    
    // 只提供 getter 方法
}
```

#### 4.2.2 查询对象

使用查询对象封装查询条件。

```java
public class ArticleQuery {
    private final ArticleStatus status;
    private final CategoryId categoryId;
    private final TagId tagId;
    private final int page;
    private final int size;
    
    // 构造函数和 getter 方法
}
```

## 5. 表现层规范

### 5.1 控制器设计

#### 5.1.1 职责单一

控制器只处理 HTTP 请求和响应，不包含任何业务逻辑。

```java
@RestController
@RequestMapping("/articles")
public class ArticleController {
    
    private final ArticleApplicationService articleApplicationService;
    
    @PostMapping
    public Result<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        // 1. 参数转换
        ArticleContent content = new ArticleContent(
            request.getTitle(), 
            request.getContent(), 
            request.getSummary()
        );
        CategoryId categoryId = new CategoryId(request.getCategoryId());
        AuthorId authorId = new AuthorId(getCurrentUserId());
        
        // 2. 调用应用服务
        ArticleAggregate article = articleApplicationService.createDraft(content, categoryId, authorId);
        
        // 3. 构建响应
        return Result.success(new ArticleResponse(article));
    }
}
```

#### 5.1.2 统一异常处理

使用全局异常处理器统一处理异常，避免在控制器中重复异常处理代码。

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(Result.badRequest(e.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleResourceNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.notFound(e.getMessage()));
    }
}
```

### 5.2 DTO 设计规范

#### 5.2.1 请求 DTO

请求 DTO 应该包含验证注解，确保数据的有效性。

```java
public class CreateArticleRequest {
    
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    @Size(max = 500, message = "文章摘要长度不能超过500个字符")
    private String summary;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    // getter 和 setter 方法
}
```

#### 5.2.2 响应 DTO

响应 DTO 应该只包含前端需要的数据，避免暴露内部实现细节。

```java
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String status;
    private CategoryResponse category;
    private UserResponse author;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    
    public ArticleResponse(ArticleAggregate article) {
        this.id = article.getId().getValue();
        this.title = article.getContent().getTitle();
        this.content = article.getContent().getContent();
        this.summary = article.getContent().getSummary();
        this.status = article.getStatus().name();
        this.createdAt = article.getCreatedAt();
        this.updatedAt = article.getUpdatedAt();
        this.publishedAt = article.getPublishedAt();
    }
}
```

## 6. API 接口设计规范

### 6.1 RESTful 设计原则

#### 6.1.1 URL 设计

- 使用名词复数形式表示资源集合
- 使用层级结构表示资源关系
- 避免在 URL 中使用动词

```java
// ✅ 正确示例
GET    /articles              // 获取文章列表
GET    /articles/{id}         // 获取特定文章
POST   /articles              // 创建文章
PUT    /articles/{id}         // 更新文章
DELETE /articles/{id}         // 删除文章
POST   /articles/{id}/publish // 发布文章（状态变更）
GET    /articles/{id}/comments // 获取文章评论

// ❌ 错误示例
GET /getArticles
POST /createArticle
POST /publishArticle
```

#### 6.1.2 HTTP 方法使用

- `GET`: 查询资源，幂等操作
- `POST`: 创建资源或非幂等操作
- `PUT`: 完整更新资源，幂等操作
- `PATCH`: 部分更新资源
- `DELETE`: 删除资源，幂等操作

### 6.2 统一响应格式

#### 6.2.1 成功响应

```java
public class Result<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> Result<T> success(T data) {
        return new Result<>(true, "操作成功", data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, message, data);
    }
}
```

#### 6.2.2 错误响应

```java
public class Result<T> {
    
    public static <T> Result<T> error(String message) {
        return new Result<>(false, message, null);
    }
    
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(false, message, null);
    }
    
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(false, message, null);
    }
    
    public static <T> Result<T> notFound(String message) {
        return new Result<>(false, message, null);
    }
}
```

### 6.3 分页查询规范

#### 6.3.1 分页参数

```java
public class PageRequest {
    @Min(value = 0, message = "页码不能小于0")
    private int page = 0;
    
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private int size = 10;
    
    private String sort;
    private String direction = "DESC";
}
```

#### 6.3.2 分页响应

```java
public class PageResult<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}
```

## 7. 数据库与持久化规范

### 7.1 持久化对象设计

#### 7.1.1 PO 对象规范

持久化对象（PO）作为数据在数据库和基础设施层之间的桥梁，不应包含任何业务逻辑。

```java
@Entity
@Table(name = "articles")
public class ArticlePo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "summary", length = 500)
    private String summary;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ArticleStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    // 构造函数、getter 和 setter 方法
}
```

#### 7.1.2 关联关系处理

```java
@Entity
@Table(name = "article_tags")
public class ArticleTagPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "article_id", nullable = false)
    private Long articleId;
    
    @Column(name = "tag_id", nullable = false)
    private Long tagId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
```

### 7.2 数据库设计规范

#### 7.2.1 表命名规范

- 表名使用复数形式
- 使用下划线分隔单词
- 关联表使用两个表名的组合

```sql
-- 主表
CREATE TABLE articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    -- 其他字段
);

-- 关联表
CREATE TABLE article_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    -- 其他字段
);
```

#### 7.2.2 索引设计

```sql
-- 单列索引
CREATE INDEX idx_articles_author_id ON articles(author_id);
CREATE INDEX idx_articles_category_id ON articles(category_id);
CREATE INDEX idx_articles_status ON articles(status);

-- 复合索引
CREATE INDEX idx_articles_status_published_at ON articles(status, published_at DESC);
CREATE INDEX idx_articles_author_status ON articles(author_id, status);

-- 唯一索引
CREATE UNIQUE INDEX uk_categories_name ON categories(name);
CREATE UNIQUE INDEX uk_tags_name ON tags(name);
```

## 8. 异常处理规范

### 8.1 异常分类

#### 8.1.1 业务异常

```java
public class BusinessException extends RuntimeException {
    private final String errorCode;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

// 具体业务异常
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String id) {
        super("RESOURCE_NOT_FOUND", String.format("%s with id %s not found", resource, id));
    }
}

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resource, String field, String value) {
        super("DUPLICATE_RESOURCE", String.format("%s with %s '%s' already exists", resource, field, value));
    }
}
```

#### 8.1.2 系统异常

```java
public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }
    
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 8.2 全局异常处理

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(Result.error(e.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.notFound(e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("参数验证失败: {}", message);
        return ResponseEntity.badRequest()
            .body(Result.badRequest(message));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGenericException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.error("系统内部错误，请稍后重试"));
    }
}
```

## 9. 日志规范

### 9.1 日志级别使用

#### 9.1.1 日志级别定义

- `ERROR`: 致命错误，需要立即处理
- `WARN`: 警告信息，可能的问题
- `INFO`: 关键业务流程信息
- `DEBUG`: 调试信息，开发阶段使用
- `TRACE`: 详细的调试信息

#### 9.1.2 日志使用示例

```java
@Service
public class ArticleApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleApplicationService.class);
    
    public ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
        logger.info("开始创建文章草稿: authorId={}, categoryId={}, title={}", 
            authorId.getValue(), categoryId.getValue(), content.getTitle());
        
        try {
            // 业务逻辑
            ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);
            ArticleAggregate savedArticle = articleRepository.save(article);
            
            logger.info("文章草稿创建成功: articleId={}, title={}", 
                savedArticle.getId().getValue(), savedArticle.getContent().getTitle());
                
            return savedArticle;
        } catch (Exception e) {
            logger.error("创建文章草稿失败: authorId={}, categoryId={}, error={}", 
                authorId.getValue(), categoryId.getValue(), e.getMessage(), e);
            throw e;
        }
    }
}
```

### 9.2 结构化日志

#### 9.2.1 日志格式

使用结构化的日志格式，便于日志分析和监控。

```java
// 使用 MDC 添加上下文信息
public class LoggingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestId = UUID.randomUUID().toString();
        
        MDC.put("requestId", requestId);
        MDC.put("method", httpRequest.getMethod());
        MDC.put("uri", httpRequest.getRequestURI());
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

## 10. 测试规范

### 10.1 单元测试

#### 10.1.1 领域模型测试

```java
class ArticleAggregateTest {
    
    @Test
    void should_create_draft_successfully() {
        // Given
        ArticleContent content = new ArticleContent("测试标题", "测试内容", "测试摘要");
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(1L);
        
        // When
        ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);
        
        // Then
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(article.getContent().getTitle()).isEqualTo("测试标题");
        assertThat(article.getCategoryId()).isEqualTo(categoryId);
        assertThat(article.getAuthorId()).isEqualTo(authorId);
    }
    
    @Test
    void should_throw_exception_when_publish_non_draft_article() {
        // Given
        ArticleAggregate article = createPublishedArticle();
        
        // When & Then
        assertThatThrownBy(() -> article.publish())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("只有草稿状态的文章才能发布");
    }
}
```

#### 10.1.2 应用服务测试

```java
@ExtendWith(MockitoExtension.class)
class ArticleApplicationServiceTest {
    
    @Mock
    private ArticleRepository articleRepository;
    
    @Mock
    private CategoryApplicationService categoryApplicationService;
    
    @InjectMocks
    private ArticleApplicationService articleApplicationService;
    
    @Test
    void should_create_draft_successfully() {
        // Given
        ArticleContent content = new ArticleContent("测试标题", "测试内容", "测试摘要");
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(1L);
        
        CategoryAggregate category = mock(CategoryAggregate.class);
        when(categoryApplicationService.findById(categoryId)).thenReturn(Optional.of(category));
        
        ArticleAggregate savedArticle = mock(ArticleAggregate.class);
        when(articleRepository.save(any(ArticleAggregate.class))).thenReturn(savedArticle);
        
        // When
        ArticleAggregate result = articleApplicationService.createDraft(content, categoryId, authorId);
        
        // Then
        assertThat(result).isEqualTo(savedArticle);
        verify(articleRepository).save(any(ArticleAggregate.class));
    }
}
```

### 10.2 集成测试

#### 10.2.1 仓储测试

```java
@DataJpaTest
class ArticleRepositoryImplTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ArticleJpaRepository jpaRepository;
    
    private ArticleRepositoryImpl articleRepository;
    
    @BeforeEach
    void setUp() {
        articleRepository = new ArticleRepositoryImpl(jpaRepository);
    }
    
    @Test
    void should_save_and_find_article() {
        // Given
        ArticleAggregate article = createTestArticle();
        
        // When
        articleRepository.save(article);
        Optional<ArticleAggregate> found = articleRepository.findById(article.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getContent().getTitle()).isEqualTo(article.getContent().getTitle());
    }
}
```

#### 10.2.2 API 测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void should_create_article_successfully() {
        // Given
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("测试标题");
        request.setContent("测试内容");
        request.setCategoryId(1L);
        
        // When
        ResponseEntity<Result> response = restTemplate.postForEntity(
            "/articles", request, Result.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }
}
```

## 11. 安全规范

### 11.1 认证授权

#### 11.1.1 Sa-Token 使用

```java
@RestController
public class ArticleController {
    
    @PostMapping("/articles")
    @SaCheckLogin  // 检查登录状态
    public Result<ArticleResponse> createArticle(@RequestBody CreateArticleRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 业务逻辑
    }
    
    @DeleteMapping("/articles/{id}")
    @SaCheckPermission("article:delete")  // 检查权限
    public Result<Void> deleteArticle(@PathVariable Long id) {
        // 业务逻辑
    }
}
```

#### 11.1.2 权限检查

```java
@Service
public class ArticleApplicationService {
    
    public void deleteArticle(ArticleId articleId, AuthorId currentUserId) {
        ArticleAggregate article = articleRepository.findById(articleId)
            .orElseThrow(() -> new ResourceNotFoundException("Article", articleId.getValue().toString()));
        
        // 权限检查：只有作者或管理员可以删除文章
        if (!article.getAuthorId().equals(currentUserId) && !isAdmin(currentUserId)) {
            throw new AccessDeniedException("没有权限删除此文章");
        }
        
        articleRepository.remove(articleId);
    }
}
```

### 11.2 输入验证

#### 11.2.1 参数验证

```java
public class CreateArticleRequest {
    
    @NotBlank(message = "文章标题不能为空")
    @Size(min = 1, max = 200, message = "文章标题长度必须在1-200个字符之间")
    private String title;
    
    @NotBlank(message = "文章内容不能为空")
    @Size(min = 1, max = 100000, message = "文章内容长度不能超过100000个字符")
    private String content;
    
    @Size(max = 500, message = "文章摘要长度不能超过500个字符")
    private String summary;
    
    @NotNull(message = "分类ID不能为空")
    @Positive(message = "分类ID必须为正数")
    private Long categoryId;
}
```

#### 11.2.2 自定义验证器

```java
@Component
public class ArticleValidator {
    
    public void validateCreateDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
        if (content == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        
        validateTitle(content.getTitle());
        validateContent(content.getContent());
    }
    
    private void validateTitle(String title) {
        if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("文章标题长度不能超过200个字符");
        }
    }
}
```

## 12. 配置管理规范

### 12.1 多环境配置

#### 12.1.1 配置文件结构

```
src/main/resources/
├── application.yml              # 通用配置
├── application-dev.yml          # 开发环境配置
├── application-test.yml         # 测试环境配置
└── application-prod.yml         # 生产环境配置
```

#### 12.1.2 配置示例

```yaml
# application.yml - 通用配置
spring:
  application:
    name: cleveronion-blog
  profiles:
    active: dev
  
server:
  port: 8080

logging:
  level:
    com.cleveronion.blog: INFO
    
---
# application-dev.yml - 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cleveronion_blog_dev
    username: dev_user
    password: dev_password
    
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
      
logging:
  level:
    com.cleveronion.blog: DEBUG
    org.hibernate.SQL: DEBUG
```

### 12.2 敏感信息管理

#### 12.2.1 环境变量

```yaml
# 生产环境配置
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    
github:
  oauth2:
    client-id: ${GITHUB_CLIENT_ID}
    client-secret: ${GITHUB_CLIENT_SECRET}
```

#### 12.2.2 配置类

```java
@ConfigurationProperties(prefix = "github.oauth2")
@Data
public class GitHubOAuth2Properties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authorizeUrl;
    private String accessTokenUrl;
    private String userInfoUrl;
}
```

## 13. 版本控制规范

### 13.1 Git 工作流

#### 13.1.1 分支模型

- `main`/`master`: 生产环境分支，保持稳定
- `develop`: 开发主分支，集成新功能
- `feature/*`: 功能开发分支，从 `develop` 创建
- `release/*`: 发布分支，用于准备发布版本
- `hotfix/*`: 紧急修复分支，从 `main`/`master` 创建

#### 13.1.2 提交信息规范

使用 Conventional Commits 规范：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

类型说明：
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

示例：
```
feat(article): 添加文章发布功能

- 实现文章状态从草稿到发布的转换
- 添加发布时间记录
- 发布时触发领域事件

Closes #123
```

### 13.2 代码审查

#### 13.2.1 Pull Request 规范

- PR 标题应该清晰描述变更内容
- PR 描述应该包含变更原因和影响范围
- 必须通过所有自动化测试
- 至少需要一个同事的代码审查

#### 13.2.2 审查清单

- [ ] 代码符合项目规范
- [ ] 业务逻辑正确
- [ ] 异常处理完善
- [ ] 测试覆盖充分
- [ ] 性能影响可接受
- [ ] 安全性考虑周全

## 14. 性能优化规范

### 14.1 数据库优化

#### 14.1.1 查询优化

```java
// ✅ 使用 EntityGraph 避免 N+1 查询
@EntityGraph(attributePaths = {"category", "author", "tags"})
@Query("SELECT a FROM ArticlePo a WHERE a.status = :status")
List<ArticlePo> findByStatusWithEntities(@Param("status") ArticleStatus status);

// ✅ 批量查询
@Query("SELECT a FROM ArticlePo a WHERE a.id IN :ids")
List<ArticlePo> findByIds(@Param("ids") List<Long> ids);
```

#### 14.1.2 分页优化

```java
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    
    public PageResult<ArticleAggregate> findPublishedArticles(PageRequest pageRequest) {
        Pageable pageable = PageRequest.of(
            pageRequest.getPage(), 
            pageRequest.getSize(),
            Sort.by(Sort.Direction.DESC, "publishedAt")
        );
        
        Page<ArticlePo> page = jpaRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);
        
        List<ArticleAggregate> articles = page.getContent().stream()
            .map(this::toDomainModel)
            .collect(Collectors.toList());
            
        return new PageResult<>(
            articles,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
```

### 14.2 缓存策略

#### 14.2.1 应用层缓存

```java
@Service
public class ArticleQueryService {
    
    @Cacheable(value = "articles", key = "#articleId")
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        return articleRepository.findById(articleId);
    }
    
    @Cacheable(value = "article-lists", key = "#pageRequest.hashCode()")
    public PageResult<ArticleAggregate> findPublishedArticles(PageRequest pageRequest) {
        return articleRepository.findPublishedArticles(pageRequest);
    }
    
    @CacheEvict(value = "articles", key = "#articleId")
    public void evictArticleCache(ArticleId articleId) {
        // 缓存失效
    }
}
```

## 15. 总结

本开发规范涵盖了 CleverOnion 博客系统开发的各个方面，从架构设计到具体的代码实现，从数据库设计到性能优化。遵循这些规范可以确保：

### 15.1 代码质量

- 统一的代码风格和命名规范
- 清晰的架构分层和职责划分
- 完善的异常处理和日志记录
- 充分的测试覆盖

### 15.2 系统可维护性

- DDD 架构确保业务逻辑的清晰表达
- 模块化设计支持功能的独立开发和部署
- 统一的开发规范降低维护成本

### 15.3 系统可扩展性

- 事件驱动架构支持系统的水平扩展
- 配置化管理支持多环境部署
- 缓存策略提升系统性能

### 15.4 开发效率

- 标准化的开发流程
- 完善的工具链支持
- 清晰的文档和规范

遵循这些规范，团队可以高效地开发和维护 CleverOnion 博客系统，确保系统的长期稳定运行和持续演进。