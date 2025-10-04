# 缓存实现指南

本文档提供详细的代码实现步骤，帮助开发者快速为 CleverOnion 博客系统添加 Redis 缓存功能。

## 1. 添加依赖

在 `pom.xml` 中确认已包含以下依赖：

```xml
<!-- Spring Boot Redis 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Spring Boot Cache 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Jackson 序列化依赖（通常已包含在 web starter 中） -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Jackson 时间模块（用于 LocalDateTime 序列化） -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

## 2. 创建缓存常量类

创建文件：`src/main/java/com/cleveronion/blog/common/cache/CacheNames.java`

```java
package com.cleveronion.blog.common.cache;

/**
 * 缓存名称常量定义
 * 统一管理所有缓存的名称，便于维护和修改
 *
 * @author CleverOnion
 * @since 2.0.0
 */
public final class CacheNames {

    // 私有构造函数，防止实例化
    private CacheNames() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== 文章相关缓存 ====================

    /**
     * 文章详情缓存
     * Key 格式: article:detail::{articleId}
     * TTL: 30 分钟
     */
    public static final String ARTICLE_DETAIL = "article:detail";

    /**
     * 文章列表缓存
     * Key 格式: article:list::status:PUBLISHED:page:0:size:10
     * TTL: 10 分钟
     */
    public static final String ARTICLE_LIST = "article:list";

    /**
     * 文章统计缓存
     * Key 格式: article:count::published
     * TTL: 5 分钟
     */
    public static final String ARTICLE_COUNT = "article:count";

    // ==================== 分类相关缓存 ====================

    /**
     * 分类详情缓存
     * Key 格式: category:detail::{categoryId}
     * TTL: 1 小时
     */
    public static final String CATEGORY_DETAIL = "category:detail";

    /**
     * 分类列表缓存
     * Key 格式: category:list::all
     * TTL: 30 分钟
     */
    public static final String CATEGORY_LIST = "category:list";

    /**
     * 分类统计缓存
     * Key 格式: category:count::{categoryId}
     * TTL: 10 分钟
     */
    public static final String CATEGORY_COUNT = "category:count";

    // ==================== 标签相关缓存 ====================

    /**
     * 标签详情缓存
     * Key 格式: tag:detail::{tagId}
     * TTL: 1 小时
     */
    public static final String TAG_DETAIL = "tag:detail";

    /**
     * 标签列表缓存
     * Key 格式: tag:list::all
     * TTL: 30 分钟
     */
    public static final String TAG_LIST = "tag:list";

    /**
     * 标签统计缓存
     * Key 格式: tag:count::{tagId}
     * TTL: 10 分钟
     */
    public static final String TAG_COUNT = "tag:count";

    // ==================== 用户相关缓存 ====================

    /**
     * 用户详情缓存
     * Key 格式: user:detail::{userId}
     * TTL: 30 分钟
     */
    public static final String USER_DETAIL = "user:detail";

    /**
     * 用户列表缓存
     * Key 格式: user:list::batch:[1,2,3]
     * TTL: 30 分钟
     */
    public static final String USER_LIST = "user:list";

    // ==================== 评论相关缓存 ====================

    /**
     * 评论列表缓存
     * Key 格式: comment:list::article:{articleId}:page:0:size:10
     * TTL: 5 分钟
     */
    public static final String COMMENT_LIST = "comment:list";

    /**
     * 评论统计缓存
     * Key 格式: comment:count::article:{articleId}
     * TTL: 5 分钟
     */
    public static final String COMMENT_COUNT = "comment:count";
}
```

## 3. 创建缓存配置类

创建文件：`src/main/java/com/cleveronion/blog/infrastructure/common/config/RedisCacheConfig.java`

```java
package com.cleveronion.blog.infrastructure.common.config;

