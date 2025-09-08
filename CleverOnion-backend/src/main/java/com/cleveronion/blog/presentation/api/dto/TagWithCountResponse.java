package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.domain.article.aggregate.TagAggregate;

/**
 * 带文章数量的标签响应DTO
 * 用于返回标签信息及其关联的文章数量
 * 
 * @author CleverOnion
 */
public class TagWithCountResponse {
    
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 使用该标签的文章数量
     */
    private Long articleCount;
    
    public TagWithCountResponse() {
    }
    
    public TagWithCountResponse(Long id, String name, Long articleCount) {
        this.id = id;
        this.name = name;
        this.articleCount = articleCount;
    }
    
    /**
     * 从TagAggregate和文章数量构造TagWithCountResponse
     * 
     * @param tagAggregate 标签聚合根
     * @param articleCount 文章数量
     * @return 带文章数量的标签响应DTO
     */
    public static TagWithCountResponse from(TagAggregate tagAggregate, Long articleCount) {
        if (tagAggregate == null) {
            return null;
        }
        
        return new TagWithCountResponse(
            tagAggregate.getId().getValue(),
            tagAggregate.getName(),
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
    
    public Long getArticleCount() {
        return articleCount;
    }
    
    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }
    
    @Override
    public String toString() {
        return "TagWithCountResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", articleCount=" + articleCount +
                '}';
    }
}