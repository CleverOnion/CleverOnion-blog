# Redis 缓存最佳实践指南

## 🔴 问题：领域对象缓存的三大问题

### 问题1：LocalDateTime序列化失败
```
LocalDateTime not supported by default
```

### 问题2：类型信息丢失
```
LinkedHashMap cannot be cast to ArticleAggregate
```

### 问题3：值对象无法反序列化
```
Cannot construct instance of ArticleId
```

---

## ✅ 通用解决方案

### 方案：不缓存领域对象，缓存DTO

**核心原则**：**在表现层（Controller）缓存DTO，而非在应用层缓存领域对象**

---

## 📖 实施指南

### 1. QueryService：移除缓存注解

```java
@Service
@Transactional(readOnly = true)
public class ArticleQueryService {
    
    // ✅ 不添加@Cacheable注解
    public Optional<ArticleAggregate> findById(ArticleId id) {
        return repository.findById(id);
    }
    
    // ✅ 不添加@Cacheable注解
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        return repository.findPublishedArticles(page, size);
    }
}
```

### 2. Controller：添加缓存注解（如需要）

```java
@RestController
@RequestMapping("/articles")
public class ArticleController {
    
    private final ArticleQueryService queryService;
    
    // ✅ 在Controller层缓存DTO
    @GetMapping("/{id}")
    @Cacheable(value = "article-responses", key = "#id")
    public Result<ArticleResponse> getArticleById(@PathVariable Long id) {
        ArticleId articleId = new ArticleId(id.toString());
        
        Optional<ArticleAggregate> article = queryService.findById(articleId);
        if (article.isEmpty()) {
            return Result.error("文章不存在");
        }
        
        // 缓存DTO而非领域对象
        ArticleResponse response = new ArticleResponse(article.get());
        return Result.success(response);
    }
    
    @GetMapping
    @Cacheable(value = "article-list-responses", 
               key = "'page:' + #page + ':size:' + #size")
    public Result<ArticleListResponse> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<ArticleAggregate> articles = queryService.findPublishedArticles(page, size);
        long total = queryService.countPublishedArticles();
        
        // 转换为DTO后缓存
        List<ArticleResponse> responses = articles.stream()
            .map(ArticleResponse::new)
            .toList();
            
        return Result.success(new ArticleListResponse(responses, total, page, size));
    }
}
```

### 3. CommandService：保留缓存失效

```java
@Service
@Transactional
public class ArticleCommandService {
    
    // ✅ 命令方法仍然需要@CacheEvict
    @CacheEvict(value = {"article-responses", "article-list-responses"}, all