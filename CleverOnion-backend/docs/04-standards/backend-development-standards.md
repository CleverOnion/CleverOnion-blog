# CleverOnion 后端项目开发规范总结

## 文档说明

本文档基于当前项目的实际代码和已有规范文档，总结了 CleverOnion 博客系统后端项目的开发规范。本规范旨在确保代码质量、可维护性和团队协作效率。

**文档版本**: 1.0  
**最后更新**: 2025-09-30  
**项目技术栈**: Spring Boot 3.5.4 + Java 21 + PostgreSQL + Redis

---

## 1. 架构设计规范

### 1.1 DDD 分层架构

项目采用领域驱动设计（DDD）的四层架构，确保关注点分离和高内聚低耦合：

```
表现层 (Presentation)  → 处理HTTP请求响应
       ↓
应用层 (Application)   → 业务流程编排、事务管理
       ↓
领域层 (Domain)        → 核心业务逻辑（纯净）
       ↓
基础设施层 (Infrastructure) → 数据持久化、外部服务
```

**依赖规则**：

- 表现层 → 只依赖应用层
- 应用层 → 依赖领域层和基础设施层
- 领域层 → **不依赖任何其他层**（保持纯净）
- 基础设施层 → 依赖领域层

### 1.2 限界上下文划分

按业务领域划分限界上下文，每个上下文独立管理：

- **文章上下文** (Article Context): 文章、分类、标签管理
- **用户上下文** (User Context): 用户信息管理
- **评论上下文** (Comment Context): 评论功能
- **认证上下文** (Auth Context): 用户认证授权
- **管理上下文** (Admin Context): 系统管理功能

---

## 2. 命名规范

### 2.1 基本命名规则

| 类型          | 命名方式            | 示例                                    |
| ------------- | ------------------- | --------------------------------------- |
| 类名/接口名   | 大驼峰 (PascalCase) | `ArticleAggregate`, `UserRepository`    |
| 方法名/变量名 | 小驼峰 (camelCase)  | `createDraft()`, `currentUserId`        |
| 常量          | 全大写+下划线       | `DEFAULT_PAGE_SIZE`, `MAX_TITLE_LENGTH` |
| 包名          | 全小写+点分隔       | `com.cleveronion.blog.domain.article`   |

### 2.2 业务对象命名规范

| 对象类型   | 命名规则              | 示例                                |
| ---------- | --------------------- | ----------------------------------- |
| 聚合根     | `*Aggregate`          | `ArticleAggregate`, `UserAggregate` |
| 值对象     | 业务概念名            | `ArticleContent`, `ArticleId`       |
| 应用服务   | `*ApplicationService` | `ArticleApplicationService`         |
| 仓储接口   | `*Repository`         | `ArticleRepository`                 |
| 仓储实现   | `*RepositoryImpl`     | `ArticleRepositoryImpl`             |
| 控制器     | `*Controller`         | `ArticleController`                 |
| 请求 DTO   | `*Request`            | `CreateArticleRequest`              |
| 响应 DTO   | `*Response`           | `ArticleResponse`                   |
| 持久化对象 | `*PO` 或 `*Po`        | `ArticlePO`, `ArticleTagPO`         |

---

## 3. 代码风格规范

### 3.1 格式规范

- **缩进**: 4 个空格（不使用 Tab）
- **大括号**: 左大括号不换行，右大括号单独一行
- **空格**: 运算符前后、逗号后必须加空格
- **行长度**: 建议不超过 120 字符

### 3.2 注释规范

#### 类注释（必须）

```java
/**
 * 文章聚合根
 * 负责文章的生命周期管理，包括创建、发布、归档等操作
 *
 * @author CleverOnion
 * @since 1.0.0
 */
public class ArticleAggregate extends AggregateRoot {
    // ...
}
```

#### 方法注释（复杂方法必须）

```java
/**
 * 发布文章
 * 只有草稿状态的文章才能发布
 *
 * @param articleId 文章ID
 * @param authorId 作者ID（用于权限验证）
 * @return 发布后的文章聚合
 * @throws IllegalArgumentException 当参数为空时抛出
 * @throws IllegalStateException 当文章状态不是草稿时抛出
 */
public ArticleAggregate publishArticle(ArticleId articleId, AuthorId authorId) {
    // ...
}
```

#### 行内注释

- 解释"为什么"而不是"是什么"
- 复杂业务逻辑必须添加注释
- 使用 `//` 进行单行注释

---

## 4. DDD 核心规范

### 4.1 领域层纯净性原则

**核心原则**: 领域层代码必须保持纯净，不能依赖任何技术框架。

❌ **错误示例**：

