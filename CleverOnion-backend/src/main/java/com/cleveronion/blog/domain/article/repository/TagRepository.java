package com.cleveronion.blog.domain.article.repository;

import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.valueobject.TagId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 标签仓储接口
 * 定义标签聚合的持久化操作
 * 使用领域模型作为参数和返回值，保持领域层的纯净性
 * 
 * @author CleverOnion
 */
public interface TagRepository {
    
    /**
     * 保存标签聚合
     * 
     * @param tag 标签聚合根
     * @return 保存后的标签聚合根（包含生成的ID）
     */
    TagAggregate save(TagAggregate tag);
    
    /**
     * 根据ID查找标签
     * 
     * @param id 标签ID
     * @return 标签聚合根的Optional包装
     */
    Optional<TagAggregate> findById(TagId id);
    
    /**
     * 根据名称查找标签
     * 
     * @param name 标签名称
     * @return 标签聚合根的Optional包装
     */
    Optional<TagAggregate> findByName(String name);
    
    /**
     * 根据ID集合查找标签列表
     * 
     * @param ids 标签ID集合
     * @return 标签列表
     */
    List<TagAggregate> findByIds(Set<TagId> ids);
    
    /**
     * 根据名称集合查找标签列表
     * 
     * @param names 标签名称集合
     * @return 标签列表
     */
    List<TagAggregate> findByNames(Set<String> names);
    
    /**
     * 根据ID删除标签
     * 
     * @param id 标签ID
     */
    void deleteById(TagId id);
    
    /**
     * 检查标签是否存在
     * 
     * @param id 标签ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(TagId id);
    
    /**
     * 检查指定名称的标签是否存在
     * 
     * @param name 标签名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByName(String name);
    
    /**
     * 查找所有标签
     * 
     * @return 标签列表
     */
    List<TagAggregate> findAll();
    
    /**
     * 根据名称关键词搜索标签
     * 
     * @param keyword 关键词
     * @return 标签列表
     */
    List<TagAggregate> findByNameContaining(String keyword);
    
    /**
     * 根据名称前缀查找标签
     * 
     * @param prefix 前缀
     * @return 标签列表
     */
    List<TagAggregate> findByNameStartingWith(String prefix);
    
    /**
     * 分页查询所有标签
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 标签列表
     */
    List<TagAggregate> findAll(int page, int size);
    
    /**
     * 统计标签总数
     * 
     * @return 标签总数
     */
    long count();
    
    /**
     * 查找最近创建的标签
     * 
     * @param limit 限制数量
     * @return 标签列表
     */
    List<TagAggregate> findRecentlyCreated(int limit);
    
    /**
     * 查找热门标签
     * 
     * @param limit 限制数量
     * @return 标签列表
     */
    List<TagAggregate> findPopularTags(int limit);
    
    /**
     * 按名称排序查找所有标签
     * 
     * @param ascending 是否升序
     * @return 标签列表
     */
    List<TagAggregate> findAllOrderByName(boolean ascending);
    
    /**
     * 按创建时间排序查找所有标签
     * 
     * @param ascending 是否升序
     * @return 标签列表
     */
    List<TagAggregate> findAllOrderByCreatedAt(boolean ascending);
    
    /**
     * 查找未使用的标签
     * 
     * @return 标签列表
     */
    List<TagAggregate> findUnusedTags();
    
    /**
     * 批量保存标签
     * 
     * @param tags 标签列表
     * @return 保存后的标签列表
     */
    List<TagAggregate> saveAll(List<TagAggregate> tags);
    
    /**
     * 批量删除标签
     * 
     * @param ids 标签ID集合
     */
    void deleteByIds(Set<TagId> ids);
    
    /**
     * 分页查询标签及其文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 标签及文章数量的映射
     */
    List<TagWithArticleCount> findTagsWithArticleCount(int page, int size);
    
    /**
     * 标签及其文章数量的数据传输对象
     */
    class TagWithArticleCount {
        private final TagAggregate tag;
        private final Long articleCount;
        
        public TagWithArticleCount(TagAggregate tag, Long articleCount) {
            this.tag = tag;
            this.articleCount = articleCount;
        }
        
        public TagAggregate getTag() {
            return tag;
        }
        
        public Long getArticleCount() {
            return articleCount;
        }
    }
}