import com.cleveronion.blog.common.cache.CacheNames;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置类
 * 配置 Spring Cache 使用 Redis 作为缓存实现
 *
 * @author CleverOnion
 * @since 2.0.0
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * 默认缓存配置
     * - 默认过期时间：30 分钟
     * - Key 序列化：String
     * - Value 序列化：JSON
     * - 不缓存 null 值
     *
     * @return 默认缓存配置
     */
    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        // 创建 ObjectMapper 用于 JSON 序列化
        ObjectMapper objectMapper = new ObjectMapper();

        // 设置可见性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用默认类型
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        // 注册 Java 8 时间模块（支持 LocalDateTime 等）
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 创建 JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            // 设置默认过期时间为 30 分钟
            .entryTtl(Duration.ofMinutes(30))
            // 配置 Key 序列化器（使用 String）
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer())
            )
            // 配置 Value 序列化器（使用 JSON）
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(jsonSerializer)
            )
            // 不缓存 null 值
            .disableCachingNullValues()
            // 使用 :: 作为 key 的分隔符
            .computePrefixWith(cacheName -> "cleveronion:cache:" + cacheName + "::");
    }

    /**
     * 缓存管理器
     * 为不同的缓存配置不同的过期时间
     *
     * @param connectionFactory Redis 连接工厂
     * @return 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 获取默认配置
        RedisCacheConfiguration defaultConfig = defaultCacheConfig();

        // 为不同的缓存配置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // ==================== 文章相关缓存 ====================

        // 文章详情 - 30 分钟（使用默认配置）
        cacheConfigurations.put(
            CacheNames.ARTICLE_DETAIL,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        // 文章列表 - 10 分钟
        cacheConfigurations.put(
            CacheNames.ARTICLE_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(10))
        );

        // 文章统计 - 5 分钟
        cacheConfigurations.put(
            CacheNames.ARTICLE_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        // ==================== 分类相关缓存 ====================

        // 分类详情 - 1 小时
        cacheConfigurations.put(
            CacheNames.CATEGORY_DETAIL,
            defaultConfig.entryTtl(Duration.ofHours(1))
        );

        // 分类列表 - 30 分钟
        cacheConfigurations.put(
            CacheNames.CATEGORY_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        // 分类统计 - 10 分钟
        cacheConfigurations.put(
            CacheNames.CATEGORY_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(10))
        );

        // ==================== 标签相关缓存 ====================

        // 标签详情 - 1 小时
        cacheConfigurations.put(
            CacheNames.TAG_DETAIL,
            defaultConfig.entryTtl(Duration.ofHours(1))
        );

        // 标签列表 - 30 分钟
        cacheConfigurations.put(
            CacheNames.TAG_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        // 标签统计 - 10 分钟
        cacheConfigurations.put(
            CacheNames.TAG_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(10))
        );

        // ==================== 用户相关缓存 ====================

        // 用户详情 - 30 分钟
        cacheConfigurations.put(
            CacheNames.USER_DETAIL,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        // 用户列表 - 30 分钟
        cacheConfigurations.put(
            CacheNames.USER_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        // ==================== 评论相关缓存 ====================

        // 评论列表 - 5 分钟（评论更新频繁）
        cacheConfigurations.put(
            CacheNames.COMMENT_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        // 评论统计 - 5 分钟
        cacheConfigurations.put(
            CacheNames.COMMENT_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        // 构建缓存管理器
        return RedisCacheManager.builder(connectionFactory)
            // 设置默认缓存配置
            .cacheDefaults(defaultConfig)
            // 设置特定缓存的配置
            .withInitialCacheConfigurations(cacheConfigurations)
            // 启用事务感知（在事务提交后才清除缓存）
            .transactionAware()
            .build();
    }
}
```

## 4. 更新配置文件

在 `application-dev.yml` 中添加缓存配置：

```yaml
spring:
  # Cache 配置
  cache:
    type: redis
    redis:
      # 默认过期时间（毫秒）
      time-to-live: 1800000 # 30 分钟
      # 不缓存 null 值
      cache-null-values: false
      # 使用 key 前缀
      use-key-prefix: true

  # Redis 配置（如果已存在则合并）
  data:
    redis:
      host: localhost
      port: 16379
      password:
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

# 日志配置（添加缓存相关日志）
logging:
  level:
    org.springframework.cache: DEBUG # 开发环境下启用缓存日志
    com.cleveronion.blog: DEBUG
```

## 5. 修改查询服务

### 5.1 文章查询服务

修改文件：`src/main/java/com/cleveronion/blog/application/article/service/ArticleQueryService.java`

```java
package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.common.cache.CacheNames;
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
 * 文章查询服务（CQRS - Query）
 * 负责处理所有文章查询操作，添加缓存支持
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
     * 根据ID查询文章（带缓存）
     * 缓存Key: article:detail::{articleId}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#id.value",
        unless = "#result == null || #result.isEmpty()"
    )
    public Optional<ArticleAggregate> findById(ArticleId id) {
        logger.debug("从数据库查询文章详情: articleId={}", id.getValue());
        return articleRepository.findById(id);
    }

    /**
     * 查询已发布文章列表（带缓存）
     * 缓存Key: article:list::published:page:{page}:size:{size}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'published:page:' + #page + ':size:' + #size",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findPublishedArticles(int page, int size) {
        logger.debug("从数据库查询已发布文章列表: page={}, size={}", page, size);
        return articleRepository.findPublishedArticles(page, size);
    }

    /**
     * 查询所有文章（带缓存）
     * 缓存Key: article:list::all:page:{page}:size:{size}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'all:page:' + #page + ':size:' + #size",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findAllArticles(int page, int size) {
        logger.debug("从数据库查询所有文章: page={}, size={}", page, size);
        return articleRepository.findAllArticles(page, size);
    }

    /**
     * 按状态查询文章（带缓存）
     * 缓存Key: article:list::status:{status}:page:{page}:size:{size}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'status:' + #status.name() + ':page:' + #page + ':size:' + #size",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findByStatus(ArticleStatus status, int page, int size) {
        logger.debug("从数据库按状态查询文章: status={}, page={}, size={}",
            status.name(), page, size);
        return articleRepository.findByStatus(status, page, size);
    }

    /**
     * 按分类查询已发布文章（带缓存）
     * 缓存Key: article:list::category:{categoryId}:page:{page}:size:{size}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'category:' + #categoryId.value + ':page:' + #page + ':size:' + #size",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findPublishedByCategoryId(
        CategoryId categoryId,
        int page,
        int size
    ) {
        logger.debug("从数据库按分类查询文章: categoryId={}, page={}, size={}",
            categoryId.getValue(), page, size);
        return articleRepository.findPublishedByCategoryId(categoryId, page, size);
    }

    /**
     * 按标签查询已发布文章（带缓存）
     * 缓存Key: article:list::tag:{tagId}:page:{page}:size:{size}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'tag:' + #tagId.value + ':page:' + #page + ':size:' + #size",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findPublishedByTagId(
        TagId tagId,
        int page,
        int size
    ) {
        logger.debug("从数据库按标签查询文章: tagId={}, page={}, size={}",
            tagId.getValue(), page, size);
        return articleRepository.findPublishedByTagId(tagId, page, size);
    }

    /**
     * 按分类和标签查询已发布文章（带缓存）
     * 缓存Key: article:list::category:{categoryId}:tag:{tagId}:page:{page}:size:{size}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'category:' + #categoryId.value + ':tag:' + #tagId.value + ':page:' + #page + ':size:' + #size",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findPublishedByCategoryAndTag(
        CategoryId categoryId,
        TagId tagId,
        int page,
        int size
    ) {
        logger.debug("从数据库按分类和标签查询文章: categoryId={}, tagId={}, page={}, size={}",
            categoryId.getValue(), tagId.getValue(), page, size);
        return articleRepository.findPublishedByCategoryAndTag(categoryId, tagId, page, size);
    }

    /**
     * 按作者查询文章（带缓存）
     * 缓存Key: article:list::author:{authorId}:page:{page}:size:{size}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'author:' + #authorId.value + ':page:' + #page + ':size:' + #size",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findByAuthorId(
        AuthorId authorId,
        int page,
        int size
    ) {
        logger.debug("从数据库按作者查询文章: authorId={}, page={}, size={}",
            authorId.getValue(), page, size);
        return articleRepository.findByAuthorId(authorId, page, size);
    }

    /**
     * 按分类查询文章（不分页，带缓存）
     * 缓存Key: article:list::category:{categoryId}:all
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'category:' + #categoryId.value + ':all'",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findByCategoryId(CategoryId categoryId) {
        logger.debug("从数据库按分类查询所有文章: categoryId={}", categoryId.getValue());
        return articleRepository.findByCategoryId(categoryId);
    }

    /**
     * 按标题搜索文章（不缓存，搜索条件多样）
     */
    public List<ArticleAggregate> searchByTitle(String keyword) {
        logger.debug("从数据库搜索文章: keyword={}", keyword);
        return articleRepository.findByTitleContaining(keyword);
    }

    /**
     * 查询最近发布的文章（带缓存）
     * 缓存Key: article:list::recent:{limit}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'recent:' + #limit",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findRecentlyPublished(int limit) {
        logger.debug("从数据库查询最近发布的文章: limit={}", limit);
        return articleRepository.findRecentlyPublished(limit);
    }

    /**
     * 查询热门文章（带缓存）
     * 缓存Key: article:list::popular:{limit}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_LIST,
        key = "'popular:' + #limit",
        unless = "#result == null || #result.isEmpty()"
    )
    public List<ArticleAggregate> findPopularArticles(int limit) {
        logger.debug("从数据库查询热门文章: limit={}", limit);
        return articleRepository.findPopularArticles(limit);
    }

    // ==================== 统计查询（带缓存） ====================

    /**
     * 统计已发布文章数量（带缓存）
     * 缓存Key: article:count::published
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_COUNT,
        key = "'published'"
    )
    public long countPublishedArticles() {
        logger.debug("从数据库统计已发布文章数量");
        return articleRepository.countPublishedArticles();
    }

    /**
     * 统计所有文章数量（带缓存）
     * 缓存Key: article:count::all
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_COUNT,
        key = "'all'"
    )
    public long countAllArticles() {
        logger.debug("从数据库统计所有文章数量");
        return articleRepository.countAllArticles();
    }

    /**
     * 按状态统计文章数量（带缓存）
     * 缓存Key: article:count::status:{status}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_COUNT,
        key = "'status:' + #status.name()"
    )
    public long countByStatus(ArticleStatus status) {
        logger.debug("从数据库按状态统计文章: status={}", status.name());
        return articleRepository.countByStatus(status);
    }

    /**
     * 按分类统计文章数量（带缓存）
     * 缓存Key: article:count::category:{categoryId}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_COUNT,
        key = "'category:' + #categoryId.value"
    )
    public long countByCategoryId(CategoryId categoryId) {
        logger.debug("从数据库按分类统计文章: categoryId={}", categoryId.getValue());
        return articleRepository.countByCategoryId(categoryId);
    }

    /**
     * 按标签统计文章数量（带缓存）
     * 缓存Key: article:count::tag:{tagId}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_COUNT,
        key = "'tag:' + #tagId.value"
    )
    public long countByTagId(TagId tagId) {
        logger.debug("从数据库按标签统计文章: tagId={}", tagId.getValue());
        return articleRepository.countByTagId(tagId);
    }

    /**
     * 按作者统计文章数量（带缓存）
     * 缓存Key: article:count::author:{authorId}
     */
    @Cacheable(
        cacheNames = CacheNames.ARTICLE_COUNT,
        key = "'author:' + #authorId.value"
    )
    public long countByAuthorId(AuthorId authorId) {
        logger.debug("从数据库按作者统计文章: authorId={}", authorId.getValue());
        return articleRepository.countByAuthorId(authorId);
    }
}
```

## 6. 修改命令服务

### 6.1 文章命令服务

修改文件：`src/main/java/com/cleveronion/blog/application/article/service/ArticleCommandService.java`

在类顶部添加导入：

```java
import com.cleveronion.blog.common.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
```

然后在各个方法上添加缓存清除注解：

```java
/**
 * 创建文章草稿（清除缓存）
 */