```java
@Entity  // JPA注解，不应出现在领域层
@Table(name = "articles")
public class ArticleAggregate {
    @Id
    @GeneratedValue
    private Long id;
}
```

✅ **正确示例**：

```java
public class ArticleAggregate extends AggregateRoot {
    private ArticleId id;
    private ArticleContent content;
    private ArticleStatus status;

    public void publish() {
        if (!status.isDraft()) {
            throw new IllegalStateException("只有草稿状态的文章才能发布");
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
}
```

### 4.2 聚合设计原则

#### 聚合根作为唯一入口

所有对聚合内部的操作必须通过聚合根：

```java
public class ArticleAggregate extends AggregateRoot {
    private Set<TagId> tagIds;

    // 通过聚合根方法操作内部集合
    public void addTag(TagId tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        this.tagIds.add(tagId);
    }
}
```

#### 聚合边界控制

- 聚合应保持较小边界
- 跨聚合引用使用 ID 而非对象
- 避免过深的对象图

```java
public class ArticleAggregate {
    private CategoryId categoryId;     // ✅ 使用ID引用
    private Set<TagId> tagIds;         // ✅ 使用ID集合
    // 不要: private Category category; // ❌ 避免直接引用其他聚合
}
```

### 4.3 值对象设计规范

#### 不可变性（强制）

```java
public class ArticleContent {
    private final String title;      // final修饰
    private final String content;
    private final String summary;

    public ArticleContent(String title, String content, String summary) {
        // 构造时验证和初始化
        this.title = validateTitle(title);
        this.content = validateContent(content);
        this.summary = summary;
    }

    // 只提供getter，不提供setter
    public String getTitle() { return title; }

    // 如需修改，返回新对象
    public ArticleContent withTitle(String newTitle) {
        return new ArticleContent(newTitle, this.content, this.summary);
    }
}
```

#### 相等性判断

值对象的相等性基于所有属性值：

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ArticleId that = (ArticleId) o;
    return Objects.equals(value, that.value);
}

@Override
public int hashCode() {
    return Objects.hash(value);
}
```

### 4.4 仓储模式规范

#### 仓储接口（领域层）

使用领域语言，表达业务意图：

```java
public interface ArticleRepository {
    ArticleAggregate save(ArticleAggregate article);
    Optional<ArticleAggregate> findById(ArticleId id);
    List<ArticleAggregate> findPublishedArticles(int page, int size);
    void deleteById(ArticleId id);
}
```

#### 仓储实现（基础设施层）

负责领域模型和持久化对象的转换：

```java
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {

    @Override
    @Transactional
    public ArticleAggregate save(ArticleAggregate article) {
        // 1. 领域模型 → PO
        ArticlePO po = ArticleConverter.toArticlePO(article);
        // 2. 保存到数据库
        ArticlePO savedPO = jpaRepository.save(po);
        // 3. PO → 领域模型
        return ArticleConverter.toArticleAggregate(savedPO, article.getTagIds());
    }
}
```

---

## 5. 应用层规范

### 5.1 应用服务职责

应用服务负责：

1. **业务流程编排**（不包含核心业务逻辑）
2. **事务管理**
3. **参数验证**
4. **领域事件发布**
5. **调用领域层和基础设施层**

### 5.2 标准应用服务模式

```java
@Service
@Transactional
public class ArticleApplicationService {

    private final ArticleRepository articleRepository;
    private static final Logger logger = LoggerFactory.getLogger(ArticleApplicationService.class);

    /**
     * 创建文章草稿
     */
    public ArticleAggregate createDraft(ArticleContent content, CategoryId categoryId, AuthorId authorId) {
        // 1. 参数验证
        if (content == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }

        logger.debug("开始创建文章草稿，作者ID: {}", authorId.getValue());

        // 2. 调用领域层创建聚合（业务逻辑在领域层）
        ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);

        // 3. 持久化
        ArticleAggregate savedArticle = articleRepository.save(article);

        logger.info("成功创建文章草稿，文章ID: {}", savedArticle.getId().getValue());

        return savedArticle;
    }

    // 查询方法标记为只读事务
    @Transactional(readOnly = true)
    public Optional<ArticleAggregate> findById(ArticleId articleId) {
        return articleRepository.findById(articleId);
    }
}
```

### 5.3 事务管理规范

- **类级别**：`@Transactional` 默认为写事务
- **方法级别**：查询方法使用 `@Transactional(readOnly = true)`
- **事务边界**：应用服务层是事务边界，不在领域层或控制器层开启事务

---

## 6. 表现层规范

### 6.1 控制器设计原则

控制器职责：

1. **处理 HTTP 请求和响应**
2. **参数转换**（DTO → 领域对象）
3. **调用应用服务**
4. **构建响应**（领域对象 → DTO）

**不应包含**: 业务逻辑、事务管理、直接数据库操作

### 6.2 标准控制器模式

```java
@RestController
@RequestMapping("/articles")
@Tag(name = "文章管理")
@Validated
public class ArticleController {

