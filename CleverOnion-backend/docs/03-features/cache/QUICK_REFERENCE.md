# 缓存快速参考手册

本文档提供常用的缓存操作快速参考。

## 1. 缓存注解速查

### 1.1 @Cacheable（查询方法）

```java
// 基础用法 - 单参数
@Cacheable(cacheNames = "article:detail", key = "#id.value")
public Optional<ArticleAggregate> findById(ArticleId id) { }

// 多参数组合 Key
@Cacheable(
    cacheNames = "article:list",
    key = "'page:' + #page + ':size:' + #size"
)
public List<ArticleAggregate> findAll(int page, int size) { }

// 条件缓存 - 不缓存空结果
@Cacheable(
    cacheNames = "article:detail",
    key = "#id.value",
    unless = "#result == null || #result.isEmpty()"
)
public Optional<ArticleAggregate> findById(ArticleId id) { }

// 条件缓存 - 只缓存特定条件
@Cacheable(
    cacheNames = "article:list",
    key = "#status.name()",
    condition = "#status == T(com.cleveronion.blog.domain.article.valueobject.ArticleStatus).PUBLISHED"
)
public List<ArticleAggregate> findByStatus(ArticleStatus status) { }
```

### 1.2 @CacheEvict（清除缓存）

```java
// 清除单个 Key
@CacheEvict(cacheNames = "article:detail", key = "#id.value")
public void delete(ArticleId id) { }

// 清除所有 Key
@CacheEvict(cacheNames = "article:list", allEntries = true)
public void deleteAll() { }

// 清除多个缓存
@CacheEvict(
    cacheNames = {"article:detail", "article:list"},
    allEntries = true
)
public void updateArticle(ArticleId id) { }
```

### 1.3 @Caching（组合注解）

```java
// 组合多个缓存操作
@Caching(evict = {
    @CacheEvict(cacheNames = "article:detail", key = "#id.value"),
    @CacheEvict(cacheNames = "article:list", allEntries = true),
    @CacheEvict(cacheNames = "article:count", allEntries = true)
})
public void updateArticle(ArticleId id) { }
```

### 1.4 @CachePut（更新缓存）

```java
// 更新缓存（方法总是执行，结果更新到缓存）
@CachePut(cacheNames = "article:detail", key = "#article.id.value")
public ArticleAggregate update(ArticleAggregate article) { }
```

## 2. 缓存名称快查表

| 缓存名称          | 用途     | TTL     | Key 示例                   |
| ----------------- | -------- | ------- | -------------------------- |
| `article:detail`  | 文章详情 | 30 分钟 | `123`                      |
| `article:list`    | 文章列表 | 10 分钟 | `published:page:0:size:10` |
| `article:count`   | 文章统计 | 5 分钟  | `published`                |
| `category:detail` | 分类详情 | 1 小时  | `456`                      |
| `category:list`   | 分类列表 | 30 分钟 | `all`                      |
| `tag:detail`      | 标签详情 | 1 小时  | `789`                      |
| `tag:list`        | 标签列表 | 30 分钟 | `all`                      |
| `user:detail`     | 用户详情 | 30 分钟 | `111`                      |

## 3. 常用 Key 生成模式

### 3.1 单参数

```java
// 值对象
key = "#id.value"                    // 123
key = "#categoryId.value"            // 456

// 基础类型
key = "#page"                        // 0
key = "#userId"                      // 123
```

### 3.2 多参数

```java
// 字符串拼接
key = "'page:' + #page + ':size:' + #size"
// 结果: page:0:size:10

// 枚举类型
key = "'status:' + #status.name()"
// 结果: status:PUBLISHED

// 复杂组合
key = "'category:' + #categoryId.value + ':tag:' + #tagId.value + ':page:' + #page"
// 结果: category:123:tag:456:page:0
```

### 3.3 集合参数

```java
// Set 转字符串
key = "'batch:' + #ids.toString()"
// 结果: batch:[1, 2, 3]

// List 处理
key = "'items:' + T(String).join(',', #items)"
// 结果: items:1,2,3
```