@CacheEvict(
    cacheNames = {
        CacheNames.ARTICLE_LIST,
        CacheNames.ARTICLE_COUNT
    },
    allEntries = true
)
public ArticleAggregate createDraft(CreateArticleDraftCommand command) {
    logger.debug("执行创建文章草稿命令: authorId={}, categoryId={}, title={}",
        command.getAuthorId().getValue(),
        command.getCategoryId().getValue(),
        command.getContent().getTitle());

    // ... 现有业务逻辑保持不变
}

/**
 * 创建并发布文章（清除缓存）
 */
@CacheEvict(
    cacheNames = {
        CacheNames.ARTICLE_LIST,
        CacheNames.ARTICLE_COUNT
    },
    allEntries = true
)
public ArticleAggregate createAndPublish(PublishArticleCommand command) {
    logger.debug("执行创建并发布文章命令: authorId={}, title={}",
        command.getAuthorId().getValue(),
        command.getContent().getTitle());

    // ... 现有业务逻辑保持不变
}

/**
 * 更新文章内容（清除缓存）
 */
@Caching(evict = {
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#command.articleId.value"
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_LIST,
        allEntries = true
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_COUNT,
        allEntries = true
    )
})
public ArticleAggregate updateContent(UpdateArticleCommand command) {
    logger.debug("执行更新文章命令: articleId={}", command.getArticleId().getValue());

    // ... 现有业务逻辑保持不变
}

