package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;

/**
 * 带文章数量的分类响应DTO
 * 用于返回分类信息及其关联的文章数量
 * 
 * @author CleverOnion
 */
public class CategoryWithCountResponse {
    
    /**
     * 分类ID
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 使用该分类的文章数量
     */
    private Long articleCount;
    
    public CategoryWithCountResponse() {
    }
    
    public CategoryWithCountResponse(Long id, String name, Long articleCount) {
        this.id = id;
        this.name = name;
        this.articleCount = articleCount;
    }
    
    public CategoryWithCountResponse(Long id, String name, String icon, Long articleCount) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.articleCount = articleCount;
    }
    
    /**
     * 从CategoryAggregate和文章数量构造CategoryWithCountResponse
     * 
     * @param categoryAggregate 分类聚合根
     * @param articleCount 文章数量
     * @return 带文章数量的分类响应DTO
     */
    public static CategoryWithCountResponse from(CategoryAggregate categoryAggregate, Long articleCount) {
        if (categoryAggregate == null) {
            return null;
        }
        
        return new CategoryWithCountResponse(
            categoryAggregate.getId().getValue(),
            categoryAggregate.getName(),
            categoryAggregate.getIcon(),
            articleCount != null ? articleCount : 0L
        );
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public Long getArticleCount() {
        return articleCount;
    }
    
    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }
    
    @Override
    public String toString() {
        return "CategoryWithCountResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", articleCount=" + articleCount +
                '}';
    }
}