## 4. 缓存清除场景速查

### 4.1 文章操作

| 操作     | 清除的缓存                                             |
| -------- | ------------------------------------------------------ |
| 创建文章 | `article:list`, `article:count`                        |
| 更新文章 | `article:detail:{id}`, `article:list`, `article:count` |
| 删除文章 | `article:detail:{id}`, `article:list`, `article:count` |
| 发布文章 | `article:detail:{id}`, `article:list`, `article:count` |
| 添加标签 | `article:detail:{id}`, `article:list`                  |

### 4.2 分类操作

| 操作     | 清除的缓存                                              |
| -------- | ------------------------------------------------------- |
| 创建分类 | `category:list`                                         |
| 更新分类 | `category:detail:{id}`, `category:list`, `article:list` |
| 删除分类 | `category:detail:{id}`, `category:list`, `article:list` |

### 4.3 标签操作

| 操作     | 清除的缓存                                    |
| -------- | --------------------------------------------- |
| 创建标签 | `tag:list`                                    |
| 更新标签 | `tag:detail:{id}`, `tag:list`, `article:list` |
| 删除标签 | `tag:detail:{id}`, `tag:list`, `article:list` |

## 5. Redis 命令速查

### 5.1 查看缓存

```bash
# 查看所有缓存 key
redis-cli KEYS "cleveronion:cache:*"

# 查看文章详情缓存
redis-cli KEYS "cleveronion:cache:article:detail::*"

# 查看特定缓存的值
redis-cli GET "cleveronion:cache:article:detail::123"

# 查看缓存剩余过期时间（秒）
redis-cli TTL "cleveronion:cache:article:detail::123"

# 查看缓存过期时间（Unix 时间戳）
redis-cli PTTL "cleveronion:cache:article:detail::123"
```

### 5.2 删除缓存

```bash
# 删除单个缓存
redis-cli DEL "cleveronion:cache:article:detail::123"

# 删除所有文章详情缓存
redis-cli --scan --pattern "cleveronion:cache:article:detail::*" | xargs redis-cli DEL

# 删除所有文章列表缓存
redis-cli --scan --pattern "cleveronion:cache:article:list::*" | xargs redis-cli DEL

# 清空当前数据库所有缓存
redis-cli FLUSHDB

# 清空所有数据库（慎用！）
redis-cli FLUSHALL
```

### 5.3 监控缓存

```bash
# 实时监控 Redis 命令
redis-cli MONITOR

# 查看 Redis 信息
redis-cli INFO

# 查看内存使用情况
redis-cli INFO memory

# 查看缓存命中率
redis-cli INFO stats | grep keyspace

# 查看大 key
redis-cli --bigkeys

# 统计 key 数量
redis-cli DBSIZE
```

## 6. 常见问题快速解决

### 6.1 缓存不生效

```java
// ❌ 错误：同类内部调用
public class ArticleService {
    public void method1() {
        this.method2();  // 不会触发缓存
    }

    @Cacheable(...)
    public void method2() { }
}

// ✅ 正确：通过 Spring 注入的代理对象调用
@Autowired
private ArticleService articleService;

public void method1() {
    articleService.method2();  // 会触发缓存
}
```

### 6.2 缓存数据不更新

```java
// ❌ 错误：在事务提交前清除缓存
@Transactional
@CacheEvict(...)
public void update() {
    // 修改数据
    // 缓存立即清除，但事务可能回滚
}

// ✅ 正确：启用事务感知
@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
    return RedisCacheManager.builder(factory)
        .transactionAware()  // 事务提交后才清除缓存
        .build();
}
```

### 6.3 序列化错误

```java
// ❌ 错误：对象无法序列化
public class Article {
    private transient LocalDateTime createdAt;  // transient 字段不会被序列化
}

// ✅ 正确：确保所有字段可序列化
public class Article {
    private LocalDateTime createdAt;  // 使用 Jackson 的 JavaTimeModule
}

// ✅ 正确：忽略不需要缓存的字段
public class Article {
    @JsonIgnore
    private SensitiveData secret;  // 不会被序列化和缓存
}
```