    private final ArticleApplicationService articleApplicationService;
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @PostMapping
    @Operation(summary = "创建文章草稿")
    public Result<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        logger.info("接收到创建文章请求，标题: {}", request.getTitle());

        // 1. 参数转换（DTO → 领域对象）
        ArticleContent content = new ArticleContent(
            request.getTitle(),
            request.getContent(),
            request.getSummary()
        );
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        CategoryId categoryId = new CategoryId(request.getCategoryId());

        // 2. 调用应用服务
        ArticleAggregate article = articleApplicationService.createDraft(content, categoryId, authorId);

        // 3. 构建响应（领域对象 → DTO）
        ArticleResponse response = buildArticleResponse(article);

        logger.info("成功创建文章，ID: {}", article.getId().getValue());

        return Result.success(response);
    }
}
```

### 6.3 DTO 设计规范

#### 请求 DTO

必须包含验证注解：

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

    // getter和setter
}
```

#### 响应 DTO

只包含前端需要的数据，隐藏内部实现：

```java
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String status;
    private CategoryResponse category;
    private UserResponse author;
    private Set<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    // 从领域对象构造
    public ArticleResponse(ArticleAggregate article, CategoryResponse category,
                          UserResponse author, Set<TagResponse> tags) {
        this.id = article.getId().getValue();
        this.title = article.getContent().getTitle();
        // ...
    }
}
```

---

## 7. API 设计规范

### 7.1 RESTful 设计原则

#### URL 设计

- 使用名词复数形式
- 使用层级结构
- 避免动词

```
✅ 正确示例：
GET    /articles              # 获取文章列表
GET    /articles/{id}         # 获取特定文章
POST   /articles              # 创建文章
PUT    /articles/{id}         # 更新文章
DELETE /articles/{id}         # 删除文章
POST   /articles/{id}/publish # 发布文章（状态变更）
GET    /articles/{id}/comments # 获取文章评论

❌ 错误示例：
GET /getArticles
POST /createArticle
POST /publishArticle
```

#### HTTP 方法使用

| 方法   | 用途                | 幂等性 |
| ------ | ------------------- | ------ |
| GET    | 查询资源            | 是     |
| POST   | 创建资源/非幂等操作 | 否     |
| PUT    | 完整更新资源        | 是     |
| PATCH  | 部分更新资源        | 否     |
| DELETE | 删除资源            | 是     |

### 7.2 统一响应格式

所有 API 响应使用统一的 `Result<T>` 包装：

```java
{
  "code": 200,
  "message": "操作成功",
  "data": { /* 实际数据 */ },
  "timestamp": 1640995200000
}
```

**状态码规范**：

- `200` - 操作成功
- `400` - 请求参数错误
- `401` - 未授权
- `403` - 禁止访问
- `404` - 资源未找到
- `500` - 服务器内部错误

### 7.3 分页查询规范

```java
// 分页参数
@GetMapping
public Result<ArticleListResponse> getArticles(
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "10") @Min(1) Integer size) {
    // ...
}

// 分页响应
{
  "content": [ /* 数据列表 */ ],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## 8. 数据库与持久化规范

### 8.1 持久化对象（PO）规范

```java
@Entity
@Table(name = "articles")
public class ArticlePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ArticleStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 构造函数、getter和setter
}
```

### 8.2 表命名规范

- 表名使用复数形式
- 使用下划线分隔单词
- 关联表使用两个表名组合

```sql
-- 主表
CREATE TABLE articles (...);
CREATE TABLE users (...);
CREATE TABLE categories (...);

-- 关联表
CREATE TABLE article_tags (...);
CREATE TABLE user_roles (...);
```

### 8.3 索引设计规范

```sql
-- 单列索引
CREATE INDEX idx_articles_author_id ON articles(author_id);
CREATE INDEX idx_articles_status ON articles(status);

-- 复合索引（注意顺序）
CREATE INDEX idx_articles_status_published_at
    ON articles(status, published_at DESC);

-- 唯一索引
CREATE UNIQUE INDEX uk_categories_name ON categories(name);
```

---

## 9. 异常处理规范

### 9.1 异常分类

```
Exception
└── RuntimeException
    └── BusinessException (业务异常基类)
        ├── ResourceNotFoundException (资源未找到)
        ├── AccessDeniedException (权限不足)
        ├── DuplicateResourceException (资源重复)
        └── ValidationException (验证异常)