/**
 * 发布文章（清除缓存）
 */
@Caching(evict = {
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#articleId.value"
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_LIST,
        allEntries = true
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_COUNT,
        allEntries = true
    )
})
public ArticleAggregate publish(ArticleId articleId, AuthorId authorId) {
    logger.debug("执行发布文章命令: articleId={}", articleId.getValue());

    // ... 现有业务逻辑保持不变
}

/**
 * 归档文章（清除缓存）
 */
@Caching(evict = {
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#articleId.value"
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_LIST,
        allEntries = true
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_COUNT,
        allEntries = true
    )
})
public ArticleAggregate archive(ArticleId articleId, AuthorId authorId) {
    logger.debug("执行归档文章命令: articleId={}", articleId.getValue());

    // ... 现有业务逻辑保持不变
}

/**
 * 撤回文章到草稿（清除缓存）
 */
@Caching(evict = {
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#articleId.value"
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_LIST,
        allEntries = true
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_COUNT,
        allEntries = true
    )
})
public ArticleAggregate revertToDraft(ArticleId articleId, AuthorId authorId) {
    logger.debug("执行撤回文章到草稿命令: articleId={}", articleId.getValue());

    // ... 现有业务逻辑保持不变
}

/**
 * 删除文章（清除缓存）
 */
