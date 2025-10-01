package com.cleveronion.blog.infrastructure.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
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
        
        // 创建支持Java 8日期时间的ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 配置宽松的多态类型验证器（生产环境建议更严格）
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Object.class)  // 允许所有类型（最宽松）
            .build();
        
        // 启用默认类型信息（用于反序列化）
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
        
        // 使用自定义ObjectMapper创建序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            // 缓存过期时间：10分钟
            .entryTtl(Duration.ofMinutes(10))
            // 键序列化：String
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
            // 值序列化：JSON（支持Java 8日期时间）
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                jsonSerializer))
            // 不缓存null值
            .disableCachingNullValues();
        
        // 针对不同缓存区域的自定义配置（缓存DTO响应）
        RedisCacheConfiguration articleResponsesConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(15));  // 文章详情响应缓存15分钟
        
        RedisCacheConfiguration articleListResponsesConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(5));   // 文章列表响应缓存5分钟
        
        RedisCacheConfiguration articleSearchResponsesConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(3));   // 搜索响应缓存3分钟
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            // 为不同的缓存区域配置不同的过期时间
            .withCacheConfiguration("article-responses", articleResponsesConfig)
            .withCacheConfiguration("article-list-responses", articleListResponsesConfig)
            .withCacheConfiguration("article-search-responses", articleSearchResponsesConfig)
            .build();
    }
}

