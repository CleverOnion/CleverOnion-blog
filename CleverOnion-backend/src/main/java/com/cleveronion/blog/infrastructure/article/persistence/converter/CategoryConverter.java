package com.cleveronion.blog.infrastructure.article.persistence.converter;

import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.infrastructure.article.persistence.po.CategoryPO;

/**
 * 分类聚合与持久化对象转换器
 * 负责CategoryAggregate和CategoryPO之间的相互转换
 * 
 * @author CleverOnion
 */
public class CategoryConverter {
    
    /**
     * 将CategoryAggregate转换为CategoryPO
     * 
     * @param categoryAggregate 分类聚合
     * @return 分类持久化对象
     */
    public static CategoryPO toCategoryPO(CategoryAggregate categoryAggregate) {
        if (categoryAggregate == null) {
            return null;
        }
        
        CategoryPO categoryPO = new CategoryPO(
            categoryAggregate.getName(),
            null, // CategoryAggregate暂时没有description属性，设置为null
            categoryAggregate.getIcon()
        );
        
        // 如果聚合有ID，说明是已存在的分类（更新操作）
        if (categoryAggregate.getId() != null) {
            categoryPO.setId(categoryAggregate.getId().getValue());
        }
        // 新创建的分类ID为null，不设置ID，让数据库自动生成
        
        // 时间戳由基础设施层的@CreationTimestamp和@UpdateTimestamp注解自动管理
        
        return categoryPO;
    }
    
    /**
     * 将CategoryPO转换为CategoryAggregate
     * 
     * @param categoryPO 分类持久化对象
     * @return 分类聚合
     */
    public static CategoryAggregate toCategoryAggregate(CategoryPO categoryPO) {
        if (categoryPO == null) {
            return null;
        }
        
        // 使用rebuild方法重建聚合
        CategoryAggregate categoryAggregate = CategoryAggregate.rebuild(
            CategoryId.of(categoryPO.getId()),
            categoryPO.getName(),
            categoryPO.getIcon()
        );
        
        // CategoryAggregate暂时没有description属性，跳过描述设置
        
        return categoryAggregate;
    }
    
    /**
     * 更新CategoryPO的字段（用于更新操作）
     * 
     * @param categoryPO 要更新的持久化对象
     * @param categoryAggregate 包含新数据的聚合
     */
    public static void updateCategoryPO(CategoryPO categoryPO, CategoryAggregate categoryAggregate) {
        if (categoryPO == null || categoryAggregate == null) {
            return;
        }
        
        categoryPO.setName(categoryAggregate.getName());
        categoryPO.setIcon(categoryAggregate.getIcon());
        // CategoryAggregate暂时没有description属性，跳过描述设置
        // updatedAt会由@UpdateTimestamp自动更新
    }
}