@Caching(evict = {
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#articleId.value"
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_LIST,
        allEntries = true
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_COUNT,
        allEntries = true
    )
})
public void delete(ArticleId articleId, AuthorId authorId) {
    logger.debug("执行删除文章命令: articleId={}", articleId.getValue());

    // ... 现有业务逻辑保持不变
}

/**
 * 添加标签到文章（清除缓存）
 */
@Caching(evict = {
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#articleId.value"
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_LIST,
        allEntries = true
    )
})
public ArticleAggregate addTags(ArticleId articleId, Set<TagId> tagIds, AuthorId authorId) {
    // ... 现有业务逻辑保持不变
}

/**
 * 从文章移除标签（清除缓存）
 */
@Caching(evict = {
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_DETAIL,
        key = "#articleId.value"
    ),
    @CacheEvict(
        cacheNames = CacheNames.ARTICLE_LIST,
        allEntries = true
    )
})
public ArticleAggregate removeTags(ArticleId articleId, Set<TagId> tagIds, AuthorId authorId) {
    // ... 现有业务逻辑保持不变
}
```

## 7. 其他服务的缓存实现

按照相同的模式，为其他服务添加缓存：

- `CategoryQueryService` - 添加 `@Cacheable`
- `CategoryCommandService` - 添加 `@CacheEvict`
- `TagQueryService` - 添加 `@Cacheable`
- `TagCommandService` - 添加 `@CacheEvict`
- `UserQueryService` - 添加 `@Cacheable`
- `CommentQueryService` - 添加 `@Cacheable`
- `CommentCommandService` - 添加 `@CacheEvict`

具体实现请参考主设计文档中的示例。

## 8. 测试缓存功能

### 8.1 手动测试

1. 启动 Redis 服务
2. 启动应用
3. 调用查询接口，观察日志
4. 再次调用相同接口，应该不会有数据库查询日志
5. 调用更新接口
6. 再次查询，应该会重新查询数据库

### 8.2 使用 Redis CLI 验证

```bash
# 连接到 Redis
redis-cli -h localhost -p 16379

