package com.cleveronion.blog.infrastructure.common.config;

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

/**
 * 缓存配置
 * 配置Redis作为缓存管理器，支持CQRS查询服务的缓存策略
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * 配置Redis缓存管理器
     * 
     * @param connectionFactory Redis连接工厂
     * @return CacheManager实例
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            // 缓存过期时间：10分钟
            .entryTtl(Duration.ofMinutes(10))
            // 键序列化：String
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
            // 值序列化：JSON
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()))
            // 不缓存null值
            .disableCachingNullValues();
        
        // 针对不同缓存区域的自定义配置
        RedisCacheConfiguration articlesConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(15));  // 文章详情缓存15分钟
        
        RedisCacheConfiguration articleListsConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(5));   // 文章列表缓存5分钟
        
        RedisCacheConfiguration articleStatsConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(30));  // 统计数据缓存30分钟
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            // 为不同的缓存区域配置不同的过期时间
            .withCacheConfiguration("articles", articlesConfig)
            .withCacheConfiguration("article-lists", articleListsConfig)
            .withCacheConfiguration("article-stats", articleStatsConfig)
            .build();
    }
}

