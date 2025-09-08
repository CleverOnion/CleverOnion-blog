package com.cleveronion.blog.infrastructure.article.persistence.converter;

import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.infrastructure.article.persistence.po.TagPO;

/**
 * 标签聚合与持久化对象转换器
 * 负责TagAggregate和TagPO之间的相互转换
 * 
 * @author CleverOnion
 */
public class TagConverter {
    
    /**
     * 将TagAggregate转换为TagPO
     * 
     * @param tagAggregate 标签聚合
     * @return 标签持久化对象
     */
    public static TagPO toTagPO(TagAggregate tagAggregate) {
        if (tagAggregate == null) {
            return null;
        }
        
        TagPO tagPO = new TagPO(
            tagAggregate.getName(),
            null // TagAggregate暂时没有color属性，设置为null
        );
        
        // 如果聚合有ID，说明是已存在的标签
        if (tagAggregate.getId() != null) {
            tagPO.setId(tagAggregate.getId().getValue());
        }
        
        // 时间戳由基础设施层的@CreationTimestamp和@UpdateTimestamp注解自动管理
        
        return tagPO;
    }
    
    /**
     * 将TagPO转换为TagAggregate
     * 
     * @param tagPO 标签持久化对象
     * @return 标签聚合
     */
    public static TagAggregate toTagAggregate(TagPO tagPO) {
        if (tagPO == null) {
            return null;
        }
        
        // 使用rebuild方法重建聚合
        TagAggregate tagAggregate = TagAggregate.rebuild(
            TagId.of(tagPO.getId()),
            tagPO.getName()
        );
        
        return tagAggregate;
    }
    
    /**
     * 更新TagPO的字段（用于更新操作）
     * 
     * @param tagPO 要更新的持久化对象
     * @param tagAggregate 包含新数据的聚合
     */
    public static void updateTagPO(TagPO tagPO, TagAggregate tagAggregate) {
        if (tagPO == null || tagAggregate == null) {
            return;
        }
        
        tagPO.setName(tagAggregate.getName());
        // TagAggregate暂时没有color属性，跳过color设置
        // updatedAt会由@UpdateTimestamp自动更新
    }
}