# 查看所有缓存 key
KEYS "cleveronion:cache:*"

# 查看特定 key 的值
GET "cleveronion:cache:article:detail::123"

# 查看 key 的 TTL
TTL "cleveronion:cache:article:detail::123"

# 手动删除缓存
DEL "cleveronion:cache:article:detail::123"

# 清空所有缓存
FLUSHDB
```

### 8.3 单元测试示例

创建测试类：`src/test/java/com/cleveronion/blog/application/article/service/ArticleQueryServiceCacheTest.java`

```java
package com.cleveronion.blog.application.article.service;

import com.cleveronion.blog.common.cache.CacheNames;
import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ArticleQueryServiceCacheTest {

    @Autowired
    private ArticleQueryService articleQueryService;

    @Autowired
    private ArticleCommandService articleCommandService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testArticleDetailCacheHit() {
        // 准备测试数据
        ArticleId articleId = new ArticleId("123");

        // 清空缓存
        Cache cache = cacheManager.getCache(CacheNames.ARTICLE_DETAIL);
        assertThat(cache).isNotNull();
        cache.clear();

        // 第一次查询 - Cache Miss
        Optional<ArticleAggregate> result1 = articleQueryService.findById(articleId);

        // 第二次查询 - Cache Hit
        Optional<ArticleAggregate> result2 = articleQueryService.findById(articleId);

        // 验证缓存中有数据
        Cache.ValueWrapper cachedValue = cache.get(articleId.getValue());
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue.get()).isEqualTo(result1);
    }

    @Test
    void testCacheEvictionOnUpdate() {
        // 准备测试数据
        ArticleId articleId = new ArticleId("123");

        // 先查询，填充缓存
        articleQueryService.findById(articleId);

        // 验证缓存存在
        Cache cache = cacheManager.getCache(CacheNames.ARTICLE_DETAIL);
        assertThat(cache).isNotNull();
        assertThat(cache.get(articleId.getValue())).isNotNull();

        // 更新文章（应该清除缓存）
        UpdateArticleCommand command = new UpdateArticleCommand(/* ... */);
        articleCommandService.updateContent(command);

        // 验证缓存已被清除
        assertThat(cache.get(articleId.getValue())).isNull();
    }
}
```

## 9. 常见问题

### 9.1 缓存不生效

**可能原因**:

1. 没有启用缓存（检查 `@EnableCaching` 注解）
2. Redis 连接失败（检查 Redis 是否启动）
3. 方法调用方式不对（Spring AOP 代理问题）

**解决方案**:

- 确保在配置类上添加了 `@EnableCaching`
- 检查 Redis 连接配置
- 避免在同一个类内部调用缓存方法（会绕过代理）

### 9.2 缓存数据不更新

**可能原因**:

1. 缓存失效逻辑不正确
2. 事务未提交就清除了缓存

**解决方案**:

- 检查 `@CacheEvict` 注解配置
- 确保缓存管理器启用了 `transactionAware()`

### 9.3 序列化错误

**可能原因**:

1. 领域对象无法序列化
2. 包含循环引用

**解决方案**:

- 确保所有需要缓存的对象可序列化
- 使用 `@JsonIgnore` 忽略不需要序列化的字段

## 10. 性能监控

在生产环境建议添加缓存监控：

1. **缓存命中率监控**
2. **缓存大小监控**
3. **缓存响应时间监控**

可以通过 Spring Boot Actuator 或自定义 AOP 实现监控。

## 11. 总结

完成以上步骤后，系统将具备完整的 Redis 缓存功能：

✅ 查询操作自动缓存  
✅ 更新操作自动清除缓存  
✅ 不同数据配置不同过期时间  
✅ 支持事务感知的缓存管理  
✅ 完整的监控和测试能力

后续可以根据实际运行情况调整缓存策略和过期时间。
