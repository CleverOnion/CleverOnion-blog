package com.cleveronion.blog.common.cache;

/**
 * 缓存名称常量定义
 * 统一管理所有缓存的名称，便于维护和修改
 * 
 * <p>缓存 Key 格式说明：
 * <ul>
 *   <li>完整格式：{key-prefix}::{cacheName}::{key}</li>
 *   <li>示例：cleveronion:cache::article:detail::123</li>
 * </ul>
 * 
 * <p>缓存过期时间（TTL）配置：
 * <ul>
 *   <li>开发环境：10 分钟（便于调试验证）</li>
 *   <li>测试环境：5 分钟（快速测试）</li>
 *   <li>生产环境：30 分钟（标准配置）</li>
 * </ul>
 * 
 * @author CleverOnion
 * @since 2.0.0
 * @see com.cleveronion.blog.infrastructure.common.config.RedisCacheConfig
 */
public final class CacheNames {
    
    // 私有构造函数，防止实例化
    private CacheNames() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== 文章相关缓存 ====================
    
    /**
     * 文章详情缓存
     * 
     * <p>用途：缓存单篇文章的完整信息（包含内容、分类、标签、作者等）
     * 
     * <p>Key 格式：{articleId}
     * <p>示例：article:detail::123
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：30 分钟</li>
     * </ul>
     * 
     * <p>失效时机：文章被创建、更新、发布、归档、删除时
     */
    public static final String ARTICLE_DETAIL = "article:detail";
    
    /**
     * 文章列表缓存
     * 
     * <p>用途：缓存文章列表查询结果（分页、按状态、按分类、按标签等）
     * 
     * <p>Key 格式：{查询条件}:page:{page}:size:{size}
     * <p>示例：
     * <ul>
     *   <li>article:list::published:page:0:size:10</li>
     *   <li>article:list::status:PUBLISHED:page:0:size:10</li>
     *   <li>article:list::category:123:page:0:size:10</li>
     *   <li>article:list::tag:456:page:0:size:10</li>
     * </ul>
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：10 分钟</li>
     * </ul>
     * 
     * <p>失效时机：任何文章操作（创建、更新、删除等）、分类/标签变更时
     */
    public static final String ARTICLE_LIST = "article:list";
    
    /**
     * 文章统计缓存
     * 
     * <p>用途：缓存文章数量统计（总数、按状态、按分类、按标签等）
     * 
     * <p>Key 格式：{统计维度}
     * <p>示例：
     * <ul>
     *   <li>article:count::published</li>
     *   <li>article:count::all</li>
     *   <li>article:count::status:PUBLISHED</li>
     *   <li>article:count::category:123</li>
     * </ul>
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：5 分钟</li>
     * </ul>
     * 
     * <p>失效时机：文章被创建、发布、删除时
     */
    public static final String ARTICLE_COUNT = "article:count";
    
    // ==================== 分类相关缓存 ====================
    
    /**
     * 分类详情缓存
     * 
     * <p>用途：缓存单个分类的详细信息
     * 
     * <p>Key 格式：{categoryId}
     * <p>示例：category:detail::456
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：1 小时</li>
     * </ul>
     * 
     * <p>失效时机：分类被创建、更新、删除时
     */
    public static final String CATEGORY_DETAIL = "category:detail";
    
    /**
     * 分类列表缓存
     * 
     * <p>用途：缓存分类列表查询结果
     * 
     * <p>Key 格式：all 或 batch:{ids}
     * <p>示例：
     * <ul>
     *   <li>category:list::all</li>
     *   <li>category:list::batch:[1,2,3]</li>
     * </ul>
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：30 分钟</li>
     * </ul>
     * 
     * <p>失效时机：任何分类操作（创建、更新、删除）
     */
    public static final String CATEGORY_LIST = "category:list";
    
    /**
     * 分类统计缓存
     * 
     * <p>用途：缓存分类相关的统计信息（如分类下的文章数量）
     * 
     * <p>Key 格式：{categoryId}
     * <p>示例：category:count::456
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：10 分钟</li>
     * </ul>
     * 
     * <p>失效时机：分类下的文章数量变化时
     */
    public static final String CATEGORY_COUNT = "category:count";
    
    // ==================== 标签相关缓存 ====================
    
    /**
     * 标签详情缓存
     * 
     * <p>用途：缓存单个标签的详细信息
     * 
     * <p>Key 格式：{tagId}
     * <p>示例：tag:detail::789
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：1 小时</li>
     * </ul>
     * 
     * <p>失效时机：标签被创建、更新、删除时
     */
    public static final String TAG_DETAIL = "tag:detail";
    
    /**
     * 标签列表缓存
     * 
     * <p>用途：缓存标签列表查询结果
     * 
     * <p>Key 格式：all 或 batch:{ids}
     * <p>示例：
     * <ul>
     *   <li>tag:list::all</li>
     *   <li>tag:list::batch:[1,2,3]</li>
     * </ul>
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：30 分钟</li>
     * </ul>
     * 
     * <p>失效时机：任何标签操作（创建、更新、删除）
     */
    public static final String TAG_LIST = "tag:list";
    
    /**
     * 标签统计缓存
     * 
     * <p>用途：缓存标签相关的统计信息（如标签下的文章数量）
     * 
     * <p>Key 格式：{tagId}
     * <p>示例：tag:count::789
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：10 分钟</li>
     * </ul>
     * 
     * <p>失效时机：标签下的文章数量变化时
     */
    public static final String TAG_COUNT = "tag:count";
    
    // ==================== 用户相关缓存 ====================
    
    /**
     * 用户详情缓存
     * 
     * <p>用途：缓存用户的基本信息
     * 
     * <p>Key 格式：{userId}
     * <p>示例：user:detail::111
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：30 分钟</li>
     * </ul>
     * 
     * <p>失效时机：用户信息被更新时
     */
    public static final String USER_DETAIL = "user:detail";
    
    /**
     * 用户列表缓存
     * 
     * <p>用途：缓存批量用户查询结果
     * 
     * <p>Key 格式：batch:{ids}
     * <p>示例：user:list::batch:[1,2,3]
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：30 分钟</li>
     * </ul>
     * 
     * <p>失效时机：用户信息被更新时
     */
    public static final String USER_LIST = "user:list";
    
    // ==================== 评论相关缓存 ====================
    
    /**
     * 评论列表缓存
     * 
     * <p>用途：缓存文章的评论列表
     * 
     * <p>Key 格式：article:{articleId}:page:{page}:size:{size}
     * <p>示例：comment:list::article:123:page:0:size:10
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：5 分钟（评论更新频繁）</li>
     * </ul>
     * 
     * <p>失效时机：评论被创建、删除时
     */
    public static final String COMMENT_LIST = "comment:list";
    
    /**
     * 评论统计缓存
     * 
     * <p>用途：缓存文章的评论数量
     * 
     * <p>Key 格式：article:{articleId}
     * <p>示例：comment:count::article:123
     * 
     * <p>TTL：
     * <ul>
     *   <li>开发环境：10 分钟</li>
     *   <li>生产环境：5 分钟</li>
     * </ul>
     * 
     * <p>失效时机：评论被创建、删除时
     */
    public static final String COMMENT_COUNT = "comment:count";
}