```

### 9.2 自定义异常

```java
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String id) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s with id %s not found", resource, id));
    }
}
```

### 9.3 全局异常处理

使用 `@RestControllerAdvice` 统一处理：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        logger.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(Result.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("非法参数: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(Result.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGenericException(Exception ex) {
        logger.error("系统异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.error("服务器内部错误，请稍后重试"));
    }
}
```

---

## 10. 日志规范

### 10.1 日志级别使用

| 级别  | 用途     | 示例场景                 |
| ----- | -------- | ------------------------ |
| ERROR | 致命错误 | 系统异常、数据丢失       |
| WARN  | 警告信息 | 业务异常、预期之外的情况 |
| INFO  | 关键流程 | 用户操作、重要业务节点   |
| DEBUG | 调试信息 | 开发阶段详细日志         |
| TRACE | 详细追踪 | 方法调用、变量值         |

### 10.2 日志记录规范

```java
@Service
public class ArticleApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleApplicationService.class);

    public ArticleAggregate createDraft(...) {
        // DEBUG: 开始操作
        logger.debug("开始创建文章草稿: authorId={}, categoryId={}, title={}",
            authorId.getValue(), categoryId.getValue(), content.getTitle());

        try {
            // 业务逻辑
            ArticleAggregate article = ArticleAggregate.createDraft(...);
            ArticleAggregate savedArticle = articleRepository.save(article);

            // INFO: 成功完成
            logger.info("文章草稿创建成功: articleId={}, title={}",
                savedArticle.getId().getValue(), savedArticle.getContent().getTitle());

            return savedArticle;
        } catch (Exception e) {
            // ERROR: 异常情况
            logger.error("创建文章草稿失败: authorId={}, error={}",
                authorId.getValue(), e.getMessage(), e);
            throw e;
        }
    }
}
```

### 10.3 日志格式规范

```properties
# 控制台日志格式
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# 文件日志格式
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

---

## 11. 配置管理规范

### 11.1 多环境配置

```
src/main/resources/
├── application.yml           # 通用配置
├── application-dev.yml       # 开发环境
├── application-test.yml      # 测试环境
└── application-prod.yml      # 生产环境
```

### 11.2 配置分类

**通用配置** (`application.yml`):

```yaml
spring:
  application:
    name: cleveronion-blog
  profiles:
    active: dev

sa-token:
  token-name: Authorization
  timeout: 2592000
```

**环境特定配置** (`application-dev.yml`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blog
    username: postgres
    password: ${DB_PASSWORD} # 使用环境变量

server:
  port: 8080
```

### 11.3 敏感信息处理

**原则**: 敏感信息不得硬编码在配置文件中

✅ **正确做法**：

```yaml
# 使用环境变量
spring:
  datasource:
    password: ${DB_PASSWORD}

github:
  oauth2:
    client-id: ${GITHUB_CLIENT_ID}
    client-secret: ${GITHUB_CLIENT_SECRET}
```

❌ **错误做法**：

```yaml
# 直接硬编码敏感信息
spring:
  datasource:
    password: mypassword123
```

---

## 12. 安全规范

### 12.1 认证授权

使用 Sa-Token 进行认证授权：

```java
@RestController
public class ArticleController {

    // 检查登录状态（方法级）
    @PostMapping("/articles")
    @SaCheckLogin
    public Result<ArticleResponse> createArticle(@RequestBody CreateArticleRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        // ...
    }

    // 检查权限
    @DeleteMapping("/articles/{id}")
    @SaCheckPermission("article:delete")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        // ...
    }
}
```

### 12.2 输入验证

#### 使用 Bean Validation

```java
public class CreateArticleRequest {
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度必须在1-200字符之间")
    private String title;

    @NotNull(message = "分类ID不能为空")
    @Positive(message = "分类ID必须为正数")
    private Long categoryId;
}
```

#### 业务规则验证

```java
public class ArticleAggregate {
    public void publish() {
        if (!status.isDraft()) {
            throw new IllegalStateException("只有草稿状态的文章才能发布");
        }
        // ...
    }
}
```

### 12.3 权限检查

应用层进行权限验证：

```java
public void deleteArticle(ArticleId articleId, AuthorId currentUserId) {
    ArticleAggregate article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourceNotFoundException("Article", articleId));

    // 权限检查
    if (!article.belongsToAuthor(currentUserId) && !isAdmin(currentUserId)) {
        throw new AccessDeniedException("没有权限删除此文章");
    }

    articleRepository.remove(articleId);
}
```