## 7. 缓存配置速查

### 7.1 application.yml 配置

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 1800000 # 默认 30 分钟
      cache-null-values: false # 不缓存 null 值
      use-key-prefix: true # 使用 key 前缀

  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 5000ms
```

### 7.2 Java 配置

```java
@Configuration
@EnableCaching  // 启用缓存
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))         // 过期时间
            .disableCachingNullValues()                // 不缓存 null
            .serializeKeysWith(stringSerializer)       // Key 序列化
            .serializeValuesWith(jsonSerializer);      // Value 序列化
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultCacheConfig())
            .transactionAware()  // 事务感知
            .build();
    }
}
```

## 8. 性能优化建议

### 8.1 缓存粒度

```java
// ❌ 不好：缓存整个大对象（包含大文本）
@Cacheable("article:detail")
public ArticleAggregate findById(ArticleId id) {
    return repository.findById(id);  // 包含 10MB 的文章内容
}

// ✅ 更好：只缓存元数据，内容单独缓存或不缓存
@Cacheable("article:meta")
public ArticleMetadata findMetaById(ArticleId id) {
    return repository.findMetaById(id);  // 只包含标题、摘要等
}
```

### 8.2 批量查询优化

```java
// ❌ 不好：循环查询导致 N+1 问题
for (ArticleId id : ids) {
    findById(id);  // 多次查询
}

// ✅ 更好：批量查询
@Cacheable(...)
public List<ArticleAggregate> findByIds(Set<ArticleId> ids) {
    return repository.findByIds(ids);  // 一次查询
}
```

### 8.3 缓存预热

```java
// 应用启动时预热热点数据
@Component
public class CacheWarmer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        // 预加载首页文章
        articleQueryService.findRecentlyPublished(10);

        // 预加载热门分类
        categoryQueryService.findAll();
    }
}
```

## 9. 调试技巧

### 9.1 启用缓存日志

```yaml
logging:
  level:
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG
```

### 9.2 监控缓存操作

```java
@Aspect
@Component
@Slf4j
public class CacheLoggingAspect {

    @Around("@annotation(cacheable)")
    public Object logCacheable(ProceedingJoinPoint pjp, Cacheable cacheable) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.debug("缓存查询: {} - {}", cacheable.cacheNames(), method);

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long duration = System.currentTimeMillis() - start;

        log.debug("缓存查询完成: {} - 耗时 {}ms", method, duration);
        return result;
    }
}
```

## 10. 测试代码模板

```java
@SpringBootTest
@AutoConfigureCache
class CacheTest {

    @Autowired
    private ArticleQueryService queryService;

    @Autowired
    private ArticleCommandService commandService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testCacheHit() {
        // 1. 清空缓存
        Cache cache = cacheManager.getCache("article:detail");
        cache.clear();

        // 2. 第一次查询（Cache Miss）
        ArticleId id = new ArticleId("123");
        Optional<ArticleAggregate> result1 = queryService.findById(id);

        // 3. 验证缓存已填充
        assertThat(cache.get("123")).isNotNull();

        // 4. 第二次查询（Cache Hit）
        Optional<ArticleAggregate> result2 = queryService.findById(id);

        // 5. 验证返回相同结果
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void testCacheEvict() {
        // 1. 填充缓存
        ArticleId id = new ArticleId("123");
        queryService.findById(id);

        // 2. 验证缓存存在
        Cache cache = cacheManager.getCache("article:detail");
        assertThat(cache.get("123")).isNotNull();

        // 3. 执行更新操作
        commandService.updateArticle(id, ...);

        // 4. 验证缓存已清除
        assertThat(cache.get("123")).isNull();
    }
}
```

---

**提示**: 将本文档保存为书签，随时查阅常用的缓存操作！
