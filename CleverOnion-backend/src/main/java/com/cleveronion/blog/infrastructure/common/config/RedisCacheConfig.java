package com.cleveronion.blog.infrastructure.common.config;

import com.cleveronion.blog.common.cache.CacheNames;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 
 * <p>配置 Spring Cache 使用 Redis 作为缓存实现，提供以下功能：
 * <ul>
 *   <li>启用声明式缓存支持（@Cacheable、@CacheEvict 等）</li>
 *   <li>配置 Redis 序列化策略（Key: String, Value: JSON）</li>
 *   <li>为不同类型的缓存设置不同的过期时间（TTL）</li>
 *   <li>启用事务感知，确保缓存一致性</li>
 * </ul>
 * 
 * <p>缓存 Key 格式：{key-prefix}::{cacheName}::{key}
 * <p>示例：cleveronion:cache::article:detail::123
 * 
 * @author CleverOnion
 * @since 2.0.0
 * @see CacheNames
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);
    
    /**
     * 默认缓存配置
     * 
     * <p>配置项：
     * <ul>
     *   <li>默认过期时间：30 分钟</li>
     *   <li>Key 序列化：StringRedisSerializer（字符串）</li>
     *   <li>Value 序列化：GenericJackson2JsonRedisSerializer（JSON）</li>
     *   <li>不缓存 null 值</li>
     *   <li>Key 前缀：cleveronion:cache::{cacheName}::</li>
     * </ul>
     * 
     * @return 默认缓存配置
     */
    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        logger.info("初始化默认 Redis 缓存配置");
        
        // 创建 ObjectMapper 用于 JSON 序列化
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 设置可见性：所有字段都可以被序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 启用默认类型：非 final 类型需要包含类型信息
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // 注册 Java 8 时间模块（支持 LocalDateTime、LocalDate 等）
        objectMapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳（使用 ISO-8601 格式）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 忽略 JSON 中未知的属性（解决计算方法导致的反序列化问题）
        objectMapper.configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        );
        
        // 创建 JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        
        logger.debug("配置缓存序列化器：Key=String, Value=JSON");
        
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
            // 不缓存 null 值（避免缓存穿透）
            .disableCachingNullValues()
            // 配置 key 前缀计算方式（使用 :: 作为分隔符）
            .computePrefixWith(cacheName -> "cleveronion:cache:" + cacheName + "::");
    }
    
    /**
     * 缓存管理器
     * 
     * <p>为不同的缓存配置不同的过期时间（TTL），优化缓存策略：
     * <ul>
     *   <li>长缓存（1小时）：分类详情、标签详情（更新频率低）</li>
     *   <li>中缓存（30分钟）：文章详情、用户详情、分类列表、标签列表</li>
     *   <li>短缓存（10分钟）：文章列表、分类统计、标签统计</li>
     *   <li>超短缓存（5分钟）：文章统计、评论列表、评论统计（更新频繁）</li>
     * </ul>
     * 
     * <p>特性：
     * <ul>
     *   <li>事务感知：在事务提交后才清除缓存，确保数据一致性</li>
     *   <li>差异化 TTL：根据数据特点设置合适的过期时间</li>
     * </ul>
     * 
     * @param connectionFactory Redis 连接工厂
     * @return 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        logger.info("初始化 Redis 缓存管理器");
        
        // 获取默认配置
        RedisCacheConfiguration defaultConfig = defaultCacheConfig();
        
        // 为不同的缓存配置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // ==================== 文章相关缓存 ====================
        
        // 文章详情 - 30 分钟（中等更新频率）
        cacheConfigurations.put(
            CacheNames.ARTICLE_DETAIL,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );
        logger.debug("配置缓存: {} - TTL: 30分钟", CacheNames.ARTICLE_DETAIL);
        
        // 文章列表 - 10 分钟（较高更新频率，涉及分页和过滤）
        cacheConfigurations.put(
            CacheNames.ARTICLE_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(10))
        );
        logger.debug("配置缓存: {} - TTL: 10分钟", CacheNames.ARTICLE_LIST);
        
        // 文章统计 - 5 分钟（高更新频率，统计数据实时性要求较高）
        cacheConfigurations.put(
            CacheNames.ARTICLE_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(5))
        );
        logger.debug("配置缓存: {} - TTL: 5分钟", CacheNames.ARTICLE_COUNT);
        
        // ==================== 分类相关缓存 ====================
        
        // 分类详情 - 1 小时（低更新频率，分类信息相对稳定）
        cacheConfigurations.put(
            CacheNames.CATEGORY_DETAIL,
            defaultConfig.entryTtl(Duration.ofHours(1))
        );
        logger.debug("配置缓存: {} - TTL: 1小时", CacheNames.CATEGORY_DETAIL);
        
        // 分类列表 - 30 分钟（低更新频率）
        cacheConfigurations.put(
            CacheNames.CATEGORY_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );
        logger.debug("配置缓存: {} - TTL: 30分钟", CacheNames.CATEGORY_LIST);
        
        // 分类统计 - 10 分钟（中等更新频率）
        cacheConfigurations.put(
            CacheNames.CATEGORY_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(10))
        );
        logger.debug("配置缓存: {} - TTL: 10分钟", CacheNames.CATEGORY_COUNT);
        
        // ==================== 标签相关缓存 ====================
        
        // 标签详情 - 1 小时（低更新频率，标签信息相对稳定）
        cacheConfigurations.put(
            CacheNames.TAG_DETAIL,
            defaultConfig.entryTtl(Duration.ofHours(1))
        );
        logger.debug("配置缓存: {} - TTL: 1小时", CacheNames.TAG_DETAIL);
        
        // 标签列表 - 30 分钟（低更新频率）
        cacheConfigurations.put(
            CacheNames.TAG_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );
        logger.debug("配置缓存: {} - TTL: 30分钟", CacheNames.TAG_LIST);
        
        // 标签统计 - 10 分钟（中等更新频率）
        cacheConfigurations.put(
            CacheNames.TAG_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(10))
        );
        logger.debug("配置缓存: {} - TTL: 10分钟", CacheNames.TAG_COUNT);
        
        // ==================== 用户相关缓存 ====================
        
        // 用户详情 - 30 分钟（中等更新频率）
        cacheConfigurations.put(
            CacheNames.USER_DETAIL,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );
        logger.debug("配置缓存: {} - TTL: 30分钟", CacheNames.USER_DETAIL);
        
        // 用户列表 - 30 分钟（批量查询）
        cacheConfigurations.put(
            CacheNames.USER_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        );
        logger.debug("配置缓存: {} - TTL: 30分钟", CacheNames.USER_LIST);
        
        // ==================== 评论相关缓存 ====================
        
        // 评论列表 - 5 分钟（高更新频率，评论互动性强）
        cacheConfigurations.put(
            CacheNames.COMMENT_LIST,
            defaultConfig.entryTtl(Duration.ofMinutes(5))
        );
        logger.debug("配置缓存: {} - TTL: 5分钟", CacheNames.COMMENT_LIST);
        
        // 评论统计 - 5 分钟（高更新频率）
        cacheConfigurations.put(
            CacheNames.COMMENT_COUNT,
            defaultConfig.entryTtl(Duration.ofMinutes(5))
        );
        logger.debug("配置缓存: {} - TTL: 5分钟", CacheNames.COMMENT_COUNT);
        
        logger.info("已配置 {} 个缓存，启用事务感知模式", cacheConfigurations.size());
        
        // 构建缓存管理器
        return RedisCacheManager.builder(connectionFactory)
            // 设置默认缓存配置
            .cacheDefaults(defaultConfig)
            // 设置特定缓存的配置
            .withInitialCacheConfigurations(cacheConfigurations)
            // 启用事务感知（在事务提交后才清除缓存，保证数据一致性）
            .transactionAware()
            .build();
    }
}