---

## 13. 测试规范

### 13.1 测试类型

| 测试类型   | 目标               | 工具             |
| ---------- | ------------------ | ---------------- |
| 单元测试   | 领域模型、应用服务 | JUnit 5, Mockito |
| 集成测试   | 仓储实现、API 接口 | Spring Boot Test |
| 端到端测试 | 完整业务流程       | RestAssured      |

### 13.2 单元测试规范

#### 领域模型测试

```java
class ArticleAggregateTest {

    @Test
    void should_create_draft_successfully() {
        // Given
        ArticleContent content = new ArticleContent("标题", "内容", "摘要");
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(1L);

        // When
        ArticleAggregate article = ArticleAggregate.createDraft(content, categoryId, authorId);

        // Then
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(article.getContent().getTitle()).isEqualTo("标题");
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

#### 应用服务测试

```java
@ExtendWith(MockitoExtension.class)
class ArticleApplicationServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleApplicationService articleApplicationService;

    @Test
    void should_create_draft_successfully() {
        // Given
        ArticleContent content = new ArticleContent("标题", "内容", "摘要");
        CategoryId categoryId = new CategoryId(1L);
        AuthorId authorId = new AuthorId(1L);

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

### 13.3 集成测试规范

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
        assertThat(response.getBody().getCode()).isEqualTo(200);
    }
}
```

---

## 14. 性能优化规范

### 14.1 数据库查询优化

#### 避免 N+1 查询

```java
// ❌ 错误：N+1查询
List<ArticleAggregate> articles = findAllArticles();
for (ArticleAggregate article : articles) {
    CategoryAggregate category = categoryRepository.findById(article.getCategoryId());
    // 每次循环都查询一次数据库
}

// ✅ 正确：批量查询
List<ArticleAggregate> articles = findAllArticles();
Set<CategoryId> categoryIds = articles.stream()
    .map(ArticleAggregate::getCategoryId)
    .collect(Collectors.toSet());
Map<CategoryId, CategoryAggregate> categories = categoryRepository.findByIds(categoryIds);
```

#### 使用适当的索引

```sql
-- 为常用查询字段创建索引
CREATE INDEX idx_articles_status_published_at
    ON articles(status, published_at DESC);
```

### 14.2 分页查询优化

```java
@Repository
public class ArticleRepositoryImpl {

    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用投影，不查询大字段
        List<ArticlePO> articles = jpaRepository
            .findByStatusOrderByCreatedAtDescOptimized(ArticleStatus.PUBLISHED.name(), pageable);
        return convertToAggregates(articles);
    }
}
```

---

## 15. 版本控制规范

### 15.1 Git 分支策略

- `main` - 生产环境分支
- `develop` - 开发主分支
- `feature/*` - 功能分支
- `bugfix/*` - Bug 修复分支
- `hotfix/*` - 紧急修复分支

### 15.2 提交信息规范

遵循 Conventional Commits：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型**：

- `feat` - 新功能
- `fix` - Bug 修复
- `docs` - 文档更新
- `style` - 代码格式
- `refactor` - 重构
- `test` - 测试
- `chore` - 构建/工具变动

**示例**：

```
feat(article): 添加文章发布功能

- 实现文章状态从草稿到发布的转换
- 添加发布时间记录
- 发布时触发领域事件

Closes #123
```

---

## 16. 代码审查清单

提交代码前自查：

- [ ] 代码符合命名规范
- [ ] 领域层保持纯净（无框架依赖）
- [ ] 业务逻辑在领域层
- [ ] 应用服务只做编排
- [ ] 控制器职责单一
- [ ] 异常处理完善
- [ ] 日志记录合理
- [ ] 添加了必要的注释
- [ ] 编写了单元测试
- [ ] 敏感信息未硬编码
- [ ] 通过了所有测试

---

## 附录：常见问题

### Q1: 什么时候应该创建新的聚合？

**A**: 当一组对象需要保证一致性，且有明确的生命周期时，应创建聚合。聚合边界内的对象应该一起变化。

### Q2: 值对象可以有行为方法吗？

**A**: 可以，但方法应该是无副作用的，且不能修改自身状态。如需"修改"，返回新的值对象实例。

### Q3: 应用服务方法太多怎么办？

**A**: 考虑按功能拆分服务类，或引入命令/查询分离（CQRS）模式。

### Q4: 如何处理跨聚合的一致性？

**A**: 使用最终一致性，通过领域事件实现聚合间的通信。

---

**文档维护**: 本规范应随项目演进持续更新，所有团队成员都有责任提出改进建议。
